package com.flux.movieproject.service.ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.dto.ticket.PriceRuleUpdateDTO;
import com.flux.movieproject.model.dto.ticket.TicketTypeResponseDTO;
import com.flux.movieproject.model.dto.ticket.UpdateTicketTypeDTO;
import com.flux.movieproject.model.entity.theater.TheaterType;
import com.flux.movieproject.model.entity.theater.TicketPriceId;
import com.flux.movieproject.model.entity.theater.TicketPriceRule;
import com.flux.movieproject.model.entity.theater.TicketType;
import com.flux.movieproject.repository.theater.TheaterTypeRepository;
import com.flux.movieproject.repository.ticket.TicketPriceRuleRepository;
import com.flux.movieproject.repository.ticket.TicketTypeRepository;

@Service
@Transactional(readOnly = true)
public class TicketService {

	@Autowired
	private TicketTypeRepository ticketTypeRepo;
	@Autowired
	private TicketPriceRuleRepository ticketPriceRuleRepo;
	@Autowired
	private TheaterTypeRepository theaterTypeRepo;

	/**
	 * 取得全部票種資料
	 * 
	 * @return
	 */
	public List<TicketType> findAllTicketTypes() {
		return ticketTypeRepo.findAll();
	}

	/**
	 * 新增票種資料
	 * 
	 * @param ticketTypeDTO
	 * @return
	 */
	@Transactional
	public TicketTypeResponseDTO createTicketType(UpdateTicketTypeDTO ticketTypeDTO) {
		// 1. 將傳入的 AddTicketTypeDTO 轉換為資料庫實體 TicketType
		TicketType newTicketType = new TicketType();
		newTicketType.setTicketTypeName(ticketTypeDTO.getTicketTypeName());
		newTicketType.setDescription(ticketTypeDTO.getDescription());
		newTicketType.setIsEnabled(ticketTypeDTO.getIsEnabled());
		newTicketType.setDiscountType(ticketTypeDTO.getDiscountType());

		if (ticketTypeDTO.getDiscountType() == null) {
			newTicketType.setDiscountValue(java.math.BigDecimal.ZERO);
		} else {
			newTicketType.setDiscountValue(ticketTypeDTO.getDiscountValue());
		}

		TicketType savedTicketType = ticketTypeRepo.save(newTicketType);
		
		// 取得所有影廳類型，為這個新票種建立對應的票價規則
        List<TheaterType> allTheaterTypes = theaterTypeRepo.findAll();
        Map<Integer, Integer> basePrices = findBasePrices();

        for (TheaterType theaterType : allTheaterTypes) {
            Integer basePrice = basePrices.getOrDefault(theaterType.getTheaterTypeId(), 0);
            recalculateAndSavePriceRule(theaterType, savedTicketType, basePrice);
        }

		// 直接建立並回傳成功的 Response DTO
		return new TicketTypeResponseDTO(true, "票種新增成功，並已自動生成對應票價規則");
	}

	/**
	 * 更新指定的票種資料
	 * 
	 * @param ticketTypeId 要更新的票種 ID
	 * @param dto          包含更新資訊的 DTO
	 * @return 操作結果的 DTO
	 */
	@Transactional
	public TicketTypeResponseDTO updateTicketType(Integer ticketTypeId, UpdateTicketTypeDTO dto) {
		// 1. 根據 ID 從資料庫尋找，回傳一個 Optional 物件
		Optional<TicketType> optionalTicketType = ticketTypeRepo.findById(ticketTypeId);

		// 2. 使用 if 判斷式檢查 Optional 物件中是否有值
		if (!optionalTicketType.isPresent()) {
			// 如果沒有值 (代表找不到對應的票種)，就拋出例外
			throw new RuntimeException("找不到 ID 為 " + ticketTypeId + " 的票種");
		}

		// 3. 如果檢查通過，代表有值，就從 Optional 中安全地取出 TicketType 物件
		TicketType existingTicketType = optionalTicketType.get();

		// 將 DTO 中的新資料更新到現有的 Entity 物件上
		existingTicketType.setTicketTypeName(dto.getTicketTypeName());
		existingTicketType.setDescription(dto.getDescription());
		existingTicketType.setIsEnabled(dto.getIsEnabled());
		existingTicketType.setDiscountType(dto.getDiscountType());

		if (dto.getDiscountType() == null) {
			existingTicketType.setDiscountValue(java.math.BigDecimal.ZERO);
		} else {
			existingTicketType.setDiscountValue(dto.getDiscountValue());
		}

		TicketType savedTicketType = ticketTypeRepo.save(existingTicketType);
		
		List<TheaterType> allTheaterTypes = theaterTypeRepo.findAll();
        Map<Integer, Integer> basePrices = findBasePrices();

        for (TheaterType theaterType : allTheaterTypes) {
            Integer basePrice = basePrices.getOrDefault(theaterType.getTheaterTypeId(), 0);
            recalculateAndSavePriceRule(theaterType, savedTicketType, basePrice);
        }

		return new TicketTypeResponseDTO(true, "票種更新成功，並已自動更新相關票價規則");
	}

