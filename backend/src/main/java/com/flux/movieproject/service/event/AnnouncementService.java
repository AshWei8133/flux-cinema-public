package com.flux.movieproject.service.event;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.dto.event.AnnouncementDTO;
import com.flux.movieproject.model.dto.event.AnnouncementListResponse;
import com.flux.movieproject.model.entity.event.Announcement;
import com.flux.movieproject.repository.event.AnnouncementRepository;
import com.flux.movieproject.utils.PictureConverter;

@Service
@Transactional(readOnly = true) // 預設所有方法都是只讀事務
public class AnnouncementService {

	@Autowired
	private AnnouncementRepository announcementRepository;

	/**
	 * 將 Announcement 實體轉換為 AnnouncementDTO 的輔助方法。
	 */
	private AnnouncementDTO convertToDTO(Announcement announcement) {
		String base64Image = null;
		byte[] imageBytes = announcement.getAnnouncementImage();
		if (imageBytes != null && imageBytes.length > 0) {
		    base64Image = Base64.getEncoder().encodeToString(imageBytes);
		}

		return new AnnouncementDTO(
				announcement.getAnnouncementId(), 
				announcement.getPublishDate(),
				announcement.getTitle(), 
				announcement.getContent(),
				base64Image
				);
	}

	/**
     * 回傳所有公告的 DTO 列表，支援標題模糊查詢、分頁與排序。
     * @param title 可選參數，用於標題的模糊查詢。
     * @param page 頁碼 (從 0 開始)。
     * @param size 每頁顯示的數量。
     * @param sortBy 排序欄位，預設為 'publishDate'。
     * @param sortOrder 排序順序 ('asc' 或 'desc')，預設為 'desc'。
     * @return 包含公告列表和總數的 AnnouncementListResponse 物件。
     */
	public AnnouncementListResponse findAllAnnouncementsWithDetails(String title, int page, int size, String sortBy, String sortOrder) {
	    
	    System.out.println("--- 開始除錯: findAllAnnouncementsWithDetails ---");
	    System.out.println("接收到的 title 參數: '" + (title != null ? title : "null") + "'");
	    System.out.println("接收到的 page 參數: " + page);
	    System.out.println("接收到的 size 參數: " + size);
	    System.out.println("接收到的 sortBy 參數: '" + (sortBy != null ? sortBy : "null") + "'");
	    System.out.println("接收到的 sortOrder 參數: '" + (sortOrder != null ? sortOrder : "null") + "'");

	    // 處理參數的預設值
	    String finalSortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "publishDate";
	    String finalSortOrder = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

	    // 建立排序物件
	    Sort sort = Sort.by(finalSortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, finalSortBy);
        
        // --- 修正點：創建 Pageable 物件 ---
        Pageable pageable = PageRequest.of(page, size, sort);
        // --- 修正結束 ---

	    Page<Announcement> announcementPage; // 將返回類型改為 Page
	    if (title != null && !title.trim().isEmpty()) {
            // --- 修正點：使用帶有 Pageable 的查詢方法 ---
	        announcementPage = announcementRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);
            // --- 修正結束 ---
	    } else {
            // --- 修正點：使用帶有 Pageable 的查詢方法 ---
	        announcementPage = announcementRepository.findAll(pageable);
            // --- 修正結束 ---
	    }
	    
	    // 從 Page 物件中獲取總數和內容
	    long totalCount = announcementPage.getTotalElements();
	    List<Announcement> announcements = announcementPage.getContent();

	    // 將實體列表轉換為 DTO 列表
	    List<AnnouncementDTO> announcementDTOs = announcements.stream()
	            .map(this::convertToDTO)
	            .collect(Collectors.toList());
	    
	    return new AnnouncementListResponse(totalCount, announcementDTOs);
	}

    // 移除重複的 getAllAnnouncementsWithDateAndTitle 方法，因為其功能已被 findAllAnnouncementsWithDetails 涵蓋
	// public AnnouncementListResponse getAllAnnouncementsWithDateAndTitle(String title) {
    //     // ... 之前的實現 ...
	// }

	public Optional<Announcement> findById(Integer announcementId) {
		return announcementRepository.findById(announcementId);
	}

	@Transactional
	public Announcement addAnnouncement(Announcement announcement) {
		if (announcement.getPublishDate() == null) {
		    announcement.setPublishDate(LocalDate.now());
		}

		if (announcement.getBase64ImageString() != null && !announcement.getBase64ImageString().isEmpty()) {
		    byte[] imageBytes = PictureConverter.convertBase64ToBytes(announcement.getBase64ImageString());
		    announcement.setAnnouncementImage(imageBytes);
		} else {
		    announcement.setAnnouncementImage(null); // 如果沒有提供圖片，則設定為 null
		}
		
		announcement.setAnnouncementId(null); // 確保新增時 ID 為 null，由資料庫生成
		return announcementRepository.save(announcement);
	}

	@Transactional
	public Announcement updateAnnouncement(Integer announcementId, AnnouncementDTO updateDTO) {
	    Optional<Announcement> existingAnnouncementOptional = announcementRepository.findById(announcementId);
	    if (existingAnnouncementOptional.isEmpty()) {
	    	// 執行期例外,程式在執行期間發生的「意外狀況」或「程式邏輯錯誤」。
	        throw new RuntimeException("Announcement not found with id: " + announcementId);
	    }
	    Announcement existingAnnouncement = existingAnnouncementOptional.get();

	    existingAnnouncement.setTitle(updateDTO.title());
	    existingAnnouncement.setContent(updateDTO.content());
	    existingAnnouncement.setPublishDate(updateDTO.publishDate());

	    String base64ImageString = updateDTO.base64ImageString(); 

	    if (base64ImageString == null || base64ImageString.isEmpty()) { 
	        existingAnnouncement.setAnnouncementImage(null);
	    } else {
	        try {
	            byte[] imageBytes = PictureConverter.convertBase64ToBytes(base64ImageString);
	            existingAnnouncement.setAnnouncementImage(imageBytes);
	        } catch (IllegalArgumentException e) {
	            throw new RuntimeException("無效的 Base64 圖片字串！", e);
	        }
	    }

	    return announcementRepository.save(existingAnnouncement);
	}

	@Transactional
	public void deleteById(Integer announcementId) {
        if (!announcementRepository.existsById(announcementId)) {
            throw new RuntimeException("找不到 ID 為 " + announcementId + " 的公告，無法刪除。");
        }
		announcementRepository.deleteById(announcementId);
	}

	public Optional<byte[]> getImageById(Integer announcementId) {
		Optional<Announcement> announcementOptional = announcementRepository.findById(announcementId);
		if (announcementOptional.isPresent()) {
		    return Optional.ofNullable(announcementOptional.get().getAnnouncementImage());
		}
		return Optional.empty();
	}
	
	public Page<Announcement> findPublic(Pageable pageable, String keyword) {
	    return announcementRepository.searchByKeyword(keyword, pageable);
	}


}
