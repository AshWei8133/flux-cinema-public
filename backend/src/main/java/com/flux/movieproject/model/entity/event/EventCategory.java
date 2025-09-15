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
public class EventCategory {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer eventCategoryId; 

    private String eventCategoryName; // 類別名稱 (例如 '會員專享')

    private String description; // 類別描述
}
