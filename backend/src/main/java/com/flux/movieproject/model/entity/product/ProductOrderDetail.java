package com.flux.movieproject.model.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "product_order_detail")
public class ProductOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id", nullable = false)
    private Integer detailId;



    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "subtotal", nullable = false)
    private Integer subtotal;

    @Column(name = "extra_price")
    private Integer extraPrice;

//    @ColumnDefault("getdate()")
//    @Column(name = "order_time", nullable = false)
//    private Instant orderTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private ProductOrder productOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Nationalized
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    //ALTER TABLE product_order_detail
    //ADD product_name NVARCHAR(50);


}