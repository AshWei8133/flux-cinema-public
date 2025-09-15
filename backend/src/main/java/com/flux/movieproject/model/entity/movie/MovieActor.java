package com.flux.movieproject.model.entity.movie; // <-- 請確認這個 package 路徑與您的檔案實際位置一致

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable; // 確保導入 Embeddable
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Data; // 確保導入 Data
import lombok.AllArgsConstructor; // 確保導入 AllArgsConstructor

import java.io.Serializable;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "movie_actor")
public class MovieActor {

	// 複合主鍵類別 - MovieActorId
    @Embeddable // 標記為可嵌入的類別，用於複合主鍵
    @Data // Lombok 註解，自動生成 Getter, Setter, equals, hashCode, toString
    @NoArgsConstructor // Lombok 註解，自動生成無參數建構子
    @AllArgsConstructor // Lombok 註解，自動生成所有參數的建構子
    public static class MovieActorId implements Serializable { // <-- 這必須是 public static
        private static final long serialVersionUID = 1L;

        @Column(name = "movie_id")
        private Integer movieId;

        @Column(name = "actor_id")
        private Integer actorId;

        // 必須覆寫 equals 和 hashCode 方法，這對於複合主鍵的正確運作至關重要
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MovieActorId that = (MovieActorId) o;
            return Objects.equals(movieId, that.movieId) &&
                   Objects.equals(actorId, that.actorId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(movieId, actorId);
        }
    }

    @EmbeddedId // 標記為複合主鍵
    private MovieActorId id;

    @ManyToOne // 多個 MovieActor 記錄對應一個 Movie
    @MapsId("movieId") // 映射複合主鍵中的 movieId 屬性
    @JoinColumn(name = "movie_id") // 對應資料庫中的外鍵欄位
    @JsonIgnore // 通常用於防止 JSON 序列化時的循環引用
    private Movie movie;

    @ManyToOne // 多個 MovieActor 記錄對應一個 Actor
    @MapsId("actorId") // 映射複合主鍵中的 actorId 屬性
    @JoinColumn(name = "actor_id") // 對應資料庫中的外鍵欄位
    private Actor actor;

    @Column(name = "character", length = 100) // 飾演角色名
    private String characterName;

    @Column(name = "order_num") // 出場順序
    private Integer orderNum;

    // 這是為了讓 TMDBDataImportService 能夠創建 MovieActor 物件而定義的建構子
    public MovieActor(Movie movie, Actor actor, String characterName, Integer orderNum) {
        this.movie = movie;
        this.actor = actor;
        this.characterName = characterName;
        this.orderNum = orderNum;
        // 初始化複合主鍵，使用 Movie 和 Actor 物件的 ID
        this.id = new MovieActorId(movie.getId(), actor.getActorId());
    }

}