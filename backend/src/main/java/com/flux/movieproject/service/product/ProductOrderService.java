package com.flux.movieproject.service.product;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.flux.movieproject.enums.CouponStatus;
import com.flux.movieproject.model.dto.member.MemberCouponListDTO;
import com.flux.movieproject.model.dto.member.MemberCouponListDTOForProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.dto.product.order.CreateOrderDTO;
import com.flux.movieproject.model.dto.product.order.CreateOrderResponseDTO;
import com.flux.movieproject.model.dto.product.order.ProductInfoDTO;
import com.flux.movieproject.model.dto.product.order.ProductOrderDTO;
import com.flux.movieproject.model.dto.product.order.ProductOrderDetailDTO;
import com.flux.movieproject.model.dto.product.order.ProductOrderMemberDTO;
import com.flux.movieproject.model.dto.product.product.ProductDTO;
import com.flux.movieproject.model.entity.event.Coupon;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.model.entity.member.MemberCoupon;
import com.flux.movieproject.model.entity.product.OrderStatus;
import com.flux.movieproject.model.entity.product.PaymentMethod;
import com.flux.movieproject.model.entity.product.Product;
import com.flux.movieproject.model.entity.product.ProductOrder;
import com.flux.movieproject.model.entity.product.ProductOrderDetail;
import com.flux.movieproject.repository.event.CouponRepository;
import com.flux.movieproject.repository.member.MemberCouponRepository;
import com.flux.movieproject.repository.member.MemberRepository;
import com.flux.movieproject.repository.product.ProductOrderRepository;
import com.flux.movieproject.repository.product.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ProductOrderService {

	private final ProductOrderRepository productOrderRepository;
	private final MemberCouponRepository memberCouponRepository;
	private final MemberRepository memberRepository;
	private final ProductRepository productRepository;
	private final CouponRepository couponRepository;

	@Autowired
	private HttpServletRequest request;
	@Autowired
	private NotificationService notificationService;

	public record UpdateProductResponseDTO(ProductDTO product, String message) {
	}

	public record UpdateOrderStatusDTO(Integer orderId, OrderStatus newStatus) {
	}

	@Autowired
	public ProductOrderService(ProductOrderRepository productOrderRepository,
			MemberCouponRepository memberCouponRepository, MemberRepository memberRepository,
			ProductRepository productRepository, CouponRepository couponRepository) {
		this.productOrderRepository = productOrderRepository;
		this.memberCouponRepository = memberCouponRepository;
		this.memberRepository = memberRepository;
		this.productRepository = productRepository;
		this.couponRepository = couponRepository;
	}

	@Transactional
	public CreateOrderResponseDTO createOrder(CreateOrderDTO createOrderDTO) {

		// Todo 樂觀鎖or悲觀鎖

		ProductOrder newOrder = new ProductOrder();

		Member member = memberRepository.findById(createOrderDTO.memberId())
				.orElseThrow(() -> new IllegalArgumentException("此ID沒有會員" + createOrderDTO.memberId()));

		Integer orderAmount = calculateOrderAmount(createOrderDTO);

		List<ProductOrderDetail> orderDetails = createOrderDTO.orderDetails().stream().map(detailDTO -> {
			Product product = productRepository.findById(detailDTO.productId())
					.orElseThrow(() -> new IllegalArgumentException("無效的商品ID: " + detailDTO.productId()));

			int subtotal = product.getPrice() * detailDTO.quantity() + detailDTO.extraPrice();

			ProductOrderDetail detail = new ProductOrderDetail();
			detail.setProduct(product);
			if (detailDTO.quantity() > product.getStock()) {
				throw new IllegalArgumentException(
						"商品 ：" + product.getProductName() + " 庫存不足。 " + "，但目前只有 " + product.getStock() + "。");
			}
			detail.setQuantity(detailDTO.quantity());
			detail.setExtraPrice(detailDTO.extraPrice());

			detail.setUnitPrice(product.getPrice());
			detail.setSubtotal(subtotal);
			detail.setProductName(product.getProductName());

			detail.setProductOrder(newOrder);
			return detail;
		}).collect(Collectors.toList());

		for (ProductOrderDetail orderProducts : orderDetails) {
			Integer orderCount = orderProducts.getQuantity();
			Integer currentStock = orderProducts.getProduct().getStock();
			Integer newStock = currentStock - orderCount;
			Product p = orderProducts.getProduct();
			p.setStock(newStock);
		}
		int discountAmount = 0;
		int finalPaymentAmount = orderAmount;
		if (createOrderDTO.couponId() != null) {
			Coupon coupon = couponRepository.findByCouponId((createOrderDTO.couponId()));
			if (orderAmount >= coupon.getMinimumSpend()) {

				switch (coupon.getDiscountType()) {
				case FIXED -> {
					finalPaymentAmount -= coupon.getDiscountAmount();
					discountAmount = coupon.getDiscountAmount();
				}
				case PERCENTAGE -> {
					double percent = coupon.getDiscountAmount() / 100.0;
					discountAmount = (int) Math.round(orderAmount * (1-percent));
					finalPaymentAmount -= discountAmount;
				}
				}
				newOrder.setCouponId(createOrderDTO.couponId());
				List<MemberCoupon> memberCoupon = memberCouponRepository.findByMemberIdAndCouponId(member.getMemberId(),coupon.getCouponId());
				MemberCoupon couponToUse = memberCoupon.stream()
						.filter(c -> "未使用".equals(c.getStatus()))
						.findFirst()
						.orElseThrow(() -> new RuntimeException("沒有可用的優惠券"));

				couponToUse.setStatus("已使用");
				couponToUse.setUsageTime(LocalDateTime.now());
				memberCouponRepository.save(couponToUse);
			}

		}

		PaymentMethod paymentMethod = createOrderDTO.paymentMethod();

		newOrder.setMember(member);

		newOrder.setPaymentMethod(paymentMethod);
		newOrder.setOrderTime(Instant.now());
		if (paymentMethod == PaymentMethod.CASH) {
			newOrder.setOrderStatus(OrderStatus.PENDING);
		} else {
			newOrder.setOrderStatus(OrderStatus.PROCESSING);
		}
		newOrder.setOrderAmount(orderAmount);

		newOrder.setDiscountAmount(discountAmount);
		newOrder.setFinalPaymentAmount(finalPaymentAmount);
		newOrder.setCustomerEmail(createOrderDTO.email());

		newOrder.setProductOrderDetails(orderDetails);
		productOrderRepository.saveAndFlush(newOrder);
		String orderNumber = String.format("ORD-%s-%04d", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
				newOrder.getOrderId());
		newOrder.setOrderNumber(orderNumber);
		productOrderRepository.save(newOrder);

		return new CreateOrderResponseDTO(newOrder.getOrderId(), newOrder.getOrderNumber(), newOrder.getOrderTime(),
				"訂單創建成功！");
	}

	private Integer calculateOrderAmount(CreateOrderDTO createOrderDTO) {
		return createOrderDTO.orderDetails().stream().mapToInt(detailDTO -> {
			Product product = productRepository.findById(detailDTO.productId())
					.orElseThrow(() -> new IllegalArgumentException("無效的商品ID: " + detailDTO.productId()));

			return product.getPrice() * detailDTO.quantity() + detailDTO.extraPrice();
		}).sum();
	}

	public ProductOrderDTO findByOrderId(Integer orderId) {
		ProductOrder order = productOrderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("此id沒有商品: " + orderId));
		return convertToDto(order);
	}

	public List<ProductOrderDTO> findAllOrders() {
		List<ProductOrder> orders = productOrderRepository.findAll();
		return orders.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	public List<ProductOrderDTO> findAllOrdersByMemberId(Integer memberId) {
		List<ProductOrder> orders = productOrderRepository.findAllByMemberMemberId(memberId);
		return orders.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	public List<ProductOrderDTO> findAllOrdersByStatus(OrderStatus orderStatus) {
		List<ProductOrder> orders = productOrderRepository.findAllByOrderStatus(orderStatus);
		return orders.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	public List<ProductOrderDTO> findOrderByMemberAndStatus(Integer memberId, OrderStatus status) {
		List<ProductOrder> orders = productOrderRepository.findByMemberMemberIdAndOrderStatus(memberId, status);
		return orders.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	@Transactional
	public ProductOrderDTO updateOrderStatus(Integer orderId, UpdateOrderStatusDTO statusDto) {
		ProductOrder order = productOrderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("找不到訂單 ID: " + orderId));

		// TODO 部分邏輯未完成
		switch (statusDto.newStatus()) {
		case CANCELLED -> {
			if (order.getOrderStatus() != OrderStatus.PREPARED) {
				throw new IllegalStateException("訂單狀態為 " + order.getOrderStatus() + "，無法取消。只有準備中的訂單才能取消。");
			}
			// 回補庫存數量
			List<ProductOrderDetail> details = order.getProductOrderDetails();
			if (details != null && !details.isEmpty()) {
				for (ProductOrderDetail detail : details) {
					Product product = detail.getProduct();
					int currentStock = product.getStock();
					product.setStock(currentStock + detail.getQuantity());
				}
			}
			order.setOrderStatus(OrderStatus.CANCELLED);
		}
		case PENDING -> {
			throw new IllegalStateException("不允許將訂單狀態改回待處理。");
		}

		case COMPLETED -> {

			order.setOrderStatus(OrderStatus.COMPLETED);
		}

		case PROCESSING -> {
		}

		}

		// 儲存變更並回傳更新後的完整訂單 DTO
		ProductOrder updatedOrder = productOrderRepository.save(order);
		return convertToDto(updatedOrder);
	}

	@Transactional
	public ProductOrderDTO updateOrderForAdmin(Integer orderId, UpdateOrderStatusDTO statusDto) {
		ProductOrder order = productOrderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("找不到訂單 ID: " + orderId));

		// 取得並驗證新狀態
		OrderStatus newStatus = statusDto.newStatus();
		if (newStatus == null) {
			throw new IllegalStateException("unknow");
		}

		// 如果是取消訂單，要恢復庫存
		if (newStatus == OrderStatus.CANCELLED) { // 使用 newStatus 而不是 statusDto.newStatus
			List<ProductOrderDetail> details = order.getProductOrderDetails();
			if (details != null && !details.isEmpty()) {
				for (ProductOrderDetail detail : details) {
					Product product = detail.getProduct();
					int currentStock = product.getStock();
					product.setStock(currentStock + detail.getQuantity());
				}
			}
		}

		order.setOrderStatus(newStatus);
		ProductOrder updatedOrder = productOrderRepository.save(order);

		switch (statusDto.newStatus()) {
		case CANCELLED -> notificationService.sendEmailAsync(order.getCustomerEmail(), "訂單狀態更新",
				"訂單 " + order.getOrderNumber() + " 已取消。");
		case PENDING -> notificationService.sendEmailAsync(order.getCustomerEmail(), "訂單狀態更新",
				"訂單 " + order.getOrderNumber() + " 已建立，等待付款。");
		case PREPARED -> notificationService.sendEmailAsync(order.getCustomerEmail(), "訂單狀態更新",
				"訂單 " + order.getOrderNumber() + " 已進入準備階段，商品即將出貨。");
		case PROCESSING -> notificationService.sendEmailAsync(order.getCustomerEmail(), "訂單狀態更新",
				"訂單 " + order.getOrderNumber() + " 正在處理中，請耐心等待。");
		case COMPLETED -> notificationService.sendEmailAsync(order.getCustomerEmail(), "訂單狀態更新",
				"訂單 " + order.getOrderNumber() + " 已完成，感謝您的購買！");
		default -> {
		}
		}

		return convertToDto(updatedOrder);
	}

	// Todo productOrderDetailRepository用途?實
	// TODO 修改訂單方法
	private ProductOrderDTO convertToDto(ProductOrder order) {
		if (order == null) {
			return null;
		}

		List<ProductOrderDetailDTO> detailDTOs = order.getProductOrderDetails().stream().map(detail -> {

			Product productEntity = detail.getProduct();

			ProductInfoDTO productInfoDTO = null;
			if (productEntity != null) {
				productInfoDTO = new ProductInfoDTO(productEntity.getProductId(), productEntity.getProductName(),
						productEntity.getCategory().getCategoryId(),productEntity.getImageUrl());
			}

			// 3. 使用構建好的 productInfoDTO 來創建 ProductOrderDetailDTO
			return new ProductOrderDetailDTO(detail.getDetailId(), productInfoDTO, detail.getQuantity(),
					detail.getUnitPrice(), detail.getSubtotal(), detail.getExtraPrice(), detail.getProductName());
		}).collect(Collectors.toList());

		String orderStatusName = order.getOrderStatus() != null ? order.getOrderStatus().name() : "PENDING";

		return new ProductOrderDTO(order.getOrderId(), order.getOrderTime(), order.getOrderAmount(),
				order.getDiscountAmount(), order.getFinalPaymentAmount(), order.getCouponId(), order.getCustomerEmail(),
				order.getPaymentMethod().name(), orderStatusName, order.getOrderNumber(),
				new ProductOrderMemberDTO(order.getMember().getMemberId(), order.getMember().getUsername()),
				detailDTOs);
	}

	@Transactional
	public void deleteOrder(Integer orderId) {

		ProductOrder order = productOrderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("找不到訂單 ID: " + orderId));

		// 已經取消的訂單才能被刪除
		if (order.getOrderStatus() != OrderStatus.CANCELLED) {
			throw new IllegalStateException("訂單狀態為 " + order.getOrderStatus() + "，無法刪除。請先取消訂單。");
		}

		productOrderRepository.delete(order);
	}

	@Transactional
	public ProductOrderDTO cancelMemberOrder(Integer orderId, Integer memberId) {
		ProductOrder order = productOrderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("找不到訂單 ID: " + orderId));

		// 驗證訂單是否屬於目前會員
		if (!order.getMember().getMemberId().equals(memberId)) {
			throw new SecurityException("您沒有權限取消此訂單");
		}

		// 檢查是否可取消
		if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.PROCESSING
				&& order.getOrderStatus() != OrderStatus.PREPARED) {
			throw new IllegalStateException("訂單狀態為 " + order.getOrderStatus().name() + "，無法取消。");
		}

		// 取消訂單或退貨時，要把商品數量加回庫存
		List<ProductOrderDetail> details = order.getProductOrderDetails();
		if (details != null && !details.isEmpty()) {
			for (ProductOrderDetail detail : details) {
				Product product = detail.getProduct();
				int currentStock = product.getStock();
				product.setStock(currentStock + detail.getQuantity());
			}
		}

		// 更新訂單狀態
		order.setOrderStatus(OrderStatus.CANCELLED);
		ProductOrder updatedOrder = productOrderRepository.save(order);

		// 發送通知（異步 + 容錯）
		notificationService.sendEmailAsync(order.getCustomerEmail(), "訂單已取消通知",
				"您的訂單 " + order.getOrderNumber() + " 已成功取消。");

		return convertToDto(updatedOrder);
	}

	// 拿到當前會員所有未使用的優惠券
	public List<MemberCouponListDTOForProduct> unusedCoupons() {

		Integer memberId = getCurrentUserId(request);
		System.out.println("目前會員 ID: " + memberId);

		Member member = memberRepository.findByMemberId(memberId);
		if (member == null) {
			System.out.println("找不到會員，ID: " + memberId);
			return Collections.emptyList();
		}

		List<MemberCoupon> memberCoupons = memberCouponRepository.findByMemberAndStatusWithCoupon(member);
		System.out.println("抓到的 MemberCoupon 數量: " + memberCoupons.size());

		System.out.println("==== 驗證 Coupon ====");
		memberCoupons.forEach(mc -> {
			System.out.println("MemberCoupon ID: " + mc.getMemberCouponId()
					+ ", Coupon: " + (mc.getCoupon() == null ? "null" : mc.getCoupon().getCouponName()));
		});


		// 轉 DTO
		return memberCoupons.stream().map(mc -> {
			System.out.println("MemberCoupon ID: " + mc.getMemberCouponId());

			if (mc.getCoupon() == null) {
				System.out.println("此 MemberCoupon 沒有對應的優惠券，MemberCoupon ID: " + mc.getMemberCouponId());
			} else {
				System.out.println("優惠券 ID: " + mc.getCoupon().getCouponId());
				System.out.println("優惠券名稱: " + mc.getCoupon().getCouponName());
				System.out.println("最低消費金額: " + mc.getCoupon().getMinimumSpend());
			}

			return MemberCouponListDTOForProduct.builder()
					.memberCouponId(mc.getMemberCouponId())
					.memberId(mc.getMember().getMemberId())
					.couponId(mc.getCoupon() != null ? mc.getCoupon().getCouponId() : null)
					.status(mc.getStatus())
					.acquisitionTime(mc.getAcquisitionTime())
					.usageTime(mc.getUsageTime())
					.serialNumber(mc.getCoupon() != null ? mc.getCoupon().getSerialNumber() : null)
					.couponName(mc.getCoupon() != null ? mc.getCoupon().getCouponName() : null)
					.couponDescription(mc.getCoupon() != null ? mc.getCoupon().getCouponDescription() : null)
					.couponCategory(mc.getCoupon() != null && mc.getCoupon().getCouponCategory() != null
							? mc.getCoupon().getCouponCategory().getCouponCategoryName() : null)
					.discountAmount(mc.getCoupon() != null ? mc.getCoupon().getDiscountAmount() : null)
					.minimumSpend(mc.getCoupon() != null ? mc.getCoupon().getMinimumSpend() : null)
					.couponImageBase64(mc.getCoupon() != null && mc.getCoupon().getCouponImage() != null
							? Base64.getEncoder().encodeToString(mc.getCoupon().getCouponImage()) : null)
					.displaySerial(mc.getCoupon() != null ? mc.getCoupon().getSerialNumber() : null)
					.discountType(mc.getCoupon() != null ? mc.getCoupon().getDiscountType() : null)
					.build();
		}).toList();
	}



	private Integer getCurrentUserId(HttpServletRequest request) {
		Object memberIdObj = request.getAttribute("memberId");
		if (memberIdObj instanceof Integer) {
			return (Integer) memberIdObj;
		}
		return null; // 未登入
	}

}
