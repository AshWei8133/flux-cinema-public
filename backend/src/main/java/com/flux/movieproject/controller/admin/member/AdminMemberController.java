package com.flux.movieproject.controller.admin.member;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.flux.movieproject.model.dto.member.MemberDTO;
import com.flux.movieproject.model.dto.member.MemberRequestDTO;
import com.flux.movieproject.service.member.MemberService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

	@Autowired
	private MemberService memberService;

	@GetMapping
	public ResponseEntity<List<MemberDTO>> memberFindAll() {
		List<MemberDTO> memberList = memberService.getAllMembers();

		return ResponseEntity.ok(memberList);
	}

	@GetMapping("/{id}")
	public ResponseEntity<MemberDTO> memberFindById(@PathVariable Integer id) {
		return ResponseEntity.ok(memberService.getMemberById(id));
	}

	@GetMapping("/photo/{id}")
	public ResponseEntity<byte[]> memberFindPhoto(@PathVariable Integer id) {
		byte[] photo = memberService.getMemberPhoto(id);

		// 檢查圖片是否存在
		if (photo != null && photo.length > 0) {
			// 如果圖片存在（非空，長度大於 0），回傳 HTTP 200 OK，設定 Content-Type: image/jpeg → 告訴前端這是圖片
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(photo);
			// 回傳圖片資料
		}

		// 圖片為 null 或長度為 0 → 回傳 HTTP 404 Not Found
		return ResponseEntity.notFound().build();
	}

	// consumes:指定此 POST 方法只接受 multipart/form-data 型態的請求，也就是表單可以同時包含文字欄位和檔案上傳。
	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<MemberDTO> addMember(@RequestPart("member") MemberRequestDTO request,
			@RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
		return ResponseEntity.ok(memberService.createMember(request, file));
	}

	@PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<MemberDTO> modifyMember(@PathVariable Integer id,
			@RequestPart("member") MemberRequestDTO request,
			@RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
		return ResponseEntity.ok(memberService.updateMember(id, request, file));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMember(@PathVariable Integer id) {
		memberService.deleteMember(id);
		return ResponseEntity.noContent().build(); // 回傳 204 No Content 表示成功
	}
}
