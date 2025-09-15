package com.flux.movieproject.repository.member;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.flux.movieproject.model.entity.member.FavoriteMovie;
import com.flux.movieproject.model.entity.member.FavoriteMovieId;
import com.flux.movieproject.model.entity.member.Member;

public interface FavoriteMovieRepository extends JpaRepository<FavoriteMovie, FavoriteMovieId> {
	/**
     * 根據會員實體，找出所有相關的收藏紀錄。
     * "JOIN FETCH fm.movie" 是一個優化，它會讓 JPA 在查詢時，
     * 順便把關聯的 Movie 物件資料也一起抓回來，避免之後的 N+1 查詢問題。
     * @param member 會員實體
     * @return 收藏紀錄的列表
     */
    @Query("SELECT fm FROM FavoriteMovie fm JOIN FETCH fm.movie WHERE fm.member = :member")
    List<FavoriteMovie> findByMemberWithMovie(Member member);

    /**
     * 根據會員實體，只找出所有收藏的電影 ID。
     * 這樣查詢比獲取完整物件更輕量、更有效率。
     * @param member 會員實體
     * @return 收藏的電影 ID 集合
     */
    @Query("SELECT fm.movie.id FROM FavoriteMovie fm WHERE fm.member = :member")
    Set<Integer> findMovieIdsByMember(Member member);

}
