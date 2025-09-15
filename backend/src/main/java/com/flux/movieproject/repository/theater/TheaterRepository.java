package com.flux.movieproject.repository.theater;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.model.entity.theater.Theater;

public interface TheaterRepository extends JpaRepository<Theater, Integer> {
	@Query("SELECT t from Theater t LEFT JOIN FETCH t.theaterType")
	List<Theater> findAllWithTheaterType();

	/**
	 * 【新增】一次性根據 ID 列表查詢多個影廳，並使用 JOIN FETCH 預先載入關聯的座位(seats)。 這樣可以避免在後續操作中因延遲加載(Lazy
	 * Loading)而觸發 N+1 查詢。
	 */
	@Query("SELECT t FROM Theater t LEFT JOIN FETCH t.seats WHERE t.theaterId IN :ids")
	List<Theater> findByIdInWithSeats(@Param("ids") List<Integer> ids);
}
