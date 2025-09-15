package com.flux.movieproject.service.member;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.flux.movieproject.model.entity.product.Cart;
import com.flux.movieproject.repository.product.CartRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.flux.movieproject.model.dto.member.GoogleLoginRequestDTO;
import com.flux.movieproject.model.dto.member.MemberDTO;
import com.flux.movieproject.model.dto.member.MemberLoginRequestDTO;
import com.flux.movieproject.model.dto.member.MemberProfileUpdateRequestDTO;
import com.flux.movieproject.model.dto.member.MemberRequestDTO;
import com.flux.movieproject.model.dto.movie.MovieListResponseDTO;
import com.flux.movieproject.model.entity.member.FavoriteMovie;
import com.flux.movieproject.model.entity.member.FavoriteMovieId;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.model.entity.member.MemberLevel;
import com.flux.movieproject.model.entity.member.MemberLevelRecord;
import com.flux.movieproject.model.entity.movie.Movie;
import com.flux.movieproject.repository.member.FavoriteMovieRepository;
import com.flux.movieproject.repository.member.MemberLevelRecordRepository;
import com.flux.movieproject.repository.member.MemberLevelRepository;
import com.flux.movieproject.repository.member.MemberRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.flux.movieproject.repository.movie.MovieRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class MemberService {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FavoriteMovieRepository favoriteMovieRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private MemberLevelRepository memberLevelRepository;

	@Autowired
	private MemberLevelRecordRepository memberLevelRecordRepository;

	// 注入 SecurityConfig 中設定的密碼加密器 Bean
	@Autowired
	private PasswordEncoder passwordEncoder;

	// Google Client ID，從前端複製過來
	private static final String GOOGLE_CLIENT_ID = "721650577954-3d28d0hu4r31lutmi1op5j5tloougota.apps.googleusercontent.com";

	/**
	 * 將 Entity 轉換為 DTO
	 * 
	 * @return
	 */
	private MemberDTO convertToDto(Member member) {
		MemberDTO dto = new MemberDTO();
		BeanUtils.copyProperties(member, dto); // 把 member 的屬性值複製到 dto

		if (member.getProfilePhoto() != null && member.getProfilePhoto().length > 0) {
			String base64Photo = Base64.getEncoder().encodeToString(member.getProfilePhoto());

			dto.setProfilePhoto(base64Photo);
		}

		return dto;
	}

	/**
	 * 取得所有會員
	 * 
	 */
	public List<MemberDTO> getAllMembers() {
		return memberRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
	}

	/**
	 * 根據 ID 取得單一會員
	 * 
	 * @param id
	 * @return
	 */
	public MemberDTO getMemberById(Integer id) {
		Member member = memberRepository.findById(id).orElseThrow(() -> new RuntimeException("找不到會員ID:" + id));
		return convertToDto(member); // convertToDto(member) 是把 Member 物件轉成 MemberDTO
	}

	/**
	 * 取得大頭貼的方法
	 * 
	 * @param id
	 * @return
	 */
	public byte[] getMemberPhoto(Integer id) {
		Member member = memberRepository.findById(id).orElseThrow(() -> new RuntimeException("找不到會員ID:" + id));
		return member.getProfilePhoto();
	}

	/**
	 * 新增會員
	 * 
	 * @return
	 * @throws IOException
	 */
	public MemberDTO createMember(MemberRequestDTO request, MultipartFile file) throws IOException {

		// 先檢查 Email 是否已存在
		if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new IllegalArgumentException("此電子郵件已被註冊");
		}

		Member member = new Member();
		BeanUtils.copyProperties(request, member, "password", "loginMethod", "thirdPartyLoginId", "profilePhotoBase64");

		String loginMethod = request.getLoginMethod();
		if ("google".equalsIgnoreCase(request.getLoginMethod())) {

			member.setLoginMethod(loginMethod.toLowerCase());
			member.setThirdPartyLoginId(request.getThirdPartyLoginId());

			// 對於第三方登入，我們不依賴用戶提供的密碼，而是生成一個安全的隨機密碼
			// 產生一個隨機字串當作臨時密碼（避免密碼欄位為空）
			String randomPassword = UUID.randomUUID().toString();

			// 直接儲存明碼
//			member.setPassword(randPassword);

			// 將臨時密碼做加密後再存（即便不會用它登入，也要避免明碼）這部份等之後再研究
			member.setPassword(passwordEncoder.encode(randomPassword));

		} else {

			// 用註冊的方式
			if (request.getPassword() == null || request.getPassword().isEmpty()) {
				throw new IllegalArgumentException("註冊需要提供密碼");
			}

			member.setLoginMethod("email");

			// 直接儲存明碼
//			member.setPassword(request.getPassword());

			// 將使用者提供的密碼加密後存入資料庫（不要存明碼）這部份等之後再研究
			member.setPassword(passwordEncoder.encode(request.getPassword()));

		}

		// 3. 處理 Base64 頭像
		String base64Photo = request.getProfilePhotoBase64();
		if (base64Photo != null && !base64Photo.isEmpty()) {
			try {
				// 前端傳來的 Base64 字串可能包含 "data:image/png;base64," 前綴，需要移除
				String pureBase64 = base64Photo.substring(base64Photo.indexOf(",") + 1);

				// 將 Base64 字串解碼成 byte[]
				byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
				member.setProfilePhoto(imageBytes);
			} catch (IllegalArgumentException e) {
				// 如果 Base64 字串格式不正確，會拋出例外
				System.err.println("Base64 解碼失敗: " + e.getMessage());
				// 你可以選擇忽略錯誤、或拋出一個自訂的業務例外
				throw new IllegalArgumentException("上傳的頭像格式不正確");
			}
		}

		// 設定註冊時間為現在（以伺服器時間為準）
		member.setRegisterDate(LocalDateTime.now());
		member.setMemberPoints(0);

		// 如果有上傳大頭貼，取出檔案位元組存進資料庫的欄位
		if (file != null && !file.isEmpty()) {
			member.setProfilePhoto(file.getBytes());
		}

		// 呼叫 repository 將 member 寫進資料庫，回傳「實際儲存後」的實體（帶有主鍵等）
		Member saveMember = memberRepository.save(member);

		MemberLevel defaultLevel = memberLevelRepository.findById(1)
				.orElseThrow(() -> new RuntimeException("Default member level with ID 1 not found."));

		MemberLevelRecord levelRecord = new MemberLevelRecord();
		levelRecord.setMemberId(saveMember.getMemberId());
		levelRecord.setMemberLevelId(defaultLevel.getMemberLevelId());
		levelRecord.setStartDate(LocalDateTime.now());
		levelRecord.setEndDate(null);

		memberLevelRecordRepository.save(levelRecord);

		// 新建會員會自動創建一個購物車
		Cart userCart = cartRepository.findByMemberId(member.getMemberId()).orElseGet(() -> {
			Cart newCart = new Cart();
			newCart.setCreatedAt(Instant.now());
			newCart.setUpdatedAt(Instant.now());
			newCart.setMemberId(member.getMemberId());
			return cartRepository.save(newCart);
		});

		// 將實體轉成對外用的 DTO（避免曝露敏感欄位），並回傳
		return convertToDto(saveMember);
	}

	/**
	 * 更新會員
	 * 
	 * @param id
	 * @param request
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public MemberDTO updateMember(Integer id, MemberRequestDTO request, MultipartFile file) throws IOException {
		Member member = memberRepository.findById(id).orElseThrow(() -> new RuntimeException("找不到會員 ID: " + id));

		BeanUtils.copyProperties(request, member, "password", "loginMethod", "thirdPartyLoginId");

		if (request.getPassword() != null && !request.getPassword().isEmpty()) {
			// 將新密碼加密後再存入
			member.setPassword(passwordEncoder.encode(request.getPassword()));
		}

		if (file != null && !file.isEmpty()) {
			member.setProfilePhoto(file.getBytes());
		}

		Member saveMember = memberRepository.save(member);

		return convertToDto(saveMember);
	}

	/**
	 * 刪除會員
	 * 
	 * @param member_id
	 */
	public void deleteMember(Integer id) {
		// 1. 先檢查資料庫中是否存在該會員 ID
		if (!memberRepository.existsById(id)) {
			// 2. 如果不存在，丟出例外
			throw new RuntimeException("找不到會員ID:" + id);
		}
		// 3. 如果存在，刪除該會員
		memberRepository.deleteById(id);
	}

	/**
	 * 會員登入驗證
	 * 
	 * @param loginRequest 包含登入帳號(email)和密碼的請求物件
	 * @return 如果驗證成功，返回該會員的實體；否則返回 null
	 */
	public Member login(MemberLoginRequestDTO loginRequest) {
		// 1. 透過 email 尋找會員
		Optional<Member> memberOptional = memberRepository.findByEmail(loginRequest.getEmail());

		// 2. 檢查會員是否存在
		if (memberOptional.isPresent()) {
			Member member = memberOptional.get();

			// 3. 使用 passwordEncoder.matches() 來比對「前端傳來的原始密碼」與「資料庫儲存的加密密碼」
			if (passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
				// 密碼比對成功，更新最後登入時間
				member.setLastLoginTime(LocalDateTime.now());
				memberRepository.save(member); // 更新資料庫
				return member; // 驗證成功，返回會員物件
			}
		}

		// 4. 會員不存在或密碼錯誤，返回 null
		return null;
	}

	/**
	 * 給後端內部邏輯用，處理會員資料、驗證或修改時使用。
	 * 
	 * @param email
	 * @return
	 */
	public Member getMemberEntityByEmail(String email) {
		return memberRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("找不到會員 Email:" + email));
	}

	/**
	 * 給 API 回應前端用，讓使用者在網頁上看到會員資訊
	 * 
	 * @param email
	 * @return
	 */
	public MemberDTO getMemberByEmail(String email) {
		Member member = getMemberEntityByEmail(email); // 直接複用上面的方法
		return convertToDto(member);
	}

	/**
	 * 更新會員個人資料 (不包含密碼和頭像)
	 * 
	 * @param memberId
	 * @param requestDTO
	 * @return
	 */
	public MemberDTO updateMemberProfile(Integer memberId, MemberProfileUpdateRequestDTO requestDTO) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new RuntimeException("找不到會員 ID: " + memberId));

		BeanUtils.copyProperties(requestDTO, member);

		Member updateMember = memberRepository.save(member);
		return convertToDto(updateMember);
	}

	/**
	 * 變更會員密碼
	 * 
	 * @param memberId
	 * @param oldPassword
	 * @param newPassword
	 */
	public void changePassword(Integer memberId, String oldPassword, String newPassword) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new RuntimeException("找不到會員 ID: " + memberId));

		// 驗證舊密碼
