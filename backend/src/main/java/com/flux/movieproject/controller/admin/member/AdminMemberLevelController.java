package com.flux.movieproject.controller.admin.member;

import com.flux.movieproject.model.dto.member.MemberLevelDTO;
import com.flux.movieproject.service.member.MemberLevelService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/member-levels")
public class AdminMemberLevelController {

	private final MemberLevelService memberLevelService;

	public AdminMemberLevelController(MemberLevelService memberLevelService) {
		this.memberLevelService = memberLevelService;
	}

	@GetMapping
	public ResponseEntity<List<MemberLevelDTO>> getAllMemberLevels() {
		List<MemberLevelDTO> levels = memberLevelService.getAllLevels();
		return ResponseEntity.ok(levels);
	}

	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<MemberLevelDTO> createMemberLevel(@RequestPart MemberLevelDTO memberLevelDTO,
			@RequestPart(required = false) MultipartFile levelIcon) {
		MemberLevelDTO createdLevel = memberLevelService.createLevel(memberLevelDTO, levelIcon);
		return ResponseEntity.ok(createdLevel);
	}

	@PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<MemberLevelDTO> updateMemberLevel(@PathVariable Integer id,
			@RequestPart MemberLevelDTO memberLevelDTO, @RequestPart(required = false) MultipartFile levelIcon) {
		MemberLevelDTO updatedLevel = memberLevelService.updateLevel(id, memberLevelDTO, levelIcon);
		return ResponseEntity.ok(updatedLevel);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMemberLevel(@PathVariable Integer id) {
		memberLevelService.deleteLevel(id);
		return ResponseEntity.noContent().build();
	}
}