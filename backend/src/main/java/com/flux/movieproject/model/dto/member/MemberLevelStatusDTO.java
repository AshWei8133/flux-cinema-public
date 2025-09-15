package com.flux.movieproject.model.dto.member;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLevelStatusDTO {
	private String currentLevelName;
	private byte[] currentLevelIcon;
	private BigDecimal totalSpent;
	private String nextLevelName;
	private BigDecimal nextLevelThreshold;
	private String upgradeConditionDescription;
	private BigDecimal progressPercentage;

}
