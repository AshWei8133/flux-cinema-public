package com.flux.movieproject.repository.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.event.Coupon;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.model.entity.member.MemberCoupon;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Integer> {

	/**
	 * 取得第一筆會員優惠券紀錄
	 * 
	 * @param member 特定會員資料
	 * @param coupon 特定優惠券資料
	 * @param status 使用狀態
	 * @return 會員優惠紀錄
	 */
	Optional<MemberCoupon> findFirstByMemberAndCouponAndStatus(Member member, Coupon coupon, String status);

	// 是否已領此券（防重複）
	boolean existsByMember_MemberIdAndCoupon_CouponId(Integer memberId, Integer couponId);

	// 這張券目前被領了幾張（控量）
	long countByCoupon_CouponId(Integer couponId);

	// 我的券（分頁）
	Page<MemberCoupon> findByMember_MemberId(Integer memberId, Pageable pageable);

	// 我的券關鍵字查詢（名稱/描述）
	@Query("""
			  select mc from MemberCoupon mc
			  join mc.coupon c
			  where mc.member.memberId = :memberId
			    and (:kw is null or :kw = ''
			         or lower(c.couponName) like lower(concat('%', :kw, '%'))
			         or lower(c.couponDescription) like lower(concat('%', :kw, '%')))
			""")
	Page<MemberCoupon> searchByMemberAndKeyword(@Param("memberId") Integer memberId, @Param("kw") String keyword,
			Pageable pageable);

	// 批量查詢：某會員在一批 couponId 中已領的券
	List<MemberCoupon> findByMember_MemberIdAndCoupon_CouponIdIn(Integer memberId, List<Integer> couponIds);

	@Query("""
			  select mc
			  from MemberCoupon mc
			  join mc.coupon c
			  where mc.member.memberId = :memberId
			""")
	Page<MemberCoupon> findAliveByMember(@Param("memberId") Integer memberId, Pageable pageable);

// 同一時間只允許一條「memberId+couponId」的請求進入檢查＋寫入
	long countByMember_MemberIdAndCoupon_CouponId(Integer memberId, Integer couponId);

	List<MemberCoupon> findByMemberAndStatus(Member member, String status);

	@Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon WHERE mc.member = :member AND mc.status = '未使用'")
	List<MemberCoupon> findByMemberAndStatusWithCoupon(@Param("member") Member member);

	@Query("SELECT mc FROM MemberCoupon mc JOIN FETCH mc.coupon " +
			"WHERE mc.member.memberId = :memberId AND mc.coupon.couponId = :couponId")
	List<MemberCoupon> findByMemberIdAndCouponId(@Param("memberId") Integer memberId,
												 @Param("couponId") Integer couponId);
	/**
     * 這是為了優化前端體驗，一次性查詢出某個會員對多張優惠券的已領取數量。
     *
     * @param memberId  會員ID
     * @param couponIds 優惠券ID列表
     * @return List<Object[]> 每個 Object[] 包含 [couponId, count]
     */
	@Query("SELECT c.couponId, COUNT(mc.memberCouponId) " +
		       "FROM MemberCoupon mc JOIN mc.coupon c JOIN mc.member m " +
		       "WHERE m.memberId = :memberId " +
		       "AND c.couponId IN :couponIds " +
		       "GROUP BY c.couponId")
		List<Object[]> findClaimCountsByMemberAndCoupons(@Param("memberId") Integer memberId, @Param("couponIds") List<Integer> couponIds);

}
