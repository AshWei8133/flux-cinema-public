package com.flux.movieproject.model.entity.member;

import java.time.LocalDateTime;

import com.flux.movieproject.model.entity.movie.Movie;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter // Lombok 自動生成所有屬性的 getter 方法
@Setter // Lombok 自動生成所有屬性的 setter 方法
@NoArgsConstructor // Lombok 自動生成無參建構子
@Entity // 標記為 JPA 實體
@Table(name = "favorite_movie") // 映射到資料庫中的 favorite_movie 表格
public class FavoriteMovie {

	@EmbeddedId // 標記為複合主鍵
	private FavoriteMovieId id; // 引用我們定義的複合主鍵類

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("memberId") // 必須與 FavoriteMovieId 中的屬性名一致
	@JoinColumn(name = "member_id", nullable = false) // 映射到 member_id 欄位
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("movieId") // 必須與 FavoriteMovieId 中的屬性名一致
	@JoinColumn(name = "movie_id", nullable = false) // 映射到 movie_id 欄位
	private Movie movie;

	private LocalDateTime addedToFavoritesTime; // 加入收藏清單的時間

}
