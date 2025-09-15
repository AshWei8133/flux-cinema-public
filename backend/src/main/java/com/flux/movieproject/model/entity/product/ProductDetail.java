package com.flux.movieproject.model.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "product_detail")
public class ProductDetail {

    @Id
    @Column(name = "product_detail_id", nullable = false)
    private Integer productDetailId;

    @Column(name = "product_order_detail", nullable = false)
    private Integer productOrderDetail;

    @Nationalized
    @Column(name = "\"size\"", length = 50)
    private String size;

    @Nationalized
    @Column(name = "color", length = 50)
    private String color;

    @Nationalized
    @Column(name = "ice_level", length = 50)
    private String iceLevel;

    @Column(name = "sweetness_level", precision = 5, scale = 2)
    private BigDecimal sweetnessLevel;

    @Nationalized
    @Column(name = "topping", length = 50)
    private String topping;

}