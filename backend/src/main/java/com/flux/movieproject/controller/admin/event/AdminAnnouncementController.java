package com.flux.movieproject.controller.admin.event;

import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.event.AnnouncementDTO;
import com.flux.movieproject.model.dto.event.AnnouncementListResponse;
import com.flux.movieproject.model.entity.event.Announcement;
import com.flux.movieproject.service.event.AnnouncementService;

@RestController
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {

	@Autowired
	private AnnouncementService announcementService;

	@GetMapping
    public ResponseEntity<AnnouncementListResponse> getAllAnnouncements(
            @RequestParam(required = false, defaultValue = "") String title,
            // --- 修正點：新增 page 和 size 參數 ---
            @RequestParam(defaultValue = "0") int page, // 頁碼，預設從 0 開始
            @RequestParam(defaultValue = "10") int size, // 每頁大小，預設 10 筆
            // --- 修正結束 ---
            @RequestParam(defaultValue = "publishDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        // 將所有參數傳遞給 Service 層
        // --- 修正點：傳遞 page 和 size 參數給 Service ---
        AnnouncementListResponse response = announcementService.findAllAnnouncementsWithDetails(title, page, size, sortBy, sortOrder);
        // --- 修正結束 ---
        return ResponseEntity.ok(response);
    }

	/**
	 * 獲取單一公告的完整資訊。
	 * * @param announcementId 公告 ID
	 * @return 完整的 Announcement 實體，如果找不到則回傳 404
	 */
	@GetMapping("/{announcementId}")
	public ResponseEntity<AnnouncementDTO> getAnnouncementById(@PathVariable("announcementId") Integer announcementId) {
		Optional<Announcement> announcementOptional = announcementService.findById(announcementId);

		// 使用 Optional 的 map 方法進行轉換，如果 Optional 為空則直接回傳 notFound
		return announcementOptional.map(this::convertToDTO) // 將 Announcement 實體轉換為 AnnouncementDTO
				.map(ResponseEntity::ok) // 如果轉換成功，則回傳 200 OK
				.orElse(ResponseEntity.notFound().build()); // 如果找不到，則回傳 404

	}

	/**
	 * 新增一個公告。 
	 * 現在接受 Announcement 實體，但回傳新增後的 AnnouncementDTO，並回傳 201 Created 狀態碼。
	 * * @param announcement 要新增的公告實體（包含 base64ImageString）
	 * @return 新增後的 AnnouncementDTO，並回傳 201 Created 狀態碼
	 */
	@PostMapping
	public ResponseEntity<AnnouncementDTO> addAnnouncement(@RequestBody Announcement announcement) {
		Announcement newAnnouncement = announcementService.addAnnouncement(announcement);
		// 新增成功後回傳 201 Created，將實體轉換為 DTO
		return new ResponseEntity<>(convertToDTO(newAnnouncement), HttpStatus.CREATED);
	}

	/**
	 * 更新一個公告。 
	 * 現在接受 Announcement 實體，但回傳更新後的 AnnouncementDTO，如果找不到則回傳 404。
	 *
	 * @param announcementId  公告 ID
	 * @param announcementDetails 包含更新內容的公告實體（包含 base64ImageString）
	 * @return 更新後的 AnnouncementDTO，如果找不到則回傳 404
	 */
	@PutMapping("/{announcementId}") 
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable("announcementId") Integer announcementId, 
            @RequestBody AnnouncementDTO updateDTO) { // <-- 接收前端傳送的 JSON 數據
        try {
            // 調用服務層的方法來更新公告
            Announcement updatedAnnouncement = announcementService.updateAnnouncement(announcementId, updateDTO);
            return ResponseEntity.ok(updatedAnnouncement); // 返回更新後的公告
        } catch (RuntimeException e) {
            // 如果找不到公告，返回 404
            return ResponseEntity.notFound().build();
        }
    }

	/**
	 * 根據 ID 刪除一個公告。
	 *
	 * @param announcementId 公告 ID
	 * @return 如果刪除成功則回傳 204 No Content，如果找不到則回傳 404
	 */
	@DeleteMapping("/{announcementId}")
	public ResponseEntity<Void> deleteAnnouncement(@PathVariable("announcementId") Integer announcementId) {
		// 先確認公告是否存在
		Optional<Announcement> existingAnnouncement = announcementService.findById(announcementId);

		if (existingAnnouncement.isPresent()) {
			announcementService.deleteById(announcementId);
			return ResponseEntity.noContent().build(); // 204 No Content
		} else {
			return ResponseEntity.notFound().build(); // 404 Not Found
		}
	}

	/**
	 * 私有輔助方法：將 Announcement 實體轉換為 AnnouncementDTO 資料傳輸物件。 這裡為了方便，直接在 Controller
	 * 中進行轉換，實際專案中可以考慮放在 Service 層或專門的 Mapper 類中。
	 *
	 * @param announcement 要轉換的 Announcement 實體。
	 * @return 轉換後的 AnnouncementDTO 物件。
	 */
	private AnnouncementDTO convertToDTO(Announcement announcement) {
		// 處理圖片 Base64 轉換，這裡假設 getAnnouncementImage() 返回的是 byte[]
		String base64Image = Optional.ofNullable(announcement.getAnnouncementImage()).filter(bytes -> bytes.length > 0) // 確保字節數組不為空
				.map(Base64.getEncoder()::encodeToString) // 將 byte[] 轉換為 Base64 字串
				.orElse(null); // 如果為空則設定為 null

		// 根據 AnnouncementDTO Record 的定義創建實例
		return new AnnouncementDTO(announcement.getAnnouncementId(), announcement.getPublishDate(),
				announcement.getTitle(), announcement.getContent(), // 假設 AnnouncementDTO 包含 content
				base64Image);
	}
}
