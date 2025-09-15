package com.flux.movieproject.controller.fluxapp.member;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.flux.movieproject.model.dto.member.MemberDTO;
import com.flux.movieproject.model.dto.member.MemberProfileUpdateRequestDTO;
import com.flux.movieproject.model.dto.member.PasswordChangeRequestDTO;
import com.flux.movieproject.model.dto.movie.MovieListResponseDTO;
import com.flux.movieproject.model.dto.product.order.ProductOrderDTO;
import com.flux.movieproject.model.dto.ticket.TicketOrderHistoryDTO;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.service.member.MemberService;
import com.flux.movieproject.service.product.ProductOrderService;
import com.flux.movieproject.service.ticket.TicketOrderService;
import com.flux.movieproject.utils.MemberJwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/membercenter")
public class MemberCenterController {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberJwtUtil memberJwtUtil;

	@Autowired
	private TicketOrderService ticketOrderService;

	@Autowired
	private ProductOrderService productOrderService;

	/**
	 * 獲取當前登入會員的個人資料
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/profile")
	public ResponseEntity<?> getMemberProfile(HttpServletRequest request) {
		try {
			// 移除 "Bearer "拿到真正的 JWT Token
			String token = request.getHeader("Authorization").substring(7);
			// 解析 JWT，取得裡面的資訊（claims）
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			String email = claims.getSubject();

			// 用 email 去資料庫查會員資料，轉成 MemberDTO
			MemberDTO member = memberService.getMemberByEmail(email);

			return ResponseEntity.ok(member);
		} catch (Exception e) {
			return ResponseEntity.status(401).body("無效的 Token 或用戶不存在");
		}
	}

	/**
	 * 根據 memberId 獲取會員資料 前端會傳 memberId，後端驗證該 memberId 是否屬於登入會員
	 *
	 * @param memberId 前端傳過來的會員ID
	 * @param request  用來拿 JWT Token 驗證身份
	 * @return MemberDTO 或錯誤訊息
	 */
	@GetMapping("/profile/by-id")
	public ResponseEntity<?> getMemberProfileById(@RequestParam Integer memberId, HttpServletRequest request) {

		try {
			// 1. 取得並解析 JWT Token
			String authHeader = request.getHeader("Authorization");
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(401).body("缺少或無效的 Token");
			}

			String token = authHeader.substring(7);
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			String emailFromToken = claims.getSubject();

			// 2. 根據 memberId 取得會員實體
			Member member = memberService.getMemberEntityById(memberId);

			// 3. 驗證 memberId 對應的會員 email 是否與 Token email 相符
			if (!member.getEmail().equals(emailFromToken)) {
				return ResponseEntity.status(403).body("無權存取其他會員資料");
			}

			// 4. 回傳 DTO
			MemberDTO memberDto = memberService.getMemberById(memberId);
			return ResponseEntity.ok(memberDto);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(401).body("無效的 Token 或會員不存在");
		}
	}

	/**
	 * 更新當前登入會員的個人資料
	 * 
	 * @param requestDTO
	 * @param request
	 * @return
	 */
	@PutMapping("/profile")
	public ResponseEntity<?> updateMemberProfile(@RequestBody MemberProfileUpdateRequestDTO requestDTO,
			HttpServletRequest request) {
		try {
			// 移除 "Bearer "拿到真正的 JWT Token
			String token = request.getHeader("Authorization").substring(7);
			// 解析 JWT，取得裡面的資訊（claims）
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			String email = claims.getSubject();

			// 透過 email 取得會員實體，以便獲取 memberId
			Member member = memberService.getMemberEntityByEmail(email);
			// 呼叫 service 更新資料
			MemberDTO updatedMember = memberService.updateMemberProfile(member.getMemberId(), requestDTO);

			return ResponseEntity.ok(updatedMember);
		} catch (Exception e) {
			return ResponseEntity.status(401).body("無效的 Token 或更新失敗");
		}
	}

	/**
	 * 變更當前登入會員的密碼
	 * 
	 * @param requestDTO
	 * @param request
	 * @return
	 */
	@PutMapping("/password")
	public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequestDTO requestDTO,
			HttpServletRequest request) {
		try {
			// 移除 "Bearer "拿到真正的 JWT Token
			String token = request.getHeader("Authorization").substring(7);
			// 解析 JWT，取得裡面的資訊（claims）
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			String email = claims.getSubject();

			// 透過 email 取得會員實體，以便獲取 memberId
			Member member = memberService.getMemberEntityByEmail(email);

			memberService.changePassword(member.getMemberId(), requestDTO.getOldPassword(),
					requestDTO.getNewPassword());

			return ResponseEntity.ok("密碼變更成功");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(400).body(e.getMessage()); // 舊密碼不正確等
		} catch (Exception e) {
			return ResponseEntity.status(401).body("無效的 Token 或密碼變更失敗");
		}
	}

	@PutMapping("/profile/photo")
	public ResponseEntity<?> updateProfilePhoto(@RequestParam MultipartFile file, HttpServletRequest request) {
		try {
			// 移除 "Bearer "拿到真正的 JWT Token
			String token = request.getHeader("Authorization").substring(7);
			// 解析 JWT，取得裡面的資訊（claims）
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			String email = claims.getSubject();

			// 透過 email 取得會員實體，以便獲取 memberId
			Member member = memberService.getMemberEntityByEmail(email);

			MemberDTO updatedMember = memberService.updateProfilePhoto(member.getMemberId(), file);

			return ResponseEntity.ok(updatedMember);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(400).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(401).body("無效的 Token 或更新失敗");
		}
	}

	/**
	 * 獲取當前登入會員的訂票紀錄
	 * 
	 * @param
	 * @return 訂票紀錄列表
	 */
	@GetMapping("/ticket-orders")
	public ResponseEntity<?> getTicketOrderHistory(HttpServletRequest request) {
		try {
			String authHeader = request.getHeader("Authorization");
			if (authHeader == null || !authHeader.startsWith("Bearer")) {
				return ResponseEntity.status(401).body("Missing or invalid Bearer token.");
			}
			String token = authHeader.substring(7);
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			String email = claims.getSubject();

			Member member = memberService.getMemberEntityByEmail(email);
			List<TicketOrderHistoryDTO> history = ticketOrderService
					.getTicketOrderHistoryByMemberId(member.getMemberId());
			return ResponseEntity.ok(history);
		} catch (Exception e) {
			return ResponseEntity.status(401).body("Invalid token or user not found.");
		}
	}

	/**
	 * 獲取當前登入會員的商品訂單歷史紀錄
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/orders/products")
	public ResponseEntity<?> getProductOrders(HttpServletRequest request) {
		try {
			// 移除 "Bearer "拿到真正的 JWT Token
			String token = request.getHeader("Authorization").substring(7);
			// 解析 JWT，取得裡面的資訊（claims）
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			System.out.println(claims);
			String email = claims.getSubject();
			// 透過 email 取得會員實體，以便獲取 memberId
			Member member = memberService.getMemberEntityByEmail(email);
			List<ProductOrderDTO> productOrders = productOrderService.findAllOrdersByMemberId(member.getMemberId());

			return ResponseEntity.ok(productOrders);
		} catch (Exception e) {
			return ResponseEntity.status(401).body("無效的 Token 或無法獲取商品訂單");
		}
	}

	@PutMapping("/orders/{orderId}/cancel")
	public ResponseEntity<?> cancelProductOrder(@PathVariable Integer orderId, HttpServletRequest request) {
		try {
			String email = getEmailFromToken(request);
			Member member = memberService.getMemberEntityByEmail(email);
			ProductOrderDTO cancelledOrder = productOrderService.cancelMemberOrder(orderId, member.getMemberId());

			return ResponseEntity.ok(cancelledOrder);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(404).body(e.getMessage());
		} catch (SecurityException e) {
			return ResponseEntity.status(403).body(e.getMessage());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(400).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("取消訂單時發生未知錯誤");
		}
	}

	// =====================收藏電影====================================================================================================
	/**
	 * 新增一部電影到當前登入會員的收藏列表。
	 */
	@PostMapping("/favorites/{movieId}")
	public ResponseEntity<String> addFavorite(HttpServletRequest request, @PathVariable Integer movieId) {
		try {
			// 透過我們下面寫好的輔助方法，從 Token 中解析出 email
			String email = getEmailFromToken(request);
			// 呼叫 Service 層執行新增邏輯
			memberService.addMovieToFavorites(email, movieId);
			return ResponseEntity.ok("電影已成功加入收藏。");
		} catch (EntityNotFoundException e) {
			// 如果 Service 找不到會員或電影，就回傳 404
			return ResponseEntity.status(404).body(e.getMessage());
		} catch (Exception e) {
			// 處理 Token 無效或其他所有錯誤
			return ResponseEntity.status(500).body("新增收藏時發生錯誤：" + e.getMessage());
		}
	}

	/**
	 * 從當前登入會員的收藏列表中移除一部電影。
	 */
	@DeleteMapping("/favorites/{movieId}")
	public ResponseEntity<String> removeFavorite(HttpServletRequest request, @PathVariable Integer movieId) {
		try {
			String email = getEmailFromToken(request);
			memberService.removeMovieFromFavorites(email, movieId);
			return ResponseEntity.ok("電影已成功從收藏中移除。");
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(404).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("移除收藏時發生錯誤：" + e.getMessage());
		}
	}

	/**
	 * 獲取當前登入會員收藏的所有電影 ID 列表。
	 */
	@GetMapping("/favorites/ids")
	public ResponseEntity<?> getFavoriteMovieIds(HttpServletRequest request) {
		try {
			String email = getEmailFromToken(request);
			Set<Integer> favoriteIds = memberService.getFavoriteMovieIds(email);
			return ResponseEntity.ok(favoriteIds);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(404).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("伺服器內部錯誤，無法獲取收藏列表。");
		}
	}

	/**
	 * 獲取當前登入會員收藏的所有電影的完整資訊列表。
	 */
	@GetMapping("/favorites")
	public ResponseEntity<?> getFavoriteMovies(HttpServletRequest request) {
		try {
			String email = getEmailFromToken(request);
			List<MovieListResponseDTO> favorites = memberService.getFavoriteMovies(email);
			return ResponseEntity.ok(favorites);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(404).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("獲取完整收藏列表時發生錯誤：" + e.getMessage());
		}
	}

	/**
	 * 輔助方法，用來從請求中解析出 Email，讓程式碼更乾淨
	 */
	private String getEmailFromToken(HttpServletRequest request) throws Exception {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new Exception("缺少或無效的 Token");
		}
		String token = authHeader.substring(7);
		JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
		return claims.getSubject();
	}

}
