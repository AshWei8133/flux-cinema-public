package com.flux.movieproject.model.dto.ticket;

import lombok.Data;

@Data
public class PriceRuleUpdateDTO {
	private Integer theaterTypeId;
    private Integer ticketTypeId;
    private Integer price;
}
