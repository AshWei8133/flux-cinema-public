package com.flux.movieproject.service.event;

import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.dto.event.coupon.CouponDTO;
import com.flux.movieproject.model.entity.event.Coupon;
import com.flux.movieproject.model.entity.event.CouponCategory;
import com.flux.movieproject.model.entity.event.Event;
import com.flux.movieproject.model.entity.event.EventEligibility;
import com.flux.movieproject.model.entity.member.MemberLevel;
import com.flux.movieproject.model.entity.movie.Movie;
import com.flux.movieproject.model.entity.theater.MovieSession;
import com.flux.movieproject.repository.event.CouponCategoryRepository;
import com.flux.movieproject.repository.event.CouponRepository;
import com.flux.movieproject.repository.event.EventEligibilityRepository;
import com.flux.movieproject.repository.event.EventRepository;
import com.flux.movieproject.repository.member.MemberLevelRepository;
import com.flux.movieproject.repository.movie.MovieRepository;
import com.flux.movieproject.repository.moviesession.MovieSessionRepository;
import com.flux.movieproject.utils.CouponNumberUtils;

/**
 * 優惠券的核心服務，負責優惠券的 CRUD 操作。
 */
@Service
@Transactional(readOnly = true)
public class CouponService {

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private CouponCategoryRepository couponCategoryRepository;

	@Autowired
	private EventEligibilityRepository eventEligibilityRepository;

	@Autowired
	private MovieRepository movieRepository;
	@Autowired
	private MovieSessionRepository movieSessionRepository;
	@Autowired
	private MemberLevelRepository memberLevelRepository;

	/**
	 * 取得所有優惠券，並支援分頁功能。
	 *
	 * @param pageable 分頁資訊，包含頁碼、每頁大小等。
	 * @return 包含優惠券DTO列表的分頁物件。
	 */
	public Page<CouponDTO> getAllCoupons(Pageable pageable) {
		Page<Coupon> couponPage = couponRepository.findAll(pageable);

		return couponPage.map(this::convertToDTO);
	}

	/**
	 * 根據ID取得單一優惠券，並轉換為DTO。
	 *
	 * @param couponId 優惠券的唯一識別碼。
	 * @return 匹配指定ID的優惠券DTO物件。
	 * @throws RuntimeException 如果找不到此優惠券，則拋出例外。
	 */
	public CouponDTO getCouponById(Integer couponId) {
		Optional<Coupon> optionalCoupon = couponRepository.findById(couponId);
		if (optionalCoupon.isPresent()) {
			return convertToDTO(optionalCoupon.get());
		} else {
			throw new RuntimeException("找不到此優惠券，ID：" + couponId);
		}
	}

	/**
	 * 新增優惠券，接收 DTO 作為參數。
	 *
	 * @param couponDTO 從前端傳來的優惠券資料物件。
	 * @return 成功新增的優惠券實體。
	 */
	@Transactional
	 public Coupon createCoupon(CouponDTO couponDTO) {
	  // 先轉 DTO -> Entity
	  Coupon coupon = convertToEntity(couponDTO);
	  EventEligibility eligibility = convertEligibility(couponDTO);
	  
	  // 新增活動條件
	  EventEligibility savedEligibility = eventEligibilityRepository.save(eligibility);
	  
	  // 設定優惠券給活動
	  coupon.setEventEligibility(savedEligibility);

	  // 先存一次，產生 couponId（因為要用它生成序號）
	  Coupon savedCoupon = couponRepository.save(coupon);


	  // 生成序號（用 utils.encode()）
	  if (savedCoupon.getSerialNumber() == null || savedCoupon.getSerialNumber().isEmpty()) {
	   String serial = CouponNumberUtils.encode(savedCoupon.getCouponId());
	   savedCoupon.setSerialNumber(serial);

	   // 再存一次，更新序號
	   savedCoupon = couponRepository.save(savedCoupon);
	  }

	  return savedCoupon;
	 }

