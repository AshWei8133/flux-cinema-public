package com.flux.movieproject.model.entity.theater;

import java.math.BigDecimal;

import com.flux.movieproject.enums.DiscountType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TicketType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer ticketTypeId;
	
	private String ticketTypeName;
	
	private String description;
	
	private Boolean isEnabled;
	
	@Enumerated(EnumType.STRING) 
	private DiscountType discountType;
	
	private BigDecimal discountValue;
}
