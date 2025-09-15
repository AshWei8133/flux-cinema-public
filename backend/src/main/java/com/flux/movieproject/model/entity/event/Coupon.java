package com.flux.movieproject.model.entity.event;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.flux.movieproject.enums.DiscountType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_eligibility_id")
    private EventEligibility eventEligibility;

    private String serialNumber;
    private String couponName;
    private String couponDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_category_id")
    private CouponCategory couponCategory;
    
    @Enumerated(EnumType.STRING) 
	private DiscountType discountType;

    private Integer discountAmount;
    private Integer minimumSpend;

    @Lob
    private byte[] couponImage;

    private String status;
    private Integer redeemableTimes;
    private Integer quantity;
}
