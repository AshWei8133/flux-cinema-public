package com.flux.movieproject.service.member;

import com.flux.movieproject.model.dto.member.MemberLevelDTO;
import com.flux.movieproject.model.dto.member.MemberLevelStatusDTO;
import com.flux.movieproject.model.entity.member.MemberLevel;
import com.flux.movieproject.model.entity.member.MemberLevelRecord;
import com.flux.movieproject.model.entity.product.OrderStatus;
import com.flux.movieproject.model.entity.product.ProductOrder;
import com.flux.movieproject.model.entity.theater.TicketOrder;
import com.flux.movieproject.repository.member.MemberLevelRecordRepository;
import com.flux.movieproject.repository.member.MemberLevelRepository;
import com.flux.movieproject.repository.product.ProductOrderRepository;
import com.flux.movieproject.repository.ticket.TicketOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberLevelService {

	private final ProductOrderRepository productOrderRepository;
	private final TicketOrderRepository ticketOrderRepository;
	private final MemberLevelRepository memberLevelRepository;
	private final MemberLevelRecordRepository memberLevelRecordRepository;

	public MemberLevelService(ProductOrderRepository productOrderRepository,
			TicketOrderRepository ticketOrderRepository, MemberLevelRepository memberLevelRepository,
			MemberLevelRecordRepository memberLevelRecordRepository) {
		this.productOrderRepository = productOrderRepository;
		this.ticketOrderRepository = ticketOrderRepository;
		this.memberLevelRepository = memberLevelRepository;
		this.memberLevelRecordRepository = memberLevelRecordRepository;
	}

	// --- 提供給前端使用的方法 ---

	@Transactional(readOnly = true)
	public MemberLevelStatusDTO getMemberLevelStatus(Integer memberId) {
		MemberLevel currentLevel = getCurrentLevel(memberId);
		BigDecimal totalSpent = calculateTotalSpent(memberId);
		MemberLevel nextLevel = findNextLevel(currentLevel);
		BigDecimal progress = calculateProgress(totalSpent, currentLevel, nextLevel);

		return MemberLevelStatusDTO.builder().currentLevelName(currentLevel.getLevelName())
				.currentLevelIcon(currentLevel.getLevelIcon()).totalSpent(totalSpent)
				.nextLevelName(nextLevel != null ? nextLevel.getLevelName() : null)
				.nextLevelThreshold(nextLevel != null ? new BigDecimal(nextLevel.getThresholdLowerBound()) : null)
				.upgradeConditionDescription(nextLevel != null ? nextLevel.getUpgradeConditionDescription() : "已達最高等級")
				.progressPercentage(progress).build();
	}

	@Transactional
	public void updateLevelOnPurchase(Integer memberId) {
		BigDecimal totalSpent = calculateTotalSpent(memberId);
		MemberLevel currentLevel = getCurrentLevel(memberId);
		MemberLevel newPotentialLevel = determineLevelBySpending(totalSpent);

		if (!currentLevel.getMemberLevelId().equals(newPotentialLevel.getMemberLevelId())) {
			memberLevelRecordRepository.findByMemberIdAndEndDateIsNull(memberId).ifPresent(record -> {
				record.setEndDate(LocalDateTime.now());
				memberLevelRecordRepository.save(record);
			});
			MemberLevelRecord newRecord = MemberLevelRecord.builder().memberId(memberId)
					.memberLevelId(newPotentialLevel.getMemberLevelId()).startDate(LocalDateTime.now()).endDate(null)
					.build();
			memberLevelRecordRepository.save(newRecord);
		}
	}

	// --- 提供給管理後台使用的方法 ---

	@Transactional(readOnly = true)
	public List<MemberLevelDTO> getAllLevels() {
		return memberLevelRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
	}

	@Transactional
	public MemberLevelDTO createLevel(MemberLevelDTO dto, MultipartFile levelIcon) {
		MemberLevel memberLevel = convertToEntity(dto);
		if (levelIcon != null && !levelIcon.isEmpty()) {
			try {
				memberLevel.setLevelIcon(levelIcon.getBytes());
			} catch (IOException e) {
				throw new RuntimeException("Could not read file: " + levelIcon.getOriginalFilename(), e);
			}
		}
		MemberLevel savedLevel = memberLevelRepository.save(memberLevel);
		return convertToDto(savedLevel);
	}

	@Transactional
	public MemberLevelDTO updateLevel(Integer id, MemberLevelDTO dto, MultipartFile levelIcon) {
		MemberLevel existingLevel = memberLevelRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("MemberLevel not found with id: " + id));

		existingLevel.setLevelName(dto.getLevelName());
		existingLevel.setThresholdLowerBound(dto.getThresholdLowerBound());
		existingLevel.setUpgradeConditionDescription(dto.getUpgradeConditionDescription());
		if (levelIcon != null && !levelIcon.isEmpty()) {
			try {
				existingLevel.setLevelIcon(levelIcon.getBytes());
			} catch (IOException e) {
				throw new RuntimeException("Could not read file: " + levelIcon.getOriginalFilename(), e);
			}
		}
		MemberLevel updatedLevel = memberLevelRepository.save(existingLevel);
		return convertToDto(updatedLevel);
	}

	@Transactional
	public void deleteLevel(Integer id) {
		if (!memberLevelRepository.existsById(id)) {
			throw new EntityNotFoundException("MemberLevel not found with id: " + id);
		}
		memberLevelRepository.deleteById(id);
	}

	@Transactional
	public MemberLevelRecord levelUp(Integer memberId) {
		BigDecimal totalSpent = calculateTotalSpent(memberId);
		Optional<MemberLevelRecord> currentLevelRecordOpt = memberLevelRecordRepository
				.findTopByMemberIdOrderByStartDateDesc(memberId);
		MemberLevelRecord currentLevelRecord = currentLevelRecordOpt.get();
		Optional<MemberLevel> currentLevelOpt = memberLevelRepository.findById(currentLevelRecord.getMemberLevelId());
		MemberLevel currentLevel = currentLevelOpt.get();
		Optional<MemberLevel> nextLevelOpt = memberLevelRepository.findById(currentLevel.getMemberLevelId() + 1);
		if (nextLevelOpt == null || nextLevelOpt.isEmpty()) {
			return null;
		}
		MemberLevel nextLevel = nextLevelOpt.get();
		if (totalSpent.compareTo(BigDecimal.valueOf(nextLevel.getThresholdLowerBound())) < 0) {
			return null;
		} else {
			currentLevelRecord.setEndDate(LocalDateTime.now());
			memberLevelRecordRepository.save(currentLevelRecord);

			MemberLevelRecord newLevelRecord = new MemberLevelRecord();
			newLevelRecord.setMemberId(memberId);
			newLevelRecord.setMemberLevelId(nextLevel.getMemberLevelId());
			newLevelRecord.setStartDate(currentLevelRecord.getEndDate());
			newLevelRecord.setEndDate(newLevelRecord.getStartDate().plusMonths(1));
			return memberLevelRecordRepository.save(newLevelRecord);
		}

	}

	// --- 私有輔助方法 ---

	private MemberLevel getCurrentLevel(Integer memberId) {
		return memberLevelRecordRepository.findByMemberIdAndEndDateIsNull(memberId)
				.map(record -> memberLevelRepository.findById(record.getMemberLevelId()).orElse(findLowestLevel()))
				.orElseGet(() -> {
					BigDecimal totalSpent = calculateTotalSpent(memberId);
					MemberLevel initialLevel = determineLevelBySpending(totalSpent);
					MemberLevelRecord newRecord = MemberLevelRecord.builder().memberId(memberId)
							.memberLevelId(initialLevel.getMemberLevelId()).startDate(LocalDateTime.now()).endDate(null)
							.build();
//					memberLevelRecordRepository.save(newRecord);
					return initialLevel;
				});
	}

	private BigDecimal calculateTotalSpent(Integer memberId) {
		BigDecimal productTotal = productOrderRepository
				.findByMemberMemberIdAndOrderStatus(memberId, OrderStatus.COMPLETED).stream()
				.map(ProductOrder::getFinalPaymentAmount).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal ticketTotal = ticketOrderRepository.findEligibleForSpending(memberId, LocalDateTime.now()).stream()
				.map(TicketOrder::getTotalAmount).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);

		return productTotal.add(ticketTotal);
	}

	private MemberLevel determineLevelBySpending(BigDecimal totalSpent) {
		return memberLevelRepository.findAllByOrderByThresholdLowerBoundDesc().stream()
				.filter(level -> totalSpent.compareTo(new BigDecimal(level.getThresholdLowerBound())) >= 0).findFirst()
				.orElse(findLowestLevel());
	}

	private MemberLevel findNextLevel(MemberLevel currentLevel) {
		return memberLevelRepository.findAll().stream()
				.filter(level -> level.getThresholdLowerBound() > currentLevel.getThresholdLowerBound())
				.min(Comparator.comparing(MemberLevel::getThresholdLowerBound)).orElse(null);
	}

	private BigDecimal calculateProgress(BigDecimal totalSpent, MemberLevel current, MemberLevel next) {
		if (next == null)
			return new BigDecimal("100");
		BigDecimal currentThreshold = new BigDecimal(current.getThresholdLowerBound());
		BigDecimal nextThreshold = new BigDecimal(next.getThresholdLowerBound());
		BigDecimal range = nextThreshold.subtract(currentThreshold);
		if (range.compareTo(BigDecimal.ZERO) <= 0)
			return new BigDecimal("100");
		BigDecimal progressInRange = totalSpent.subtract(currentThreshold);
		return progressInRange.multiply(new BigDecimal("100")).divide(range, 2, RoundingMode.HALF_UP);
	}

	private MemberLevel findLowestLevel() {
		return memberLevelRepository.findAll().stream().min(Comparator.comparing(MemberLevel::getThresholdLowerBound))
				.orElseThrow(() -> new IllegalStateException("No member levels found in the database."));
	}

	private MemberLevelDTO convertToDto(MemberLevel entity) {
		return MemberLevelDTO.builder().memberLevelId(entity.getMemberLevelId()).levelName(entity.getLevelName())
				.levelIcon(entity.getLevelIcon()).thresholdLowerBound(entity.getThresholdLowerBound())
				.upgradeConditionDescription(entity.getUpgradeConditionDescription()).build();
	}

	private MemberLevel convertToEntity(MemberLevelDTO dto) {
		return MemberLevel.builder().memberLevelId(dto.getMemberLevelId()).levelName(dto.getLevelName())
				.levelIcon(dto.getLevelIcon()).thresholdLowerBound(dto.getThresholdLowerBound())
				.upgradeConditionDescription(dto.getUpgradeConditionDescription()).build();
	}
}
