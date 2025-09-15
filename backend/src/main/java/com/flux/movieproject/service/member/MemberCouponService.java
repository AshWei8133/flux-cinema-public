package com.flux.movieproject.service.member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.dto.event.CouponListItemDTO;
import com.flux.movieproject.model.dto.member.MemberCouponListDTO;
import com.flux.movieproject.model.entity.event.Coupon;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.model.entity.member.MemberCoupon;
import com.flux.movieproject.repository.event.CouponRepository;
import com.flux.movieproject.repository.member.MemberCouponRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberCouponService {
	@Autowired
    private  CouponRepository couponRepository;
	
	@Autowired
    private  MemberCouponRepository memberCouponRepository;
	
//	@Autowired
//	private MemberLevelResolver levelResolver;
//	
//	@Autowired
//	private EventEligibilityRepository eligibilityRepository;
	
	
    /** 前台優惠專區列表（回傳 CouponListItemDTO，status=已領取/未領取） */
	public Page<CouponListItemDTO> getMarket(Integer memberId, String keyword, Pageable pageable) {
	    Page<Coupon> page = couponRepository.findAll(pageable); // 假設這裡沒有關鍵字過濾
	    List<Coupon> coupons = page.getContent();
	    List<Integer> couponIds = coupons.stream().map(Coupon::getCouponId).collect(Collectors.toList());

	    // 如果用戶已登入，查詢他對這些券的領取紀錄
	    Map<Integer, Long> myClaimCounts = new HashMap<>();
	    if (memberId != null && !couponIds.isEmpty()) {
	        List<Object[]> results = memberCouponRepository.findClaimCountsByMemberAndCoupons(memberId, couponIds);
	        for (Object[] result : results) {
	            Integer couponId = (Integer) result[0];
	            Long count = (Long) result[1];
	            myClaimCounts.put(couponId, count);
	        }
	    }
	    
	    System.out.println("回傳給前端的使用次數法 " + myClaimCounts);

	    // 組 DTO
	    List<CouponListItemDTO> dtoList = coupons.stream().map(c -> {
	        long count = myClaimCounts.getOrDefault(c.getCouponId(), 0L);
	        return toMarketDTO(c, count); // 使用新的 toMarketDTO 方法
	    }).collect(Collectors.toList());
	    
	    // ... 過濾 keyword 的邏輯需要整合進來 ...
	    return new PageImpl<>(dtoList, pageable, page.getTotalElements());
	}

//    private CouponListItemDTO toMarketDTO(Coupon c, boolean claimedByMe) {
//        CouponListItemDTO dto = new CouponListItemDTO();
//        dto.setCouponId(c.getCouponId());
//        dto.setSerialNumber(c.getSerialNumber());
//        dto.setEventId(c.getEvent() != null ? c.getEvent().getEventId() : null);
//        dto.setCouponCategoryId(c.getCouponCategory() != null ? c.getCouponCategory().getCouponCategoryId() : null);
//        dto.setEventEligibilityId(c.getEventEligibility() != null ? c.getEventEligibility().getEventEligibilityId() : null);
//
//        dto.setCouponName(c.getCouponName());
//        dto.setCouponDescription(c.getCouponDescription());
//        dto.setCouponCategory(c.getCouponCategory() != null ? c.getCouponCategory().getCouponCategoryName() : null);
//        dto.setDiscountAmount(c.getDiscountAmount());
//        dto.setMinimumSpend(c.getMinimumSpend());
//        dto.setStatus(claimedByMe ? "已領取" : "未領取");
//        dto.setRedeemableTimes(c.getRedeemableTimes());
//        dto.setQuantity(c.getQuantity());
//        if (c.getCouponImage() != null) {
//            dto.setCouponImageBase64(Base64.getEncoder().encodeToString(c.getCouponImage()));
//        }
//        return dto;
//    }

    /** 會員中心我的優惠券（回傳 MemberCouponListDTO，status: ALL/未使用/已使用） */
    @Transactional(readOnly = true)
    public Page<MemberCouponListDTO> getMyCoupons(Integer memberId, String status, String keyword, Pageable pageable) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId is null (Auth interceptor didn't set request attribute)");
        }

        Page<MemberCoupon> page = (keyword == null || keyword.isBlank())
                ? memberCouponRepository.findAliveByMember(memberId, pageable)    // ✅ 改這裡
                : memberCouponRepository.searchByMemberAndKeyword(memberId, keyword, pageable);

        List<MemberCoupon> src = page.getContent();

        List<MemberCoupon> filtered = new ArrayList<>();
        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            String s = status.trim();
            for (MemberCoupon mc : src) {
                // 注意：equalsIgnoreCase 對 null 參數安全，但我們仍保留
                if (s.equalsIgnoreCase(mc.getStatus())) {
                    filtered.add(mc);
                }
            }
        } else {
            filtered.addAll(src);
        }

        List<MemberCouponListDTO> dtoList = new ArrayList<>();
        for (MemberCoupon mc : filtered) {
            dtoList.add(toMemberDTOSafe(mc));  // 用安全版本
        }

        // totalElements 用原本 page 的，表示「符合 memberId + keyword 的總數」
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    private MemberCouponListDTO toMemberDTOSafe(MemberCoupon mc) {
        if (mc == null) throw new IllegalStateException("MemberCoupon is null");

        Member member = mc.getMember();
        if (member == null || member.getMemberId() == null) {
            throw new IllegalStateException("MemberCoupon.member is null or memberId missing");
        }

        Coupon c = mc.getCoupon();
        if (c == null || c.getCouponId() == null) {
            throw new IllegalStateException("MemberCoupon.coupon is null or couponId missing");
        }

        MemberCouponListDTO dto = new MemberCouponListDTO();
        dto.setMemberCouponId(mc.getMemberCouponId());
        dto.setMemberId(member.getMemberId());
        dto.setCouponId(c.getCouponId());
        dto.setStatus(mc.getStatus()); // '未使用' / '已使用'
        dto.setAcquisitionTime(mc.getAcquisitionTime());
        dto.setUsageTime(mc.getUsageTime());

        dto.setSerialNumber(c.getSerialNumber());
        dto.setCouponName(c.getCouponName());
        dto.setCouponDescription(c.getCouponDescription());
        dto.setCouponCategory(
                (c.getCouponCategory() != null) ? c.getCouponCategory().getCouponCategoryName() : null
        );
        dto.setDiscountAmount(c.getDiscountAmount());
        dto.setMinimumSpend(c.getMinimumSpend());
        if (c.getCouponImage() != null) {
            dto.setCouponImageBase64(Base64.getEncoder().encodeToString(c.getCouponImage()));
        }
        return dto;
    }

    /** 領取（建立 member_coupon，status='未使用'） */
    @Transactional
    public MemberCouponListDTO claim(Integer memberId, Integer couponId) {
        Coupon c = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("優惠券不存在: " + couponId));
        if (!isEnabled(c.getStatus())) {
            throw new IllegalStateException("此優惠券未啟用，無法領取。");
        }
        
		// 1. 取得這張券的「每人限領次數」
		Integer limit = c.getRedeemableTimes();

		// 2. 如果有設定限領次數 (例如: > 0)
		if (limit != null && limit > 0) {
			// 3. 查詢資料庫，看這位會員 (memberId) 已經領了幾次這張券 (couponId)
			long myClaimCount = memberCouponRepository.countByMember_MemberIdAndCoupon_CouponId(memberId, couponId);

			// 4. 如果已領取的次數 >= 限制次數，就拋出異常，阻止領取
			if (myClaimCount >= limit) {
				// 這個錯誤訊息會被前端的 catch 捕獲並顯示給使用者
				throw new IllegalStateException("已達個人領取上限，每人限領 " + limit + " 張。");
			}
		}

        // 有控量就檢查是否還有名額（不做 schema 變更）
        if (c.getQuantity() != null) {
            long claimed = memberCouponRepository.countByCoupon_CouponId(couponId);
            if (claimed >= c.getQuantity()) {
                throw new IllegalStateException("此優惠券已發完。");
            }
        }

        Member m = new Member();
        m.setMemberId(memberId);

        MemberCoupon mc = new MemberCoupon();
        mc.setMember(m);
        mc.setCoupon(c);
        mc.setStatus("未使用");
        mc.setAcquisitionTime(LocalDateTime.now());
        mc = memberCouponRepository.save(mc); // ★ 每次都新增一筆
        
        System.out.println("會員優惠券紀錄!!!!!!!  " + mc.getMemberCouponId());
        

        // （可選）真的把 quantity 減 1 —— 不是 schema 變更，只是更新值
        if (c.getQuantity() != null) {
            c.setQuantity(Math.max(0, c.getQuantity() - 1));
            couponRepository.save(c);
        }

        return toMemberDTOSafeWithDisplaySerial(mc);
    }

    private MemberCouponListDTO toMemberDTOSafeWithDisplaySerial(MemberCoupon mc) {
        MemberCouponListDTO dto = toMemberDTOSafe(mc); // 你原本的安全轉換

        // ★ 核心：用「主券序號或券ID」 + 「這筆 memberCouponId」組顯示序號
        Coupon c = mc.getCoupon();
        String base = (c.getSerialNumber() != null && !c.getSerialNumber().isBlank())
                ? c.getSerialNumber()
                : "C" + c.getCouponId();
        String displaySerial = base + "-" + mc.getMemberCouponId();

        // 不改 DB，也不必改 Entity，只要在 DTO 回傳這個「顯示用序號」
        dto.setSerialNumber(displaySerial); // 或新增 dto.setDisplaySerial(displaySerial)

        return dto;
    }



