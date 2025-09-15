package com.flux.movieproject.model.entity.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member_level")
public class MemberLevel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer memberLevelId;

	private String levelName;

	@Lob
	private byte[] levelIcon;

	private Integer thresholdLowerBound;

	private String upgradeConditionDescription;

}
