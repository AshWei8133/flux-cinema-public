package com.flux.movieproject.model.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Nationalized
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;


    @Column(name = "price", nullable = false)
    private Integer price;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @Nationalized
    @Lob
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;


    @Column(name = "creation_time", nullable = false)
    private Instant creationTime;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;


}