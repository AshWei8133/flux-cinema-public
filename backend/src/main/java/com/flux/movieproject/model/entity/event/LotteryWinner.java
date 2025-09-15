package com.flux.movieproject.model.entity.event;

import java.time.LocalDateTime;

import com.flux.movieproject.model.entity.member.EventParticipation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table
public class LotteryWinner {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer lotteryWinnerId;
	
	// 關聯擁有者
	// 定義多對一關聯：多個中獎記錄可以關聯到一個活動參加記錄
	@ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "event_record_id", nullable = false)
	private EventParticipation eventParticipation;
	
	private String prizeContent;
	
	private LocalDateTime drawTime;
}
