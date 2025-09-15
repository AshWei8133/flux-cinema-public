package com.flux.movieproject.model.entity.member;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable // 標記為可嵌入的複合主鍵類
@NoArgsConstructor // Lombok 自動生成無參建構子
@AllArgsConstructor // Lombok 自動生成所有參數的建構子
public class FavoriteMovieId implements Serializable {

	private static final long serialVersionUID = 1L; // 序列化 ID

	@Column(name = "member_id", nullable = false) // 映射到 member_id 欄位，且不可為空
	private Integer memberId;

	@Column(name = "movie_id", nullable = false) // 映射到 movie_id 欄位，且不可為空
	private Integer movieId;

	// 複合主鍵類必須重寫 equals() 和 hashCode() 方法
	// 這是 JPA 正確比較和識別實體所必需的
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FavoriteMovieId that = (FavoriteMovieId) o;
		return Objects.equals(memberId, that.memberId) && Objects.equals(movieId, that.movieId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(memberId, movieId);
	}

	// 您可能還需要 getter 方法，Lombok 的 @Getter 在這裡會自動生成
	public Integer getMemberId() {
		return memberId;
	}

	public Integer getMovieId() {
		return movieId;
	}

}
