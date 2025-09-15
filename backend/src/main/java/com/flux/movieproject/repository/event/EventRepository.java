package com.flux.movieproject.repository.event;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.model.dto.event.EventAdminListDTO;
import com.flux.movieproject.model.entity.event.Event;

// Generic介面  1.實體類別  2.PK型別
public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

	List<Event> findByTitleContainingIgnoreCase(String title);

	List<Event> findAllByOrderByStartDateDesc();

	// Spring Data JPA 會自動根據這個方法名稱和參數，產生正確的 SQL 查詢
	List<Event> findByTitleContainingIgnoreCase(String title, Sort sort);

	/**
	 * 【新增方法】: 查詢最新的前 5 筆活動，且狀態必須是指定的狀態之一。 Spring Data JPA 會根據這個方法名稱，自動產生對應的 SQL 查詢。
	 * "Top5": 限定最多返回 5 筆結果。 "ByStatusIn": 根據狀態欄位進行篩選，條件是狀態必須在傳入的 List 中。
	 * "OrderByStartDateDesc": 根據 startDate 欄位進行降序排列 (由新到舊)。
	 * 
	 * @param status 包含所有有效狀態的列表 (例如 ["進行中", "尚未開始"])
	 * @return 最新的 5 筆活動實體列表
	 */
//    List<Event> findTop5ByStatusInOrderByStartDateDesc(List<String> statuses);
	List<Event> findTop5ByOrderByStartDateDesc(); // 根據 startDate 排序

	@Query("""
			    select e from Event e
			    where (:kw is null
			        or lower(e.title) like lower(concat('%', :kw, '%'))
			        or lower(e.content) like lower(concat('%', :kw, '%')))
			""")
	Page<Event> search(@Param("kw") String keyword, Pageable pageable);
	
	
	// 使用 JPQL 的 JOIN FETCH
    // 這會產生一條包含 JOIN 的 SQL，一次性獲取 Event 和其關聯的 EventCategory
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.eventCategory")
    List<Event> findAllWithCategory(Sort sort);

    // 如果還要加上 title 查詢
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.eventCategory WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Event> findByTitleContainingIgnoreCaseWithCategory(@Param("title") String title, Sort sort);
    
    @Query("""
			SELECT NEW com.flux.movieproject.model.dto.event.EventAdminListDTO(
			    e.eventId,
			    e.title,
			    e.content,
			    c.eventCategoryId,
			    c.eventCategoryName,
			    'pending', 
			    e.startDate,
			    e.endDate,
			    e.sessionCount,
			    (e.image IS NOT NULL) 
			)
			FROM Event e LEFT JOIN e.eventCategory c
			WHERE (:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')))
			""")
	List<EventAdminListDTO> findAllAsAdminListDTO(@Param("title") String title, Sort sort);
    
    @Query("""
    		  SELECT e FROM Event e
    		  WHERE (e.endDate IS NULL OR e.endDate >= :today)  
    		  ORDER BY e.startDate DESC, e.eventId DESC
    		""")
    		List<Event> findActiveOrderByStartDateDesc(@Param("today") java.time.LocalDate today, Pageable pageable);

    
}
