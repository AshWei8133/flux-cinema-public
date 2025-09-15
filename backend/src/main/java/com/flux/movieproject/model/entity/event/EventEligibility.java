package com.flux.movieproject.model.entity.event;


import com.flux.movieproject.model.entity.member.MemberLevel;
import com.flux.movieproject.model.entity.movie.Movie;
import com.flux.movieproject.model.entity.theater.MovieSession;

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
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventEligibility {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer eventEligibilityId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id")
	private Event event; // 關聯的 Event 實體
	
	// 多對一關聯：可選的電影關聯
    // 假設 Movie 表格存在且其主鍵是 movie_id (或 id)
    @ManyToOne(fetch = FetchType.LAZY) // 推薦使用 FetchType.LAZY (延遲加載)
    @JoinColumn(name = "movie_id") // movie_id 可為 NULL，所以 JoinColumn 不設定 nullable = false
    private Movie movie; // 關聯的 Movie 實體

    // 多對一關聯：可選的場次關聯
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "session_id")
    private MovieSession session;

    // 多對一關聯：可選的會員等級關聯
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "member_level_id")
    private MemberLevel memberLevel;
    
    

    
    
    
    
    
    
    
    
    
    
    
}