	/**
	 * 輔助方法：將 CouponDTO 轉換為 Coupon 實體 這個方法是解決問題的關鍵
	 */
	private Coupon convertToEntity(CouponDTO dto) {
		Coupon coupon = new Coupon();

		// 根據 DTO 中的 ID，從資料庫中找出關聯實體
		Event event = eventRepository.findById(dto.getEventId())
				.orElseThrow(() -> new RuntimeException("找不到 ID 為 " + dto.getEventId() + " 的活動。"));

		CouponCategory category = couponCategoryRepository.findById(dto.getCouponCategoryId())
				.orElseThrow(() -> new RuntimeException("找不到 ID 為 " + dto.getCouponCategoryId() + " 的優惠券類別。"));

		// 將找到的實體設定給 coupon 物件
		coupon.setEvent(event);
		coupon.setCouponCategory(category);

		// 補充完整：設定其他屬性
		coupon.setCouponName(dto.getCouponName());
		coupon.setCouponDescription(dto.getCouponDescription());
		coupon.setDiscountAmount(dto.getDiscountAmount());
		coupon.setMinimumSpend(dto.getMinimumSpend());
		coupon.setStatus(dto.getStatus());
		coupon.setDiscountType(dto.getDiscountType());
		coupon.setRedeemableTimes(dto.getRedeemableTimes());
		coupon.setQuantity(dto.getQuantity());

		// 如果你的 DTO 包含 Base64 圖片字串，你需要將其轉換回位元組陣列
		if (dto.getCouponImageBase64() != null && !dto.getCouponImageBase64().isEmpty()) {
			try {
				// 假設你已經導入了 java.util.Base64
				byte[] imageBytes = Base64.getDecoder().decode(dto.getCouponImageBase64());
				coupon.setCouponImage(imageBytes);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("無效的 Base64 圖片字串。", e);
			}
		}

		return coupon;
	}

	private EventEligibility convertEligibility(CouponDTO dto) {

		// 創建活動條件物件
		EventEligibility eligibility = new EventEligibility();

		// 取得活動物件
		Event event = eventRepository.findById(dto.getEventId())
				.orElseThrow(() -> new RuntimeException("找不到 ID 為 " + dto.getEventId() + " 的活動。"));

		eligibility.setEvent(event);

		if (dto.getMovieId() == null && dto.getMemberLevelId() == null && dto.getSessionId() == null) {
			throw new RuntimeException("未設定任何活動條件，請重新確認");
		}

		// 判斷是否有 電影 欄位
		if (dto.getMovieId() != null) {
			Optional<Movie> movieOptional = movieRepository.findById(dto.getMovieId());
			if (movieOptional.isPresent()) {
				eligibility.setMovie(movieOptional.get());
			}
		}

		if (dto.getMemberLevelId() != null) {
			// 判斷是否有 會員等級 欄位
			Optional<MemberLevel> memberLevelOptional = memberLevelRepository.findById(dto.getMemberLevelId());
			if (memberLevelOptional.isPresent()) {
				eligibility.setMemberLevel(memberLevelOptional.get());
			}
		}

		if (dto.getSessionId() != null) {
			Optional<MovieSession> movieSessionOptional = movieSessionRepository.findById(dto.getSessionId());
			// 判斷是否有 場次 欄位
			if (movieSessionOptional.isPresent()) {
				eligibility.setSession(movieSessionOptional.get());
			}
		}

		return eligibility;
	}

	/**
	 * 更新現有的優惠券資訊。
	 *
	 * @param couponId      欲更新優惠券的唯一識別碼。
	 * @param couponDetails 包含新資訊的優惠券物件。
	 * @return 更新後完整的優惠券物件。
	 * @throws RuntimeException 如果找不到此優惠券，則拋出例外。
	 */
	@Transactional
	public Coupon updateCoupon(Integer couponId, Coupon couponDetails) {
		// 1. 根據 ID 從資料庫中找到現有的 Coupon 實體
		Coupon existingCoupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new RuntimeException("找不到此優惠券，ID：" + couponId));

