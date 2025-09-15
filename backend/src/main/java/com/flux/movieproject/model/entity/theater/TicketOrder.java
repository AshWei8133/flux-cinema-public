package com.flux.movieproject.model.entity.theater;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.flux.movieproject.enums.OrderStatus;
import com.flux.movieproject.model.entity.event.Coupon;
import com.flux.movieproject.model.entity.member.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TicketOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer ticketOrderId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private Integer totalAmount;

	private Integer totalTicketAmount;

	private Integer totalDiscount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coupon_id")
	private Coupon coupon;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	@CreatedDate
	@Column(name = "created_time", nullable = false, updatable = false)
	private LocalDateTime createdTime;

	private LocalDateTime paymentTime;
	
	/**
	 * 支付方式 (例如 'Credit Card', 'LINE Pay')
	 */
	@Column(name = "payment_type", length = 50)
	private String paymentType;

	/**
	 * 第三方金流平台返回的唯一交易ID
	 */
	@Column(name = "payment_transaction_id", length = 255)
	private String paymentTransactionId;
	

	// 建立票券訂單與明細關聯
	@OneToMany(mappedBy = "ticketOrder", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TicketOrderDetail> ticketOrderDetails = new ArrayList<>();
}
