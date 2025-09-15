package com.flux.movieproject.repository.movie;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.model.entity.movie.Actor;

public interface ActorRepository extends JpaRepository<Actor, Integer>{

	Optional<Actor> findByName(String name); // 根據姓名查找演員
    Optional<Actor> findByTmdbActorId(Integer tmdbActorId); // 根據 TMDB ID 查找演員

    /**
     * 根據 ID 查詢演員的詳細資訊，並預先載入關聯的電影及角色列表。
     * @param actorId 演員 ID
     * @return 演員實體，包含已載入的 movieActors 及其關聯的 movie。
     */
    // 修正點：使用 @Query 明確定義查詢，並使用 :actorId 綁定參數
    @Query("SELECT a FROM Actor a LEFT JOIN FETCH a.movieActors ma LEFT JOIN FETCH ma.movie m WHERE a.actorId = :actorId")
    @EntityGraph(attributePaths = {"movieActors", "movieActors.movie"}) // 仍然保留 EntityGraph 以確保載入策略
    Optional<Actor> findActorWithDetailsByActorId(@Param("actorId") Integer actorId); // 修正點：使用 @Param 綁定參數
}
