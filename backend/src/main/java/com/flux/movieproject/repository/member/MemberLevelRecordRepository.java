package com.flux.movieproject.repository.member;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.flux.movieproject.model.entity.member.MemberLevelRecord;

@Repository
public interface MemberLevelRecordRepository extends JpaRepository<MemberLevelRecord, Integer> {

	/**
	 * 根據會員ID查詢當前有效的會員等級紀錄，當前有效意味著該紀錄的結束日期為 NULL
	 * 
	 * @param memberId
	 * @return
	 */
	Optional<MemberLevelRecord> findByMemberIdAndEndDateIsNull(Integer memberId);
	
	/**
     * 根據會員 ID 查找最新的一筆等級紀錄。
     * 方法名中的 "ByMemberId" 會對應到 MemberLevelRecord 實體中的 "memberId" 屬性。
     * "Top" 表示只取第一筆結果。
     * "OrderByStartDateDesc" 表示根據 startDate 欄位進行降冪排序。
     * @param memberId 會員的 ID
     * @return 一個包含最新等級紀錄的 Optional 物件，如果找不到則為空
     */
    Optional<MemberLevelRecord> findTopByMemberIdOrderByStartDateDesc(Integer memberId);


	// @Query("""
	//         SELECT r FROM MemberLevelRecord r
	//         WHERE r.memberId = :memberId
	//           AND r.startDate <= :now
	//           AND (r.endDate IS NULL OR r.endDate >= :now)
	//         ORDER BY r.startDate DESC
	//         """)
	//     Optional<MemberLevelRecord> findCurrentLevel(@Param("memberId") Integer memberId,
	//                                                  @Param("now") LocalDateTime now);

	//     // 若你想在抓不到「目前」紀錄時，退而求其次抓「最近的一筆歷史」
	//     @Query("""
	//         SELECT r FROM MemberLevelRecord r
	//         WHERE r.memberId = :memberId
	//         ORDER BY r.startDate DESC
	//         """)
	//     Optional<MemberLevelRecord> findLatestAny(@Param("memberId") Integer memberId);
	}

