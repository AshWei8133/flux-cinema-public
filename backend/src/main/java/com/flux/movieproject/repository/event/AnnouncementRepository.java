package com.flux.movieproject.repository.event;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.flux.movieproject.model.entity.event.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {
	/**
	 * 根據標題進行不區分大小寫的模糊搜尋。 
	 *
	 * @param title 標題關鍵字。
	 * @return 符合條件的公告列表。
	 */
	List<Announcement> findByTitleContainingIgnoreCase(String title);

	/**
	 * 查詢所有公告並按發布日期降序排列。 
	 *
	 * @return 按發布日期由近到遠排序的公告列表。
	 */
	List<Announcement> findAllByOrderByPublishDateDesc();
	
	// 讓 findAll 方法可以接收 Sort 參數
    List<Announcement> findAll(Sort sort);

    // 讓模糊查詢方法也可以接收 Sort 參數
    List<Announcement> findByTitleContainingIgnoreCase(String title, Sort sort);
    
    /**
	 * 根據標題進行模糊查詢，忽略大小寫，並支援分頁和排序。
	 * @param title 標題關鍵字
	 * @param pageable 分頁和排序資訊
	 * @return 包含公告分頁結果的 Page 物件
	 */
	Page<Announcement> findByTitleContainingIgnoreCase(String title, Pageable pageable);

	// 由於 findAll(Pageable pageable) 是 JpaRepository 內建的方法，無需額外聲明。
    // 如果你只需要排序，也可以保留這個，但 findAll(Pageable) 已經包含了排序功能
	// List<Announcement> findAllByOrderByPublishDateDesc(); // 這個方法現在可以用 findAll(Pageable) 代替
	
    /**
     * 根據類型名稱查詢是否存在。
     * Spring Data JPA 會根據這個方法名稱，自動產生對應的 SQL 查詢語句。
     * @param announcementTitle 欲檢查的類型名稱
     * @return 如果存在則返回 true，否後返回 false
     */
    boolean existsByTitle(String title); // 假設你用 title 檢查是否存在
    
    
    @Query("""
            SELECT a FROM Announcement a
            WHERE (:kw IS NULL OR
                   lower(a.title)   LIKE lower(concat('%', :kw, '%')) OR
                   lower(a.content) LIKE lower(concat('%', :kw, '%')))
            """)
        Page<Announcement> searchByKeyword(@Param("kw") String keyword, Pageable pageable);
}