//    private MemberCoupon findExisting(Integer memberId, Integer couponId) {
//        List<Integer> ids = new ArrayList<Integer>();
//        ids.add(couponId);
//        List<MemberCoupon> list = memberCouponRepository.findByMember_MemberIdAndCoupon_CouponIdIn(memberId, ids);
//        if (list == null || list.isEmpty()) return null;
//        return list.get(0);
//    }

    private boolean isEnabled(String status) {
        if (status == null) return false;
        String s = status.trim();
        return "1".equals(s) || "ACTIVE".equalsIgnoreCase(s) || "啟用".equals(s);
    }
    
    /** 回傳 true 表示該會員可領這張券 */
    // public boolean canClaim(Integer memberId, Integer couponId) {
    //     Coupon c = couponRepository.findById(couponId)
    //             .orElseThrow(() -> new RuntimeException("Coupon not found: " + couponId));

    //     // 取得該活動的所有資格條件
    //     var rules = eligibilityRepository.findByEvent(c.getEvent());

    //     // 若活動沒有任何「有等級要求」的規則 ⇒ 視為不限 ⇒ 可領
    //     boolean anyLevelRule = rules.stream().anyMatch(r -> r.getMemberLevel() != null);
    //     if (!anyLevelRule) return true;

    //     int userRank = levelResolver.resolveCurrentRank(memberId); // 無紀錄 => 0（不限）
    //     // 只要存在一條規則：memberLevel.rank <= userRank 或規則為 NULL（不限），即可領
    //     return rules.stream().anyMatch(r ->
    //             r.getMemberLevel() == null || // 不限
    //             Optional.ofNullable(r.getMemberLevel().getRank()).orElse(0) <= userRank
    //     );
    // }
    
    private CouponListItemDTO toMarketDTO(Coupon c, long myClaimCount) {
        CouponListItemDTO dto = new CouponListItemDTO();
        
        // --- 將 Coupon Entity 的所有欄位複製到 DTO ---
        dto.setCouponId(c.getCouponId());
        dto.setCouponName(c.getCouponName());
        dto.setCouponDescription(c.getCouponDescription());
        dto.setDiscountAmount(c.getDiscountAmount());
        dto.setMinimumSpend(c.getMinimumSpend());
        dto.setQuantity(c.getQuantity());
        dto.setRedeemableTimes(c.getRedeemableTimes());
        
        if (c.getCouponCategory() != null) {
            dto.setCouponCategory(c.getCouponCategory().getCouponCategoryName());
        }
        
        if (c.getCouponImage() != null) {
            dto.setCouponImageBase64(Base64.getEncoder().encodeToString(c.getCouponImage()));
        }
        
        // --- 設定我們新加入的欄位 ---
        dto.setMyClaimCount(myClaimCount);
        dto.setStatus(myClaimCount > 0 ? "已領取" : "未領取");
        
        return dto;
    }
    
}
