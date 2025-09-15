package com.flux.movieproject.repository.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.flux.movieproject.model.entity.event.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
	
	Coupon findByCouponId(Integer couponId);
	
	/**
	 * 查詢邏輯：券啟用中(status)且該活動沒有設任何等級限制）或（有設，但存在一條要求等級 ≤ userRank）
	 * 普通(rank = 1 )只會看到（不限 或 ≤ 1）;黃金(2)（不限 或 ≤ 2）,尊爵(3) 看到（不限或 ≤3）
	 */
	// @Query("""
	//         SELECT DISTINCT c
	//         FROM Coupon c
	//         LEFT JOIN EventEligibility ee ON ee.event = c.event
	//         LEFT JOIN ee.memberLevel ml
	//         WHERE c.status = 'ACTIVE'
	//           AND (
	//                 NOT EXISTS (
	//                     SELECT 1 FROM EventEligibility e0
	//                     WHERE e0.event = c.event AND e0.memberLevel IS NOT NULL
	//                 )
	//                 OR
	//                 EXISTS (
	//                     SELECT 1 FROM EventEligibility e1
	//                     WHERE e1.event = c.event
	//                       AND (e1.memberLevel IS NULL OR e1.memberLevel.rank <= :userRank)
	//                 )
	//           )
	//         """)
	//     Page<Coupon> findClaimableByUserRank(@Param("userRank") int userRank, Pageable pageable);
	

}