	/**
	 * 刪除指定的票種資料
	 * 
	 * @param ticketTypeId 要刪除的票種 ID
	 * @return 操作結果的 DTO
	 */
	@Transactional
	public TicketTypeResponseDTO deleteTicketType(Integer ticketTypeId) {
		if (!ticketTypeRepo.existsById(ticketTypeId)) {
			throw new RuntimeException("找不到 ID 為 " + ticketTypeId + " 的票種，無法刪除");
		}

		ticketTypeRepo.deleteById(ticketTypeId);

		return new TicketTypeResponseDTO(true, "票種刪除成功");
	}

	/**
	 * 查詢所有影廳類型的基礎票價(即全票時的價格)
	 * 
	 * @return 影廳類別ID比對價格的鍵值對
	 */
	public Map<Integer, Integer> findBasePrices() {
		// 1. 使用 Repository 查詢所有票種名稱為 "全票" 的票價規則
		List<TicketPriceRule> fullPriceRules = ticketPriceRuleRepo.findByTicketTypeTicketTypeName("全票");

		// 2. 建立一個新的 HashMap 來存放結果
		Map<Integer, Integer> basePrices = new HashMap<>();

		// 3. 使用傳統 for 迴圈遍歷查詢結果
		for (TicketPriceRule rule : fullPriceRules) {
			// 將 theaterTypeId 作為 Key，price 作為 Value，存入 Map 中
			basePrices.put(rule.getTheaterType().getTheaterTypeId(), rule.getPrice());
		}

		// 4. 回傳整理好的 Map
		return basePrices;
	}

	/**
	 * 批次更新或新增票價規則
	 * 
	 * @param dtoList 前端傳來的票價規則列表
	 * @return 操作結果的 DTO
	 */
	@Transactional
	public TicketTypeResponseDTO batchUpdatePriceRules(List<PriceRuleUpdateDTO> dtoList) {
		// 使用 for 迴圈遍歷前端傳來的每一筆資料
		for (PriceRuleUpdateDTO dto : dtoList) {
			// 1. 根據 dto 的 theaterTypeId 和 ticketTypeId 組合出複合主鍵
			TicketPriceId id = new TicketPriceId(dto.getTheaterTypeId(), dto.getTicketTypeId());

			// 2. 嘗試從資料庫中尋找是否已存在此規則
			Optional<TicketPriceRule> existingRuleOpt = ticketPriceRuleRepo.findById(id);

			if (existingRuleOpt.isPresent()) {
				// 3a. 如果規則已存在，則執行「更新」
				TicketPriceRule ruleToUpdate = existingRuleOpt.get();
				ruleToUpdate.setPrice(dto.getPrice());
				ticketPriceRuleRepo.save(ruleToUpdate);
			} else {
				// 3b. 如果規則不存在，則執行「新增」
				TicketPriceRule newRule = new TicketPriceRule();
				newRule.setTicketPriceId(id);
				newRule.setPrice(dto.getPrice());

				// 為了建立完整的 Entity 關聯，需要從資料庫中找到對應的物件
				TheaterType theaterType = theaterTypeRepo.findById(dto.getTheaterTypeId())
						.orElseThrow(() -> new RuntimeException("找不到 ID 為 " + dto.getTheaterTypeId() + " 的影廳類型"));
				TicketType ticketType = ticketTypeRepo.findById(dto.getTicketTypeId())
						.orElseThrow(() -> new RuntimeException("找不到 ID 為 " + dto.getTicketTypeId() + " 的票種"));

				newRule.setTheaterType(theaterType);
				newRule.setTicketType(ticketType);

				// 設定預設值
				newRule.setValidSDate(LocalDateTime.now());

				ticketPriceRuleRepo.save(newRule);
			}
		}

		return new TicketTypeResponseDTO(true, "票價規則已成功儲存");
	}
	
	/**
     * 一個私有的輔助方法，用於計算並儲存單一的票價規則
     * @param theaterType 影廳類型
     * @param ticketType 票種
     * @param basePrice 該影廳類型的基礎票價
     */
    private void recalculateAndSavePriceRule(TheaterType theaterType, TicketType ticketType, Integer basePrice) {
        int finalPrice = basePrice; // 預設價格等於基礎價

        if (ticketType.getDiscountType() != null) {
            switch (ticketType.getDiscountType()) {
                case PERCENTAGE:
                    // BigDecimal 提供更精確的計算
                    BigDecimal calculated = new BigDecimal(basePrice).multiply(ticketType.getDiscountValue());
                    finalPrice = calculated.setScale(0, RoundingMode.HALF_UP).intValue(); // 四捨五入到整數
                    break;
                case FIXED:
                    finalPrice = basePrice + ticketType.getDiscountValue().intValue();
                    break;
                default:
                    break;
            }
        }
        
        // 確保價格不小於 0
        finalPrice = Math.max(0, finalPrice);

        // 準備儲存或更新
        TicketPriceId id = new TicketPriceId(theaterType.getTheaterTypeId(), ticketType.getTicketTypeId());
        Optional<TicketPriceRule> existingRuleOpt = ticketPriceRuleRepo.findById(id);

        TicketPriceRule rule;
        if (existingRuleOpt.isPresent()) {
            rule = existingRuleOpt.get(); // 更新現有規則
        } else {
            rule = new TicketPriceRule(); // 建立新規則
            rule.setTicketPriceId(id);
            rule.setTheaterType(theaterType);
            rule.setTicketType(ticketType);
            rule.setValidSDate(LocalDateTime.now());
        }
        
        rule.setPrice(finalPrice);
        ticketPriceRuleRepo.save(rule);
    }

}
