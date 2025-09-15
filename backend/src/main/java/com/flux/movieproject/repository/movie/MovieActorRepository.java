package com.flux.movieproject.repository.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 導入 Query
import org.springframework.data.repository.query.Param; // 導入 Param
import com.flux.movieproject.model.entity.movie.MovieActor;
import com.flux.movieproject.model.entity.movie.MovieActor.MovieActorId; // 確保導入 MovieActor.MovieActorId
import java.util.Optional;

public interface MovieActorRepository extends JpaRepository<MovieActor, MovieActorId> {
    // 這裡不需要額外的方法，JpaRepository 已經提供了基本操作

    /**
     * 根據電影 ID 和演員 ID 查詢 MovieActor 關聯。
     * @param movieId 電影 ID
     * @param actorId 演員 ID
     * @return 匹配的 MovieActor 實體或 Optional.empty()
     */
    @Query("SELECT ma FROM MovieActor ma WHERE ma.id.movieId = :movieId AND ma.id.actorId = :actorId")
    Optional<MovieActor> findByMovieIdAndActorId(@Param("movieId") Integer movieId, @Param("actorId") Integer actorId); // 修正點：新增自定義查詢方法
}