//		if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
//			throw new IllegalArgumentException("舊密碼不正確");
//		}

		// 設定新密碼並加密
		member.setPassword(passwordEncoder.encode(newPassword));
		memberRepository.save(member);
	}

	/**
	 * 更新會員大頭貼
	 * 
	 * @param memberId
	 * @param photoFile
	 * @return 更新後的 MemberDTO
	 * @throws IOException
	 */
	public MemberDTO updateProfilePhoto(Integer memberId, MultipartFile photoFile) throws IOException {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new RuntimeException("找不到會員 ID: " + memberId));

		if (photoFile != null && !photoFile.isEmpty()) {
			member.setProfilePhoto(photoFile.getBytes());
		} else {
			throw new IllegalArgumentException("請提供有效的圖片檔案");
		}

		Member updatedMember = memberRepository.save(member);
		return convertToDto(updatedMember);
	}

	/**
	 * 根據 memberId 取得會員實體
	 * 
	 * @param memberId
	 * @return Member
	 */
	public Member getMemberEntityById(Integer memberId) {
		return memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("找不到會員ID: " + memberId));
	}

	/**
	 * 根據 memberId 取得 DTO
	 * 
	 * @param memberId
	 * @return MemberDTO
	 */
	public MemberDTO getMemberProById(Integer memberId) {
		Member member = getMemberEntityById(memberId);
		return convertToDto(member);
	}

	/**
	 * 驗證電子郵件和電話是否存在
	 * 
	 * @param email
	 * @param phone
	 * @return
	 */
	public boolean verifyEmailAndPhone(String email, String phone) {
		return memberRepository.findByEmailAndPhone(email, phone).isPresent();
	}

	/**
	 * 重設密碼
	 * 
	 * @param email
	 * @param newPassword
	 */
	public void insecureResetPassword(String email, String newPassword) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Email not found: " + email));
		member.setPassword(passwordEncoder.encode(newPassword));
		memberRepository.save(member);
	}

	/**
	 * Google 登入
	 * 
	 * @param loginRequest
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Member googleLogin(GoogleLoginRequestDTO loginRequest) throws GeneralSecurityException, IOException {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
				.setAudience(Collections.singletonList(GOOGLE_CLIENT_ID)).build();

		GoogleIdToken idToken = verifier.verify(loginRequest.getCredential());
		if (idToken != null) {
			Payload payload = idToken.getPayload();

			// 取得 Google 使用者資訊
			String googleId = payload.getSubject();
			String email = payload.getEmail();
			String name = (String) payload.get("name");

			// 1. 優先用 googleId 尋找會員
			Optional<Member> memberOptional = memberRepository.findByThirdPartyLoginId(googleId);

			if (memberOptional.isPresent()) {
				// 如果會員已存在，更新最後登入時間並返回
				Member member = memberOptional.get();
				member.setLastLoginTime(LocalDateTime.now());
				return memberRepository.save(member);
			} else {
				// 2. 如果用 googleId 找不到，再用 email 嘗試尋找
				Optional<Member> emailMemberOptional = memberRepository.findByEmail(email);
				if (emailMemberOptional.isPresent()) {
					// 如果 email 存在，表示使用者可能以前用 email 註冊過
					// 將 googleId 和登入方式綁定到現有帳號
					Member member = emailMemberOptional.get();
					member.setThirdPartyLoginId(googleId);
					member.setLoginMethod("google");
					member.setLastLoginTime(LocalDateTime.now());
					return memberRepository.save(member);
				} else {
					// 3. 如果都不存在，創建新會員
					Member newMember = new Member();

					String username = name;
					if (memberRepository.findByUsername(name).isPresent()) {
						username = name + "_" + googleId.substring(0, 6);
					}
					newMember.setUsername(username);
					newMember.setEmail(email);
					newMember.setThirdPartyLoginId(googleId);
					newMember.setRegisterDate(LocalDateTime.now());
					newMember.setLastLoginTime(LocalDateTime.now());
					newMember.setMemberPoints(0);
					newMember.setLoginMethod("google");

					// 因為 Google 登入時沒有提供 phone 和 gender，我們需要給予預設值以避免資料庫噴錯
					String phonePlaceholder = " ";
//					if (phonePlaceholder.length() > 20) {
//						phonePlaceholder = phonePlaceholder.substring(phonePlaceholder.length() - 20);
//					}
					newMember.setPhone(phonePlaceholder);
					newMember.setGender(" ");

					// 因為資料庫密碼欄位是 NOT NULL，所以生成一個安全的隨機密碼
					String randomPassword = UUID.randomUUID().toString();
					newMember.setPassword(passwordEncoder.encode(randomPassword));

					Member savedGoogleMember = memberRepository.save(newMember);
					MemberLevel defaultLevel = memberLevelRepository.findById(1)
							.orElseThrow(() -> new RuntimeException("Default member level with ID 1 not found."));

					MemberLevelRecord levelRecord = new MemberLevelRecord();
					levelRecord.setMemberId(savedGoogleMember.getMemberId());
					levelRecord.setMemberLevelId(defaultLevel.getMemberLevelId());
					levelRecord.setStartDate(LocalDateTime.now());
					levelRecord.setEndDate(null);

					memberLevelRecordRepository.save(levelRecord);
					return savedGoogleMember;
				}
			}
		} else {
			// 修正 BUG：將 throw 移到正確的位置
			throw new GeneralSecurityException("Invalid Google ID token.");
		}
	}

	// -----------------------------收藏電影------------------------------------------------------------------------------------------------
	private Integer getCurrentUserId(HttpServletRequest request) {
		Object memberIdObj = request.getAttribute("memberId");
		if (memberIdObj instanceof Integer) {
			return (Integer) memberIdObj;
		}
		return null; // 未登入
	}

	// --- 【新增】收藏相關的商業邏輯 ---

	/**
	 * 新增一部電影到指定會員的收藏列表。
	 * 
	 * @param email   會員的 Email
	 * @param movieId 要收藏的電影 ID
	 */
	@Transactional
	public void addMovieToFavorites(String email, Integer movieId) {
		// 1. 根據 Email 找出會員，如果找不到就拋出錯誤
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("找不到 Email 為 " + email + " 的會員"));

		// 2. 根據 movieId 找出電影，如果找不到就拋出錯誤
		Movie movie = movieRepository.findById(movieId)
				.orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + movieId + " 的電影"));

		// 3. 建立一個複合主鍵
		FavoriteMovieId favoriteId = new FavoriteMovieId(member.getMemberId(), movie.getId());

		// 4. 檢查是否已經收藏過，避免重複新增
		if (favoriteMovieRepository.existsById(favoriteId)) {
			// 如果已經存在，可以直接結束，或拋出一個錯誤讓前端知道
			System.out.println("會員 " + email + " 已收藏過電影 ID: " + movieId);
			return;
		}

		// 5. 建立新的 FavoriteMovie 關聯實體
		FavoriteMovie favoriteMovie = new FavoriteMovie();
		favoriteMovie.setId(favoriteId);
		favoriteMovie.setMember(member);
		favoriteMovie.setMovie(movie);
		favoriteMovie.setAddedToFavoritesTime(LocalDateTime.now()); // 記錄當下時間

		// 6. 將新的收藏紀錄存入資料庫
		favoriteMovieRepository.save(favoriteMovie);
	}

	/**
	 * 從指定會員的收藏列表中移除一部電影。
	 * 
	 * @param email   會員的 Email
	 * @param movieId 要移除的電影 ID
	 */
	@Transactional
	public void removeMovieFromFavorites(String email, Integer movieId) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("找不到 Email 為 " + email + " 的會員"));

		FavoriteMovieId favoriteId = new FavoriteMovieId(member.getMemberId(), movieId);

		// 直接根據複合主鍵刪除，這是最有效率的方式
		favoriteMovieRepository.deleteById(favoriteId);
	}

	/**
	 * 獲取指定會員收藏的所有電影 ID。
	 * 
	 * @param email 會員的 Email
	 * @return 一個包含電影 ID 的 Set 集合
	 */
	@Transactional(readOnly = true)
	public Set<Integer> getFavoriteMovieIds(String email) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("找不到 Email 為 " + email + " 的會員"));

		// 呼叫我們在 Repository 中定義好的方法來獲取 ID 列表
		return favoriteMovieRepository.findMovieIdsByMember(member);
	}

	/**
	 * 獲取指定會員收藏的所有電影的完整資訊。
	 * 
	 * @param email 會員的 Email
	 * @return 一個包含電影 DTO 的列表
	 */
	@Transactional(readOnly = true)
	public List<MovieListResponseDTO> getFavoriteMovies(String email) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("找不到 Email 為 " + email + " 的會員"));

		// 【已優化】改用您在 Repository 中定義好的、效率更高的方法
		// 這個方法會一次性地將電影資料也抓取回來，避免 N+1 問題
		List<FavoriteMovie> favorites = favoriteMovieRepository.findByMemberWithMovie(member);

		// 使用 Java Stream API，將每一個 FavoriteMovie 實體，轉換成前端需要的 MovieListResponseDTO
		return favorites.stream().map(favorite -> {
			Movie movie = favorite.getMovie();
			// 這裡假設 MovieListResponseDTO 有一個接收 Movie 物件的建構子
			MovieListResponseDTO dto = new MovieListResponseDTO();
			dto.setId(movie.getId());
			dto.setTitleLocal(movie.getTitleLocal());
			dto.setTitleEnglish(movie.getTitleEnglish());
			dto.setCertification(movie.getCertification());
			dto.setDurationMinutes(movie.getDurationMinutes());
			dto.setOverview(movie.getOverview());
			dto.setPosterImage(movie.getPosterImage());
			// 如果您的 DTO 還有其他欄位，也可以在這裡一併設定
			// dto.setTitleEnglish(movie.getTitleEnglish());
			// dto.setCertification(movie.getCertification());

			return dto;
		}).collect(Collectors.toList());
	}
}
