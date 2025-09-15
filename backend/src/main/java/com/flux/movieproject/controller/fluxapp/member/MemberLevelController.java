package com.flux.movieproject.controller.fluxapp.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.member.MemberLevelStatusDTO;
import com.flux.movieproject.model.entity.member.Member;
import com.flux.movieproject.model.entity.member.MemberLevelRecord;
import com.flux.movieproject.service.member.MemberLevelService;
import com.flux.movieproject.service.member.MemberService;
import com.flux.movieproject.utils.MemberJwtUtil;
import com.nimbusds.jwt.JWTClaimsSet;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/member")
public class MemberLevelController {

	private final MemberLevelService memberLevelService;
	private final MemberService memberService;
	private final MemberJwtUtil memberJwtUtil;

	public MemberLevelController(MemberLevelService memberLevelService, MemberService memberService,
			MemberJwtUtil memberJwtUtil) {
		this.memberLevelService = memberLevelService;
		this.memberService = memberService;
		this.memberJwtUtil = memberJwtUtil;
	}

	/**
	 * 前端會員中心的 API 端點
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/level-status")
	public ResponseEntity<?> getMemberLevelStatus(HttpServletRequest request) {
		try {
			String token = request.getHeader("Authorization").substring(7);
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			String email = claims.getSubject();
			Member member = memberService.getMemberEntityByEmail(email);

			MemberLevelStatusDTO statusDTO = memberLevelService.getMemberLevelStatus(member.getMemberId());
			return ResponseEntity.ok(statusDTO);
		} catch (Exception e) {
			return ResponseEntity.status(401).body("無效的 Token 或用戶不存在:" + e.getMessage());
		}
	}
	
	@PostMapping("/level-up")
	public ResponseEntity<?> updateLevel(HttpServletRequest request) {
		try {
			String token = request.getHeader("Authorization").substring(7);
			JWTClaimsSet claims = memberJwtUtil.getClaimsFromToken(token);
			String email = claims.getSubject();
			Member member = memberService.getMemberEntityByEmail(email);

			memberLevelService.levelUp(member.getMemberId());
			return ResponseEntity.ok("升等判斷完成");
		} catch (Exception e) {
			return ResponseEntity.status(401).body("無效的 Token 或用戶不存在:" + e.getMessage());
		}
	}
}
