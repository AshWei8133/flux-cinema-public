package com.flux.movieproject.model.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "product_optional_price")
public class ProductOptionalPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id", nullable = false)
    private Integer optionId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Nationalized
    @Column(name = "\"size\"", length = 50)
    private String size;

    @Nationalized
    @Column(name = "topping", length = 50)
    private String topping;

    @Column(name = "extra_price", nullable = false)
    private Integer extraPrice;

}