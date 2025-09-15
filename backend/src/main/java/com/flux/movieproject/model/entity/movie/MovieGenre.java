package com.flux.movieproject.model.entity.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "movie_genre")
public class MovieGenre {

	@EmbeddedId // 標記為複合主鍵
	private MovieGenreId id;

	@ManyToOne // 多個 MovieGenre 記錄對應一個 Movie
	@MapsId("movieId") // 映射複合主鍵中的 movieId 屬性
	@JoinColumn(name = "movie_id") // 對應資料庫中的外鍵欄位
	@JsonIgnore
	private Movie movie;

	@ManyToOne // 多個 MovieGenre 記錄對應一個 Genre
	@MapsId("genreId") // 映射複合主鍵中的 genreId 屬性
	@JoinColumn(name = "genre_id") // 對應資料庫中的外鍵欄位
	private Genre genre;

	// **修正點：這個建構子是關鍵！**
	// 這是為了讓 TMDBDataImportService 能夠創建 MovieGenre 物件而定義的建構子
	public MovieGenre(Movie movie, Genre genre) {
		this.movie = movie;
		this.genre = genre;
		// 初始化複合主鍵，使用 Movie 和 Genre 物件的 ID
		this.id = new MovieGenreId(movie.getId(), genre.getGenreId());
	}
}
