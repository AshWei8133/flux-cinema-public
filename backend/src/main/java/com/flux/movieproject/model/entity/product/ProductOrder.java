package com.flux.movieproject.model.entity.product;

import com.flux.movieproject.model.entity.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "product_order")
public class ProductOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Integer orderId;




    @Column(name = "order_time", nullable = false)
    private Instant orderTime;

    @Column(name = "order_amount", nullable = false)
    private Integer orderAmount;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "final_payment_amount", nullable = false)
    private Integer finalPaymentAmount;

    @Column(name = "coupon_id")
    private Integer couponId;

    @Column(name = "orderNumber", length = 50) // 對應資料庫欄位
    private String orderNumber;


    @Column(name="customer_email")
    private String customerEmail;



    @Enumerated(EnumType.STRING)
    @Nationalized
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;


    @Enumerated(EnumType.STRING)
    @Nationalized
    @Column(name = "order_status", length = 20)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "productOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOrderDetail>  productOrderDetails = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") //
    private Member member; //
}


//    @Column(name = "shipping_address",nullable = true)
//    @Nationalized // 如果地址包含中文或其他非拉丁文字元
//    private String shippingAddress;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "shipping_method",nullable = true)
//    private ShippingMethod shippingMethod;