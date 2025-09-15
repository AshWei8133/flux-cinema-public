package com.flux.movieproject.service.ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.flux.movieproject.enums.DiscountType;
import com.flux.movieproject.enums.OrderDetailStatus;
import com.flux.movieproject.enums.OrderStatus;
import com.flux.movieproject.enums.SeatStatus;
import com.flux.movieproject.model.dto.ticket.CouponInfoForTicketDTO;
import com.flux.movieproject.model.dto.ticket.ManualPaymentDTO;
import com.flux.movieproject.model.dto.ticket.MemberInfoForTicketDTO;
import com.flux.movieproject.model.dto.ticket.ReservationRequest;
import com.flux.movieproject.model.dto.ticket.ReservationResponse;
import com.flux.movieproject.model.dto.ticket.TicketItemDTO;
import com.flux.movieproject.model.dto.ticket.TicketOrderDetailDTO;
import com.flux.movieproject.model.dto.ticket.TicketOrderHistoryDTO;
import com.flux.movieproject.model.dto.ticket.TicketOrderSearchRequestDTO;
import com.flux.movieproject.model.dto.ticket.TicketOrderSummaryDTO;
import com.flux.movieproject.model.dto.ticket.TicketRequest;
import com.flux.movieproject.model.dto.ticket.UpdateOrderResponse;
import com.flux.movieproject.model.dto.ticket.UpdateTicketOrderResponseDTO;
import com.flux.movieproject.model.entity.event.Coupon;
import com.flux.movieproject.model.entity.event.EventEligibility;
import com.flux.movieproject.model.entity.event.TicketOrderApplicableCouponDTO;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.model.entity.member.MemberCoupon;
import com.flux.movieproject.model.entity.member.MemberLevelRecord;
import com.flux.movieproject.model.entity.theater.MovieSession;
import com.flux.movieproject.model.entity.theater.SessionSeat;
import com.flux.movieproject.model.entity.theater.TicketOrder;
import com.flux.movieproject.model.entity.theater.TicketOrderDetail;
import com.flux.movieproject.model.entity.theater.TicketType;
import com.flux.movieproject.repository.member.MemberCouponRepository;
import com.flux.movieproject.repository.member.MemberLevelRecordRepository;
import com.flux.movieproject.repository.member.MemberRepository;
import com.flux.movieproject.repository.moviesession.MovieSessionRepository;
import com.flux.movieproject.repository.ticket.SessionSeatRepository;
import com.flux.movieproject.repository.ticket.TicketOrderRepository;
import com.flux.movieproject.repository.ticket.TicketTypeRepository;
import com.flux.movieproject.utils.OrderNumberUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class TicketOrderService {
	@Autowired
	private TicketOrderRepository ticketOrderRepo;
	@Autowired
	private SessionSeatRepository sessionSeatRepo;
	@Autowired
	private MemberRepository memberRepo;
	@Autowired
	private MovieSessionRepository movieSessionRepo;
	@Autowired
	private TicketTypeRepository ticketTypeRepo;
	@Autowired
	private MemberCouponRepository memberCouponRepo;
	@Autowired
	private MemberLevelRecordRepository memberLevelRecordRepo;

	public Page<TicketOrderSummaryDTO> findOrdersByCriteria(TicketOrderSearchRequestDTO searchDTO) {
		/**
		 * Specification<TicketOrder> spec = (root, query, cb) -> { ... };
		 * 根據情況自由組合的「查詢規則」 root: 代表查詢的起始點，也就是 TicketOrder 這張表。 query: 代表整個查詢語句，可以用來設定
		 * JOIN 等進階操作。 cb (CriteriaBuilder): 這是一個「規則產生器」，我們用它來建立各種查詢條件， 例如「等於
		 * (equal)」、「相似 (like)」、「介於 (between)」。
		 */
		// Specification 讓我們可以用 Java 物件的方式來組合 SQL 的 WHERE 條件
		Specification<TicketOrder> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// 為了查詢會員資訊，需要 JOIN member 表
			Join<TicketOrder, Member> memberJoin = root.join("member");

			// 判斷傳入的是否為專業訂單編號 (例如 FX 開頭)
			if (StringUtils.hasText(searchDTO.getOrderNumber()) && searchDTO.getOrderNumber().startsWith("FX")) {
				try {
					// 如果是，則先解碼回 ID
					Integer decodedId = OrderNumberUtils.decode(searchDTO.getOrderNumber());
					// 清空所有其他條件，只進行精確的 ID 查詢
					predicates.clear();
					predicates.add(cb.equal(root.get("ticketOrderId"), decodedId));
					// 將所有條件用 AND 連接起來並返回，結束後續的條件判斷
					return cb.and(predicates.toArray(new Predicate[0]));
				} catch (IllegalArgumentException e) {
					// 如果解碼失敗，代表格式錯誤，可以讓它查無結果
					// 這裡我們加入一個永遠為 false 的條件
					return cb.disjunction(); // or cb.equal(cb.literal(1), 0)
				}
			}

			// 根據 DTO 中的條件，動態增加查詢 Predicate
			// StringUtils.hasText() 檢查傳來的字串是不是 null 或 空字串
			if (StringUtils.hasText(searchDTO.getUsername())) {
				predicates.add(cb.like(memberJoin.get("username"), "%" + searchDTO.getUsername() + "%"));
			}
			if (StringUtils.hasText(searchDTO.getEmail())) {
				predicates.add(cb.like(memberJoin.get("email"), "%" + searchDTO.getEmail() + "%"));
			}
			if (StringUtils.hasText(searchDTO.getPhone())) {
				predicates.add(cb.like(memberJoin.get("phone"), "%" + searchDTO.getPhone() + "%"));
			}
			if (StringUtils.hasText(searchDTO.getStatus())) {
				try {
					// 【修改】將傳入的狀態字串轉換為 Enum
					OrderStatus statusEnum = OrderStatus.valueOf(searchDTO.getStatus().toUpperCase());
					predicates.add(cb.equal(root.get("status"), statusEnum));
				} catch (IllegalArgumentException e) {
					// 如果傳入無效的狀態字串，讓查詢查無結果
					predicates.add(cb.disjunction());
				}
			}
			if (StringUtils.hasText(searchDTO.getPaymentType())) {
				predicates.add(cb.equal(root.get("paymentType"), searchDTO.getPaymentType()));
			}
			if (searchDTO.getStartDate() != null && searchDTO.getEndDate() != null) {
				// 情況一：起始和結束日期都有 -> 使用 between
				predicates.add(cb.between(root.get("createdTime"), searchDTO.getStartDate().atStartOfDay(),
						searchDTO.getEndDate().plusDays(1).atStartOfDay()));
			} else if (searchDTO.getStartDate() != null) {
				// 情況二：只有起始日期 -> 查詢大於等於該日期的所有訂單
				predicates
						.add(cb.greaterThanOrEqualTo(root.get("createdTime"), searchDTO.getStartDate().atStartOfDay()));
			} else if (searchDTO.getEndDate() != null) {
				// 情況三：只有結束日期 -> 查詢小於等於該日期（的結束時間）的所有訂單
				predicates.add(cb.lessThan(root.get("createdTime"), searchDTO.getEndDate().plusDays(1).atStartOfDay()));
			}

			// 將所有條件用 AND 連接起來
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		// 建立分頁請求物件，page 從 0 開始，所以要 -1。並設定預設按訂購時間倒序排列。
		Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getPageSize(),
				Sort.by("createdTime").descending());

		// 使用findAll(Specification, Pageable) 進行查詢
		Page<TicketOrder> orderPage = ticketOrderRepo.findAll(spec, pageable);

		// 將查詢結果的 Page<TicketOrder> 轉換為 Page<OrderSummaryDTO>
		return orderPage.map(this::convertToSummaryDTO);
	}

	/**
	 * 查詢單筆訂單的完整詳情
	 * 
	 * @param orderId 訂單的資料庫 ID (已經過解碼)
	 * @return 包含完整巢狀資訊的 DTO
	 */
	public TicketOrderDetailDTO findOrderDetailById(Integer orderId) {
		TicketOrder order = ticketOrderRepo.findById(orderId)
				.orElseThrow(() -> new RuntimeException("訂單不存在，ID: " + orderId));
		return convertToDetailDTO(order);
	}

	/**
	 * 手動將訂單標記為已付款
	 */
	@Transactional
	public UpdateTicketOrderResponseDTO markOrderAsPaid(String orderNumber, ManualPaymentDTO dto) {

		Integer orderId = OrderNumberUtils.decode(orderNumber);

		TicketOrder order = ticketOrderRepo.findById(orderId)
				.orElseThrow(() -> new RuntimeException("訂單不存在，ID: " + orderId));

		if (order.getStatus() != OrderStatus.PENDING) {
			throw new IllegalStateException("只有'待處理'的訂單才能標記為已付款。");
		}

		order.setStatus(OrderStatus.PAID);
		order.setPaymentTime(LocalDateTime.now());
		order.setPaymentType(dto.getPaymentType());
		order.setPaymentTransactionId("MANUAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

		ticketOrderRepo.save(order);

		UpdateTicketOrderResponseDTO response = new UpdateTicketOrderResponseDTO();
		response.setSuccess(true);
		response.setMessage("訂單已成功標記為已付款");

		return response;

	}

	/**
	 * 執行訂單退款
	 */
	@Transactional
	public UpdateTicketOrderResponseDTO refundOrder(String orderNumber) {

		Integer orderId = OrderNumberUtils.decode(orderNumber);

		TicketOrder order = ticketOrderRepo.findById(orderId)
				.orElseThrow(() -> new RuntimeException("訂單不存在，ID: " + orderId));

		if (order.getStatus() != OrderStatus.PAID) {
			throw new IllegalStateException("只有'已付款'的訂單才能辦理退款。");
		}

		// 如果訂單沒有明細 (可能已被取消)，直接將訂單標記為已取消
		if (order.getTicketOrderDetails().isEmpty()) {
			order.setStatus(OrderStatus.CANCELLED);
			ticketOrderRepo.save(order);
			UpdateTicketOrderResponseDTO response = new UpdateTicketOrderResponseDTO();
			response.setSuccess(true);
			response.setMessage("訂單已退款");

			return response;

		}

		// 檢查退款時限
		LocalDateTime startTime = order.getTicketOrderDetails().get(0).getSessionSeat().getMovieSession()
				.getStartTime();
		LocalDateTime refundDeadline = startTime.minusMinutes(10);
		if (LocalDateTime.now().isAfter(refundDeadline)) {
			throw new IllegalStateException("已超過可退款時間 (場次開始前10分鐘)。");
		}

		// 釋出座位
		for (TicketOrderDetail detail : order.getTicketOrderDetails()) {
			// 1. 將訂單明細的狀態更新為「已退款」
			detail.setStatus(OrderDetailStatus.REFUNDED);

			// 2. 釋放對應的座位
			SessionSeat sessionSeat = detail.getSessionSeat();
			if (sessionSeat != null) {
				sessionSeat.setStatus(SeatStatus.AVAILABLE);
				// 我們不再需要手動解除關聯，狀態的改變已經代表了一切
			}
		}

		// 更新訂單狀態
		order.setStatus(OrderStatus.REFUNDED);
		order.setPaymentTime(null);
		order.setPaymentType(null);
		order.setPaymentTransactionId(null);

		ticketOrderRepo.save(order);

		UpdateTicketOrderResponseDTO response = new UpdateTicketOrderResponseDTO();
		response.setSuccess(true);
		response.setMessage("訂單已取消");

		return response;
	}

	/**
	 * 建立臨時預訂。
	 * 
	 * @Transactional 確保方法內所有資料庫操作的原子性。
	 * @param request  前端傳來的預訂請求
	 * @param memberId 從 Token 中解析出的、可信的會員 ID
	 * @return 包含訂單ID和到期時間的回應
	 */
	@Transactional
	public ReservationResponse createReservation(ReservationRequest request, Integer memberId) {
		// 1. 獲取並以悲觀鎖鎖定請求的座位
		List<SessionSeat> lockedSeats = sessionSeatRepo.findAndLockBySessionSeatIdIn(request.getSeatIds());

		// 2. 驗證座位狀態與數量
		if (lockedSeats.size() != request.getSeatIds().size()) {
			throw new RuntimeException("部分座位不存在或已被搶訂");
		}
		for (SessionSeat seat : lockedSeats) {
			if (seat.getStatus() != SeatStatus.AVAILABLE) {
				String seatInfo = seat.getSeat().getRowNumber() + "排" + seat.getSeat().getColumnNumber() + "號";
				throw new RuntimeException("座位 " + seatInfo + " 已被預訂，請重新選擇。");
			}
		}

		// 3. 根據支付方式計算到期時間
		MovieSession session = movieSessionRepo.findById(request.getSessionId())
				.orElseThrow(() -> new RuntimeException("場次不存在"));

		LocalDateTime expiryTime;
		if ("online".equalsIgnoreCase(request.getPaymentMethod())) {
			expiryTime = LocalDateTime.now().plusMinutes(15);
		} else if ("counter".equalsIgnoreCase(request.getPaymentMethod())) {
			expiryTime = session.getStartTime().minusMinutes(30);
		} else {
			throw new IllegalArgumentException("無效的支付方式");
		}

		// 4. 查找會員並建立 TicketOrder
		Member currentMember = memberRepo.findById(memberId).orElseThrow(() -> new RuntimeException("會員不存在"));

		TicketOrder order = new TicketOrder();
		order.setMember(currentMember);
		order.setStatus(OrderStatus.PENDING);

		// 5. 建立訂單明細(TicketOrderDetail)並更新座位(SessionSeat)狀態
		int totalTickets = request.getTickets().stream().mapToInt(req -> req.getQuantity()).sum();
		if (totalTickets != lockedSeats.size()) {
			throw new RuntimeException("票券總數與座位數不符");
		}

		int totalAmount = 0;
		int seatIndex = 0;
		for (TicketRequest ticketReq : request.getTickets()) {
			TicketType ticketType = ticketTypeRepo.findById(ticketReq.getTicketTypeId())
					.orElseThrow(() -> new RuntimeException("票種不存在"));

			for (int i = 0; i < ticketReq.getQuantity(); i++) {
				SessionSeat currentSeat = lockedSeats.get(seatIndex++);

				// 更新座位狀態和到期時間
				currentSeat.setStatus(SeatStatus.RESERVED);
				currentSeat.setReservedExpiredDate(expiryTime);

				// 建立訂單明細
				TicketOrderDetail detail = new TicketOrderDetail();
				detail.setTicketOrder(order);
				detail.setSessionSeat(currentSeat);
				detail.setTicketType(ticketType);
				detail.setUnitPrice(ticketReq.getUnitPrice()); // 這裡可以用後端價格再驗證一次

				detail.setStatus(OrderDetailStatus.ACTIVE);

				order.getTicketOrderDetails().add(detail);
				totalAmount += ticketReq.getUnitPrice();
			}
		}
		order.setTotalAmount(totalAmount);
		order.setTotalTicketAmount(totalAmount);
		order.setTotalDiscount(0);

		// 6. 保存訂單。由於 TicketOrder 中的 `cascade = CascadeType.ALL`，訂單明細會被一併保存。
		// 同時，被修改的 SessionSeat 實體也會在交易提交時一併更新到資料庫。
		TicketOrder savedOrder = ticketOrderRepo.save(order);

		// 7. 返回成功結果
		return new ReservationResponse(savedOrder.getTicketOrderId(), expiryTime);
	}

	/**
	 * 【新增方法】取消一個臨時預訂
	 * 
	 * @param orderId  要取消的訂單 ID
	 * @param memberId 從 Token 中解析出的會員 ID，用於驗證身份
	 */
	@Transactional
	public void cancelTemporaryOrder(Integer orderId, Integer memberId) {
		// 1. 查找訂單，並驗證訂單是否存在且屬於當前使用者
		TicketOrder order = ticketOrderRepo.findById(orderId)
				.orElseThrow(() -> new RuntimeException("訂單不存在，ID: " + orderId));

		// 安全性檢查：確保只有訂單的擁有者才能取消自己的訂單
		if (!order.getMember().getMemberId().equals(memberId)) {
			throw new IllegalStateException("無權限取消此訂單。");
		}

		// 2. 只有處於 PENDING (待處理) 狀態的訂單才能被取消
		if (order.getStatus() != OrderStatus.PENDING) {
			// 如果訂單已經是 PAID, CANCELLED 等其他狀態，則不做任何事或拋出例外
			// 在這個情境下，靜默處理即可，因為流程可能已經被其他方式終止
			System.out.println("訂單 " + orderId + " 狀態為 " + order.getStatus() + "，無需取消。");
			return;
		}

		// 3. 獲取所有需要被釋放的座位 ID
		List<Integer> sessionSeatIdsToRelease = ticketOrderRepo.findSessionSeatIdsByOrderId(orderId);

		// 這能確保 session_seat 與 ticket_order_detail 的一對一關聯被正確解除
		for (TicketOrderDetail detail : order.getTicketOrderDetails()) {
			detail.setTicketOrder(null); // 解除關聯
		}

		// 【新增】清空訂單的明細列表
		order.getTicketOrderDetails().clear();
		ticketOrderRepo.saveAndFlush(order); // 立即將變更寫入資料庫，解除級聯關聯

		if (!sessionSeatIdsToRelease.isEmpty()) {
			// 4. 【核心】使用單一 UPDATE 語句，批次釋放所有座位
			sessionSeatRepo.releaseSeats(sessionSeatIdsToRelease);
		}

		// 5. 更新訂單狀態為 CANCELLED
		order.setStatus(OrderStatus.CANCELLED);

		// 6. 由於整個方法在 @Transactional 管理下，所有變更(order狀態)會在方法結束時自動保存。
		// ticketOrderRepo.save(order); // 這一行是非必要的

		System.out.println("訂單 " + orderId + " 已成功取消，相關座位已釋放。");
	}

	// 一個專門的方法來處理定時任務
	@Scheduled(fixedRate = 60000) // 每分鐘執行一次
	@Transactional
	public void checkAndCancelExpiredReservations() {
		LocalDateTime now = LocalDateTime.now();
		// 1. 查詢所有狀態為 PENDING 的訂單，因為過期時間在明細裡，所以無法在查詢時判斷
		List<TicketOrder> pendingOrders = ticketOrderRepo.findByStatus(OrderStatus.PENDING);

		int cancelledCount = 0;
		for (TicketOrder order : pendingOrders) {
			// 2. 檢查此訂單的第一個座位是否過期
			// 這裡假設一個訂單的所有座位有相同的過期時間，這是合理的設計
			if (!order.getTicketOrderDetails().isEmpty()) {
				TicketOrderDetail firstDetail = order.getTicketOrderDetails().get(0);
				SessionSeat firstSeat = firstDetail.getSessionSeat();
				// 只有當座位確實存在且過期時間已到，才進行取消
				if (firstSeat != null && firstSeat.getReservedExpiredDate() != null
						&& now.isAfter(firstSeat.getReservedExpiredDate())) {
					System.out.println("定時任務: 偵測到過期訂單 ID: " + order.getTicketOrderId() + "，正在處理...");

					// 3. 釋放座位並解除關聯
					for (TicketOrderDetail detail : order.getTicketOrderDetails()) {
						// 1. 將訂單明細的狀態更新為「已取消」
						detail.setStatus(OrderDetailStatus.CANCELLED);

						// 2. 釋放對應的座位
						SessionSeat sessionSeat = detail.getSessionSeat();
						if (sessionSeat != null) {
							sessionSeat.setStatus(SeatStatus.AVAILABLE);
							sessionSeat.setReservedExpiredDate(null);
						}
					}

					// 使用 saveAll 批次更新座位狀態
					sessionSeatRepo.saveAll(
							order.getTicketOrderDetails().stream().map(TicketOrderDetail::getSessionSeat).toList());

					// 4. 更新訂單狀態為 CANCELLED
					order.setStatus(OrderStatus.CANCELLED);
					// 由於設置了 orphanRemoval = true，清空列表會自動刪除 TicketOrderDetail
					order.getTicketOrderDetails().clear();
					ticketOrderRepo.save(order);

					cancelledCount++;
				}
			}
		}

		if (cancelledCount > 0) {
			System.out.println("定時任務: 已成功取消 " + cancelledCount + " 筆過期訂單。");
		}
	}

	/**
	 * 輔助方法：將 TicketOrder Entity 轉換為包含完整明細的 TicketOrderDetailDTO
	 */
	private TicketOrderDetailDTO convertToDetailDTO(TicketOrder order) {
		TicketOrderDetailDTO dto = new TicketOrderDetailDTO();

		// 1. 在回傳給前端前，將資料庫 ID 加密成專業訂單編號
		dto.setOrderNumber(OrderNumberUtils.encode(order.getTicketOrderId()));

		// 2. 映射會員資訊
		MemberInfoForTicketDTO memberDTO = new MemberInfoForTicketDTO();
		if (order.getMember() != null) {
			memberDTO.setMemberId(order.getMember().getMemberId());
			memberDTO.setUsername(order.getMember().getUsername());
			memberDTO.setEmail(order.getMember().getEmail());
			memberDTO.setPhone(order.getMember().getPhone());
		}
		dto.setMember(memberDTO);

		// 3. 映射優惠券資訊
		if (order.getCoupon() != null) {
			CouponInfoForTicketDTO couponDTO = new CouponInfoForTicketDTO();
			couponDTO.setCouponId(order.getCoupon().getCouponId());
			couponDTO.setCouponName(order.getCoupon().getCouponName());
			dto.setCoupon(couponDTO);
		}

		// 4. 映射訂單主體資訊
		dto.setTotalAmount(order.getTotalAmount());
		dto.setTotalTicketAmount(order.getTotalTicketAmount());
		dto.setTotalDiscount(order.getTotalDiscount());
		dto.setStatus(order.getStatus().name());
		dto.setPaymentType(order.getPaymentType());
		dto.setCreatedTime(order.getCreatedTime());
		dto.setPaymentTime(order.getPaymentTime());

		// 5. 映射訂單明細列表
		List<TicketItemDTO> ticketItems = new ArrayList<>();
		for (TicketOrderDetail detail : order.getTicketOrderDetails()) {
			TicketItemDTO itemDTO = new TicketItemDTO();
			SessionSeat ss = detail.getSessionSeat();

			itemDTO.setMovieTitle(ss.getMovieSession().getMovie().getTitleLocal());
			itemDTO.setStartTime(ss.getMovieSession().getStartTime());
			itemDTO.setTheaterName(ss.getMovieSession().getTheater().getTheaterName());
			itemDTO.setSeatPosition(ss.getSeat().getRowNumber() + ss.getSeat().getColumnNumber());
			itemDTO.setTicketTypeName(detail.getTicketType().getTicketTypeName());
			itemDTO.setUnitPrice(detail.getUnitPrice());

			ticketItems.add(itemDTO);
		}
		dto.setTicketOrderDetails(ticketItems);

		return dto;
	}

	/**
	 * 輔助方法：將 TicketOrder Entity 轉換為 TicketOrderSummaryDTO
	 */
	private TicketOrderSummaryDTO convertToSummaryDTO(TicketOrder order) {
		MemberInfoForTicketDTO memberDTO = new MemberInfoForTicketDTO(order.getMember().getMemberId(),
				order.getMember().getUsername(), order.getMember().getEmail(), order.getMember().getPhone());

		// 加密訂單 ID
		// 在轉換 DTO 時，呼叫 encode 方法將 Integer ID 轉換為訂單編號
		String orderNumber = OrderNumberUtils.encode(order.getTicketOrderId());

		return new TicketOrderSummaryDTO(orderNumber, memberDTO, order.getCreatedTime(), order.getTotalAmount(),
				order.getStatus().name(), order.getPaymentType());
	}

	/**
	 * 【核心業務邏輯】根據臨時訂單 ID 更新訂單狀態並計算最終金額
	 *
	 * @param ticketOrderId    臨時訂單的資料庫整數 ID (已由 Controller 解碼)
	 * @param memberCouponId   【修改】使用的「會員優惠券」ID (member_coupon_id)，可能為 null
	 * @param memberId         來自 JWT 的會員 ID
	 * @return 包含最終金額和商品名稱的回應
	 */
	@Transactional // 【重要】確保整個方法在一個事務中執行，若有任何步驟失敗則全部回滾
	public UpdateOrderResponse updateOrderAndPrepareForPayment(Integer ticketOrderId, Integer memberCouponId, Integer memberId) {
		// 1. 根據資料庫整數 ID 查詢訂單，並檢查會員與訂單是否匹配
		TicketOrder order = ticketOrderRepo.findById(ticketOrderId)
				.orElseThrow(() -> new RuntimeException("找不到對應的臨時訂單"));

		// 驗證訂單是否屬於當前會員
		if (!order.getMember().getMemberId().equals(memberId)) {
			throw new IllegalStateException("無權限操作此訂單。");
		}

		// 2. 檢查訂單狀態，確保是「臨時預訂」狀態，防止重複付款
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new IllegalStateException("訂單狀態無效，無法進行付款。");
		}
		
		// 3. 計算並更新訂單總金額與折扣
		BigDecimal finalAmount = new BigDecimal(order.getTotalTicketAmount()); // 從總票價開始計算，使用 BigDecimal 以保證精度
		BigDecimal totalDiscount = BigDecimal.ZERO;

		// 【核心修改】處理優惠券邏輯
		if (memberCouponId != null) {
			// A. 直接透過 memberCouponId 查找 MemberCoupon 實體
			MemberCoupon memberCoupon = memberCouponRepo.findById(memberCouponId)
					.orElseThrow(() -> new IllegalStateException("您使用的優惠券不存在或已被使用。"));
			
			// B. 【重要安全性驗證】確認這張券是否屬於當前使用者
			if (!memberCoupon.getMember().getMemberId().equals(memberId)) {
				throw new IllegalStateException("您無權使用此優惠券。");
			}

			// C. 【重要狀態驗證】確認這張券是否為「未使用」狀態
			if (!"未使用".equals(memberCoupon.getStatus())) {
				throw new IllegalStateException("此優惠券已被使用。");
			}
			
			Coupon coupon = memberCoupon.getCoupon();

			// D. 驗證優惠券是否符合使用條件 (最低消費)
			if (finalAmount.compareTo(BigDecimal.valueOf(coupon.getMinimumSpend())) < 0) {
				throw new IllegalStateException("此訂單金額不符優惠券使用條件。");
			}

			// E. 【同步計算邏輯】複製 findApplicableCoupons 中的動態折扣計算邏輯
			DiscountType discountType = coupon.getDiscountType();
			switch (discountType) {
				case FIXED:
					totalDiscount = BigDecimal.valueOf(coupon.getDiscountAmount());
					break;
				case PERCENTAGE:
					BigDecimal discountPercent = new BigDecimal("100").subtract(BigDecimal.valueOf(coupon.getDiscountAmount()));
					totalDiscount = finalAmount.multiply(discountPercent.divide(new BigDecimal("100")))
											 .setScale(0, RoundingMode.HALF_UP); // 四捨五入到整數
					break;
				default:
					// 正常情況下不應發生，但作為防禦性程式設計
					throw new IllegalStateException("未知的折扣類型: " + discountType);
			}
			
			finalAmount = finalAmount.subtract(totalDiscount);
			if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
				finalAmount = BigDecimal.ZERO;
			}

			// F. 更新 MemberCoupon 狀態為「已使用」，並記錄使用時間
			memberCoupon.setStatus("已使用");
			memberCoupon.setUsageTime(LocalDateTime.now());
			// 因為此方法在 @Transactional 中，JPA 的髒檢查機制會自動保存變更，
			// 但明確呼叫 save() 可讓意圖更清晰
			memberCouponRepo.save(memberCoupon);

			// G. 將優惠券實體設定到訂單上
			order.setCoupon(coupon);
		}
		
		// 4. 更新訂單實體的屬性
		order.setTotalDiscount(totalDiscount.intValue()); // 轉回 integer 存入
		order.setTotalAmount(finalAmount.intValue());     // 轉回 integer 存入
		order.setStatus(OrderStatus.PENDING); // 狀態仍為 PENDING，直到付款成功
		order.setPaymentType("ECPay_Credit");

		// 5. 將更新後的訂單儲存回資料庫 (同樣，在 @Transactional 中可省略，但保留更清晰)
		ticketOrderRepo.save(order);

		// 6. 準備回應 DTO
		UpdateOrderResponse response = new UpdateOrderResponse();
		response.setOrderId(OrderNumberUtils.encode(order.getTicketOrderId()).replaceAll("-", ""));
		response.setFinalAmount(finalAmount.intValue()); // 回傳 integer
		
		String itemName = order.getTicketOrderDetails().stream()
                .map(TicketOrderDetail::getTicketType)
                .collect(Collectors.groupingBy(TicketType::getTicketTypeName, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> String.format("%s x %d", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("#"));

		if (itemName.isEmpty()) {
			itemName = "Flux電影院 - 電影票";
		}
		response.setItemName(itemName);

		return response;
	}

	/**
	 * 處理綠界回傳的付款成功
	 * 
	 * @param orderNumber   加密訂單編號
	 * @param transactionId 綠界回傳的交易編號
	 */
	@Transactional
	public void handlePaymentSuccess(String orderNumber, String transactionId) {
		System.out.println("付款成功 " + orderNumber + " 交易編號： " + transactionId);
		// "-"位於第9個字元
		String formattedOrderNumber = orderNumber.substring(0, 8) + "-" + orderNumber.substring(8);

		Integer orderId = OrderNumberUtils.decode(formattedOrderNumber);
		TicketOrder order = ticketOrderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("訂單不存在"));

		// 避免重複處理
		if (order.getStatus() != OrderStatus.PAID) {
			order.setStatus(OrderStatus.PAID);
			order.setPaymentTime(LocalDateTime.now());
			order.setPaymentTransactionId(transactionId);
			ticketOrderRepo.save(order);
		}
	}

	/**
	 * 處理綠界回傳的付款失敗或取消
	 * 
	 * @param orderNumber 加密訂單編號
	 */
	@Transactional
	public void handlePaymentFailure(String orderNumber) {
		Integer orderId = OrderNumberUtils.decode(orderNumber);
		TicketOrder order = ticketOrderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("訂單不存在"));

		// 只有當訂單處於「待付款」狀態時才需要處理失敗邏輯
		// 避免重複處理或處理已付款的訂單
		if (order.getStatus() == OrderStatus.PENDING) {
			// 1. 【新增】歸還已使用的優惠券
			if (order.getCoupon() != null) {
				// 找到與訂單關聯的 MemberCoupon
				Optional<MemberCoupon> memberCouponOpt = memberCouponRepo
						.findFirstByMemberAndCouponAndStatus(order.getMember(), order.getCoupon(), "已使用");

				if (memberCouponOpt.isPresent()) {
					MemberCoupon memberCoupon = memberCouponOpt.get();
					memberCoupon.setStatus("未使用"); // 將狀態設回「未使用」
					memberCoupon.setUsageTime(null); // 清空使用時間
					memberCouponRepo.save(memberCoupon);
					System.out.println(
							"訂單 " + orderNumber + " 付款失敗，優惠券 " + memberCoupon.getCoupon().getCouponName() + " 已歸還。");
				}
			}

			// 2. 將訂單狀態更新為取消
			order.setStatus(OrderStatus.CANCELLED);
			ticketOrderRepo.save(order);
		}
	}

	/**
	 * 根據會員ID查詢其所有訂票紀錄
	 * 
	 * @param memberId
	 * @return
	 */
	public List<TicketOrderHistoryDTO> getTicketOrderHistoryByMemberId(Integer memberId) {
		return ticketOrderRepo.findTicketOrderHistoryByMemberId(memberId);
	}

	/**
	 * 取得符合條件的優惠券供前端顯示
	 * @param memberId 會員ID
	 * @param sessionId 場次ID
	 * @param subtotal 消費金額
	 * @return 回復優惠券列表包含可扣除金額
	 */
	public List<TicketOrderApplicableCouponDTO> findApplicableCoupons(Integer memberId, Integer sessionId,
			BigDecimal subtotal) {

		// 1. 獲取上下文資訊
		// 1.1 獲取當前會員實體
		Member currentMember = memberRepo.findById(memberId)
				.orElseThrow(() -> new RuntimeException("找不到會員，ID: " + memberId));

		// 1.2 獲取電影場次及電影資訊
		MovieSession session = movieSessionRepo.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("找不到場次，ID: " + sessionId));
		Integer movieId = session.getMovie().getId();

		Integer currentMemberLevelId = memberLevelRecordRepo.findTopByMemberIdOrderByStartDateDesc(memberId)
				.map(MemberLevelRecord::getMemberLevelId) // 直接從 record 物件獲取 memberLevelId
				.orElse(1); // 如果找不到任何紀錄，預設等級為 1

		// 2. 撈出會員所有「未使用」的優惠券
		List<MemberCoupon> unusedCoupons = memberCouponRepo.findByMemberAndStatus(currentMember, "未使用");

		LocalDate today = LocalDate.now();

		// 3. 在記憶體中進行過濾
		return unusedCoupons.stream()
				// 階段一：過濾掉上下文不符的券（電影、場次、會員等級...）
				.filter(memberCoupon -> isCouponContextuallyValid(memberCoupon.getCoupon(), movieId, sessionId,
						currentMemberLevelId, today))
				// 階段二：將剩下的券轉換為 DTO，並在其中計算「可用性」與「實際折扣金額」
				.map(memberCoupon -> buildApplicableDto(memberCoupon, subtotal)).collect(Collectors.toList());
	}

	/**
	 * 將 isCouponApplicable 更名為 isCouponContextuallyValid，職責更清晰。
	 * 此方法只檢查優惠券的適用「情境」（電影、會員等級等），不關心「金額」。
	 */
	private boolean isCouponContextuallyValid(Coupon coupon, Integer movieId, Integer sessionId,
			Integer currentMemberLevelId, LocalDate today) {

		// --- 檢查點 1: 活動時效性 ---【先不判斷活動區間，未來可在此處啟用】
//    	Event event = coupon.getEvent();
//    	if (event != null) {
//    		// 2.1 活動是否已開始
//    		if (event.getStartDate() != null && today.isBefore(event.getStartDate())) {
//    			return false;
//    		}
//    		// 2.2 活動是否已結束
//    		if (event.getEndDate() != null && today.isAfter(event.getEndDate())) {
//    			return false;
//    		}
//    	}

		// --- 檢查點 2: 適用資格 (EventEligibility) ---
		EventEligibility eligibility = coupon.getEventEligibility();
		if (eligibility == null) {
			return true; // 通用券，上下文永遠有效
		}
		if (eligibility.getMovie() != null && !Objects.equals(eligibility.getMovie().getId(), movieId)) {
			return false;
		}
		if (eligibility.getSession() != null && !Objects.equals(eligibility.getSession().getSessionId(), sessionId)) {
			return false;
		}
		if (eligibility.getMemberLevel() != null) {
			Integer requiredLevelId = eligibility.getMemberLevel().getMemberLevelId();
			if (currentMemberLevelId < requiredLevelId) {
				return false;
			}
		}
		return true; // 所有上下文檢查都通過
	}

	/**
	 * 負責建立 DTO，並在其中完成「低消判斷」與「折扣金額計算」。
	 */
	private TicketOrderApplicableCouponDTO buildApplicableDto(MemberCoupon memberCoupon, BigDecimal subtotal) {
		Coupon coupon = memberCoupon.getCoupon();

		// 1. 判斷是否滿足低消 (isUsable)
		boolean isUsable = subtotal.compareTo(BigDecimal.valueOf(coupon.getMinimumSpend())) >= 0;

		// 2. 根據 discountType 計算實際折扣金額
		BigDecimal calculatedDiscountAmount;

		// 如果優惠券當前不可用（未滿足低消），則無論是什麼類型，折扣金額都應為 0。
		if (!isUsable) {
			calculatedDiscountAmount = BigDecimal.ZERO;
		} else {
			// 如果優惠券可用，再根據其類型進行計算
			DiscountType discountType = coupon.getDiscountType();
			switch (discountType) {
			case FIXED:
				// 定額：在滿足低消的前提下，直接使用 discountAmount 的值
				calculatedDiscountAmount = BigDecimal.valueOf(coupon.getDiscountAmount());
				break;

			case PERCENTAGE:
				// 比例：在滿足低消的前提下，計算 subtotal * (discountAmount / 100)
				BigDecimal discountPercent = new BigDecimal("100").subtract(BigDecimal.valueOf(coupon.getDiscountAmount()));
				calculatedDiscountAmount = subtotal.multiply(discountPercent.divide(new BigDecimal("100"))).setScale(0,
						RoundingMode.HALF_UP); // 四捨五入到整數
				break;

			default:
				throw new IllegalStateException("未知的折扣類型: " + discountType);
			}
		}

		// 3. 建立並回傳最終的 DTO
		return new TicketOrderApplicableCouponDTO(memberCoupon.getMemberCouponId(), coupon.getCouponName(),
				coupon.getCouponDescription(), calculatedDiscountAmount, // 使用計算出的最終折扣額
				coupon.getMinimumSpend(), isUsable);
	}
	
	 /**
     * 最終確認一筆「臨櫃付款」的訂單。
     * 此方法將臨時預訂轉為正式預訂，並套用優惠券。
     *
     * @param ticketOrderId    臨時訂單的資料庫整數 ID (已由 Controller 解碼)
     * @param memberCouponId   會員使用的優惠券 ID (member_coupon_id)，可能為 null
     * @param memberId         來自 JWT 的會員 ID，用於驗證
     */
    @Transactional 
    public void finalizeCounterReservation(Integer ticketOrderId, Integer memberCouponId, Integer memberId) {
        // 1. 根據 ID 查詢訂單，並驗證訂單是否屬於當前會員
        TicketOrder order = ticketOrderRepo.findById(ticketOrderId)
                .orElseThrow(() -> new RuntimeException("找不到對應的臨時訂單"));

        if (!order.getMember().getMemberId().equals(memberId)) {
            throw new IllegalStateException("無權限操作此訂單。");
        }

        // 2. 檢查訂單狀態，確保是「臨時預訂」狀態 (PENDING)，防止重複確認
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("訂單狀態已變更，無法重複確認。");
        }
        
        // 3. 計算並更新訂單總金額與折扣 (這段邏輯與 updateOrderAndPrepareForPayment 完全相同)
        BigDecimal finalAmount = new BigDecimal(order.getTotalTicketAmount());
        BigDecimal totalDiscount = BigDecimal.ZERO;

        // 如果使用者選擇了優惠券
        if (memberCouponId != null) {
            MemberCoupon memberCoupon = memberCouponRepo.findById(memberCouponId)
                    .orElseThrow(() -> new IllegalStateException("您使用的優惠券不存在或已被使用。"));
            
            if (!memberCoupon.getMember().getMemberId().equals(memberId)) {
                throw new IllegalStateException("您無權使用此優惠券。");
            }

            if (!"未使用".equals(memberCoupon.getStatus())) {
                throw new IllegalStateException("此優惠券已被使用。");
            }
            
            Coupon coupon = memberCoupon.getCoupon();

            if (finalAmount.compareTo(BigDecimal.valueOf(coupon.getMinimumSpend())) < 0) {
                throw new IllegalStateException("此訂單金額不符優惠券使用條件。");
            }

            DiscountType discountType = coupon.getDiscountType();
            switch (discountType) {
                case FIXED:
                    totalDiscount = BigDecimal.valueOf(coupon.getDiscountAmount());
                    break;
                case PERCENTAGE:
                	BigDecimal discountPercent = new BigDecimal("100").subtract(BigDecimal.valueOf(coupon.getDiscountAmount()));
                    totalDiscount = finalAmount.multiply(discountPercent.divide(new BigDecimal("100")))
                                             .setScale(0, RoundingMode.HALF_UP);
                    break;
                default:
                    throw new IllegalStateException("未知的折扣類型: " + discountType);
            }
            
            finalAmount = finalAmount.subtract(totalDiscount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            // 將優惠券標記為「已使用」
            memberCoupon.setStatus("已使用");
            memberCoupon.setUsageTime(LocalDateTime.now());
            memberCouponRepo.save(memberCoupon);

            // 訂單關聯此優惠券
            order.setCoupon(coupon);
        }
        
        // 4. 【核心差異】更新訂單實體的最終狀態
        order.setTotalDiscount(totalDiscount.intValue());
        order.setTotalAmount(finalAmount.intValue());
        
        // 將付款方式明確標記為「臨櫃付款」
        order.setPaymentType("Counter"); 
        
        // 狀態依然是 PENDING (待處理/待付款)，後台管理員會手動將其變更為 PAID
        order.setStatus(OrderStatus.PENDING); 

        // 5. 保存更新後的訂單
        ticketOrderRepo.save(order);

        System.out.println("訂單 ID: " + ticketOrderId + " 已成功確認為臨櫃付款預訂。");
    }

}
