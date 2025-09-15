package com.flux.movieproject.model.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberLevelDTO {
	private Integer memberLevelId;
	private String levelName;
	private byte[] levelIcon;
	private Integer thresholdLowerBound;
	private String upgradeConditionDescription;
}
