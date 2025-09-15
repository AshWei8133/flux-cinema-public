package com.flux.movieproject.repository.event;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.event.EventCategory;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Integer> {

	 /**
     * 根據類型名稱查詢是否存在。
     * Spring Data JPA 會根據這個方法名稱，自動產生對應的 SQL 查詢語句。
     * @param categoryName 欲檢查的類型名稱
     * @return 如果存在則返回 true，否則返回 false
     */
    boolean existsByEventCategoryName(String eventCategoryName);
}
