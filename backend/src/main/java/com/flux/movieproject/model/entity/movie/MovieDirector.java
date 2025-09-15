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
@Table(name = "movie_director")
public class MovieDirector {

    @EmbeddedId // 標記為複合主鍵
    private MovieDirectorId id;

    @ManyToOne // 多個 MovieDirector 記錄對應一個 Movie
    @MapsId("movieId") // 映射複合主鍵中的 movieId 屬性
    @JoinColumn(name = "movie_id") // 對應資料庫中的外鍵欄位
    @JsonIgnore
    private Movie movie;

    @ManyToOne // 多個 MovieDirector 記錄對應一個 Director
    @MapsId("directorId") // 映射複合主鍵中的 directorId 屬性
    @JoinColumn(name = "director_id") // 對應資料庫中的外鍵欄位
    private Director director;

    // **修正點：這個建構子是關鍵！**
    // 這是為了讓 TMDBDataImportService 能夠創建 MovieDirector 物件而定義的建構子
    public MovieDirector(Movie movie, Director director) {
        this.movie = movie;
        this.director = director;
        // 初始化複合主鍵，使用 Movie 和 Director 物件的 ID
        this.id = new MovieDirectorId(movie.getId(), director.getDirectorId());
    }
}
