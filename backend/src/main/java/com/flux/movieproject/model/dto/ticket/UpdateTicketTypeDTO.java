package com.flux.movieproject.model.dto.ticket;

import java.math.BigDecimal;

import com.flux.movieproject.enums.DiscountType;

import lombok.Data;

@Data
public class UpdateTicketTypeDTO {

    private String ticketTypeName;

    private String description;

    private Boolean isEnabled;

    private DiscountType discountType;

    private BigDecimal discountValue;
}