		// 2. 將傳入的 couponDetails 資訊更新到現有實體上
		if (couponDetails.getCouponName() != null) {
			existingCoupon.setCouponName(couponDetails.getCouponName());
		}
		if (couponDetails.getCouponDescription() != null) {
			existingCoupon.setCouponDescription(couponDetails.getCouponDescription());
		}

		// 3. 處理關聯實體，如果傳入的 couponDetails 中有更新的類別ID，則需要重新查找並設定
		if (couponDetails.getCouponCategory() != null
				&& couponDetails.getCouponCategory().getCouponCategoryId() != null) {
			CouponCategory category = couponCategoryRepository
					.findById(couponDetails.getCouponCategory().getCouponCategoryId())
					.orElseThrow(() -> new RuntimeException(
							"找不到 ID 為 " + couponDetails.getCouponCategory().getCouponCategoryId() + " 的優惠券類別。"));
			existingCoupon.setCouponCategory(category);
		}

		if (couponDetails.getDiscountAmount() != null) {
			existingCoupon.setDiscountAmount(couponDetails.getDiscountAmount());
		}
		if (couponDetails.getMinimumSpend() != null) {
			existingCoupon.setMinimumSpend(couponDetails.getMinimumSpend());
		}
		if (couponDetails.getStatus() != null) {
			existingCoupon.setStatus(couponDetails.getStatus());
		}
		if (couponDetails.getRedeemableTimes() != null) {
			existingCoupon.setRedeemableTimes(couponDetails.getRedeemableTimes());
		}
		if (couponDetails.getQuantity() != null) {
			existingCoupon.setQuantity(couponDetails.getQuantity());
		}

		// 4. 將更新後的實體儲存回資料庫
		return couponRepository.save(existingCoupon);
	}

	/**
	 * 根據ID刪除指定的優惠券。
	 *
	 * @param couponId 欲刪除優惠券的唯一識別碼。
	 * @throws RuntimeException 如果找不到此優惠券，則拋出例外。
	 */
	@Transactional
	public void deleteCoupon(Integer couponId) {
		if (!couponRepository.existsById(couponId)) {
			throw new RuntimeException("找不到此優惠券，ID：" + couponId);
		}
		couponRepository.deleteById(couponId);
	}

	/**
	 * ✅ 新增：輔助方法，將 Coupon 實體轉換為 CouponDTO。
	 */
	private CouponDTO convertToDTO(Coupon coupon) {
		CouponDTO dto = new CouponDTO();
		dto.setCouponId(coupon.getCouponId());
		dto.setSerialNumber(coupon.getSerialNumber());
		dto.setCouponName(coupon.getCouponName());
		dto.setCouponDescription(coupon.getCouponDescription());
		dto.setDiscountAmount(coupon.getDiscountAmount());
		dto.setMinimumSpend(coupon.getMinimumSpend());
		dto.setStatus(coupon.getStatus());
		dto.setDiscountType(coupon.getDiscountType());
		dto.setRedeemableTimes(coupon.getRedeemableTimes());
		dto.setQuantity(coupon.getQuantity());

		// 處理關聯實體，安全地存取懶加載的屬性
		if (coupon.getEvent() != null) {
			dto.setEventId(coupon.getEvent().getEventId());
			dto.setEventTitle(coupon.getEvent().getTitle());
		}
		if (coupon.getCouponCategory() != null) {
			dto.setCouponCategoryId(coupon.getCouponCategory().getCouponCategoryId());
			dto.setCouponCategoryName(coupon.getCouponCategory().getCouponCategoryName());
		}

		// 假設圖片以 Base64 字串儲存
		if (coupon.getCouponImage() != null && coupon.getCouponImage().length > 0) {
		    String base64 = Base64.getEncoder().encodeToString(coupon.getCouponImage());
		    dto.setCouponImageBase64(base64);
		}

		return dto;
	}
}