package com.flux.movieproject.model.entity.event;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor 
@Table
public class CouponCategory {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer couponCategoryId; 

    private String couponCategoryName; // 類別名稱 

    private String description; // 類別描述
}