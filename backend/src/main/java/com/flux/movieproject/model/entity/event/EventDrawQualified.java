package com.flux.movieproject.model.entity.event;

import java.time.LocalDate;

import com.flux.movieproject.model.entity.member.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class EventDrawQualified {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer qualifiedId;
	
	// 多對一關聯：多個抽獎資格可來自一個活動
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

	// 多對一關聯：多個抽獎資格可來自一個會員
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
	private String source;

	private LocalDate createTime;

}
