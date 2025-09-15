package com.flux.movieproject.model.entity.member;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "member_level_record")
public class MemberLevelRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer memberLevelRecordId;

	private Integer memberId;

	private Integer memberLevelId;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

}
