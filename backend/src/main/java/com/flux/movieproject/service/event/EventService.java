package com.flux.movieproject.service.event;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.dto.event.EventAdminListDTO;
import com.flux.movieproject.model.dto.event.EventAdminListResponse;
import com.flux.movieproject.model.dto.event.EventDTO;
import com.flux.movieproject.model.dto.event.event.EventListDTO;
import com.flux.movieproject.model.entity.event.Event;
import com.flux.movieproject.model.entity.event.EventCategory;
import com.flux.movieproject.repository.event.EventCategoryRepository;
import com.flux.movieproject.repository.event.EventRepository;
import com.flux.movieproject.utils.PictureConverter;

@Service
@Transactional(readOnly = true)
public class EventService {

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private EventCategoryRepository eventCategoryRepository;

	/**
	 * 將 Event 實體轉換為 EventDTO 的輔助方法。
	 */
	private EventDTO convertToDto(Event event) {
		String base64Image = null;
		byte[] imageBytes = event.getImage();
		if (imageBytes != null && imageBytes.length > 0) {
			base64Image = Base64.getEncoder().encodeToString(imageBytes);
		}

		// 移除從資料庫獲取狀態，改為即時計算
		String status = getEventStatus(event.getStartDate(), event.getEndDate());

		// 從關聯的 EventCategory 物件中獲取 ID 和名稱
		EventCategory category = event.getEventCategory();
		Integer eventCategoryId = (category != null) ? category.getEventCategoryId() : null;
		String eventCategoryName = (category != null) ? category.getEventCategoryName() : null;

		return new EventDTO(event.getEventId(), event.getTitle(), event.getContent(), eventCategoryId,
				eventCategoryName, status, // 使用計算出的狀態
				event.getStartDate(), event.getEndDate(), event.getSessionCount(), base64Image);
	}

	// 新增一個輔助方法，用於在後端計算狀態
	private String getEventStatus(LocalDate startDate, LocalDate endDate) {
		LocalDate now = LocalDate.now();

		// 判斷「即將開始」的邏輯：
		// 如果開始日期在未來一個禮拜內
		if (startDate.isAfter(now) && startDate.isBefore(now.plusWeeks(1).plusDays(1))) {
			return "upcoming";
		}

		// 判斷「進行中」的邏輯：
		// 當前日期在開始日期或之後，且在結束日期或之前
		if (!now.isBefore(startDate) && !now.isAfter(endDate)) {
			return "ongoing";
		}

		// 判斷「已結束」的邏輯：
		if (now.isAfter(endDate)) {
			return "ended";
		}

		return "pending"; // 其他情況（例如太久遠的未來活動）
	}

	/**
	 * 回傳所有活動的 DTO 列表，支援查詢與排序。
	 * 
	 * @param title     可選參數，用於標題的模糊查詢。
	 * @param sortBy    排序欄位，預設為 'startDate'。
	 * @param sortOrder 排序順序 預設為 'desc'。
	 * @return 包含活動列表和總數的 EventListResponse 物件。
	 */
	public EventAdminListResponse findAllEventsWithDetails(String title, String sortBy, String sortOrder) {

		// 處理參數的預設值，避免 NullPointerException
		String finalSortBy = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "startDate";
		String finalSortOrder = (sortOrder != null && !sortOrder.isEmpty()) ? sortOrder : "desc";

		// 建立一個變數來儲存 JPA 需要的、可能包含點(.)路徑的屬性名
		String jpaSortProperty = finalSortBy;

		// 檢查前端傳來的排序欄位是否是關聯物件的欄位
		if ("eventCategoryName".equals(finalSortBy)) {
			// 如果是，將其轉換為 JPA 能夠理解的完整路徑
			jpaSortProperty = "eventCategory.eventCategoryName";
		}
		// 未來如果有其他關聯欄位排序，可以在此處增加 else if 判斷

		// 使用轉換後的 jpaSortProperty 來建立 Sort 物件
		Sort sort = Sort.by(finalSortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
				jpaSortProperty);
		
		String titleParam = (title != null && !title.trim().isEmpty()) ? title.trim() : null;

		// 【核心修改】直接呼叫 Repository 的投影查詢方法
		List<EventAdminListDTO> dtoList = eventRepository.findAllAsAdminListDTO(titleParam, sort);

		// 因為狀態是在 Java 中計算的，我們需要在取回 DTO 後再處理一次
		// 這是一個輕量級的 Java 記憶體操作，非常快
		List<EventAdminListDTO> finalDtoList = dtoList.stream()
				.map(dto -> new EventAdminListDTO(dto.eventId(), dto.title(), dto.content(), dto.eventCategoryId(),
						dto.eventCategoryName(), getEventStatus(dto.startDate(), dto.endDate()), // <-- 在這裡計算正確的狀態
						dto.startDate(), dto.endDate(), dto.sessionCount(), dto.hasImage()))
				.collect(Collectors.toList());

		if (finalDtoList.isEmpty()) {
			return new EventAdminListResponse(0L, new ArrayList<>());
		}

		Long count = (long) finalDtoList.size();
		return new EventAdminListResponse(count, finalDtoList);
	}

	/**
	 * 專門用於將 Event 轉換為 EventAdminListDTO 的輔助方法
	 */
//	private EventAdminListDTO convertToAdminListDto(Event event) {
//		boolean hasImage = event.getImage() != null && event.getImage().length > 0;
//		String status = getEventStatus(event.getStartDate(), event.getEndDate());
//		EventCategory category = event.getEventCategory();
//		Integer eventCategoryId = (category != null) ? category.getEventCategoryId() : null;
//		String eventCategoryName = (category != null) ? category.getEventCategoryName() : null;
//
//		return new EventAdminListDTO(event.getEventId(), event.getTitle(), event.getContent(), eventCategoryId,
//				eventCategoryName, status, event.getStartDate(), event.getEndDate(), event.getSessionCount(), hasImage);
//	}

	// findEventImageById 方法維持不變，它與 DTO 無關
	public byte[] findEventImageById(Integer eventId) {
		return eventRepository.findById(eventId).map(Event::getImage).orElse(null);
	}

	/**
	 * 根據 ID 查詢單筆活動詳情。
	 */
	public EventDTO findEventById(Integer eventId) {
		Optional<Event> eventOptional = eventRepository.findById(eventId);
		if (eventOptional.isEmpty()) {
			throw new RuntimeException("找不到 ID 為 " + eventId + " 的活動。");
		}
		return convertToDto(eventOptional.get());
	}

	/**
	 * 新增一筆活動。
	 */
	@Transactional
	public Event addEvent(EventDTO eventDTO) {
		Event event = new Event();
		event.setTitle(eventDTO.title());
		event.setContent(eventDTO.content());
		event.setStartDate(eventDTO.startDate());
		event.setEndDate(eventDTO.endDate());
		event.setSessionCount(eventDTO.sessionCount());

		if (eventDTO.eventCategoryId() != null) {
			EventCategory category = eventCategoryRepository.findById(eventDTO.eventCategoryId())
					.orElseThrow(() -> new RuntimeException("找不到指定的活動類型 ID: " + eventDTO.eventCategoryId()));
			event.setEventCategory(category);
		}

		if (eventDTO.base64ImageString() != null) {
			byte[] imageBytes = PictureConverter.convertBase64ToBytes(eventDTO.base64ImageString());
			event.setImage(imageBytes);
		}
		return eventRepository.save(event);
	}

	/**
	 * 根據 ID 刪除一筆活動。
	 */
	@Transactional
	public void deleteEventById(Integer eventId) {
		if (!eventRepository.existsById(eventId)) {
			throw new RuntimeException("找不到 ID 為 " + eventId + " 的活動，無法刪除。");
		}
		eventRepository.deleteById(eventId);
	}

	/**
	 * 更新一筆活動的完整資料。
	 */
	@Transactional
	public EventDTO updateEvent(Integer eventId, EventDTO updateDTO) {
		Optional<Event> existingEventOptional = eventRepository.findById(eventId);
		if (existingEventOptional.isEmpty()) {
			throw new RuntimeException("活動找不到，ID: " + eventId);
		}
		Event existingEvent = existingEventOptional.get();

		existingEvent.setTitle(updateDTO.title());
		existingEvent.setContent(updateDTO.content());
		existingEvent.setStartDate(updateDTO.startDate());
		existingEvent.setEndDate(updateDTO.endDate());
		existingEvent.setSessionCount(updateDTO.sessionCount());

		if (updateDTO.eventCategoryId() != null) {
			EventCategory category = eventCategoryRepository.findById(updateDTO.eventCategoryId())
					.orElseThrow(() -> new RuntimeException("找不到指定的活動類型 ID: " + updateDTO.eventCategoryId()));
			existingEvent.setEventCategory(category);
		} else {
			existingEvent.setEventCategory(null); // 如果傳入 null，則清除關聯
		}

		if (updateDTO.base64ImageString() != null) {
			try {
				if (updateDTO.base64ImageString().isEmpty()) {
					existingEvent.setImage(null);
				} else {
					byte[] imageBytes = PictureConverter.convertBase64ToBytes(updateDTO.base64ImageString());
					existingEvent.setImage(imageBytes);
				}
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("圖片 Base64 字串格式無效。", e);
			}
		}
		Event updatedEvent = eventRepository.save(existingEvent);
		return convertToDto(updatedEvent);
	}

	/** 提供前台列表查詢用（分頁 + 關鍵字），回傳實體 Page<Event> */
	public Page<Event> list(Pageable pageable, String keyword) {
		String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		if (kw == null) {
			return eventRepository.findAll(pageable);
		}
		return eventRepository.search(kw, pageable);
	}

	/** 提供前台單筆查詢用，找不到就丟出 400/404 類型的錯誤 */
	public Event getById(Integer id) {
		return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
	}

	public Optional<byte[]> getImageById(Integer id) {
		return eventRepository.findById(id).map(Event::getImage);
	}
	
	@Transactional(readOnly = true)
	public List<EventListDTO> findLatestActiveEvents(int limit) {
	    int top = Math.max(1, limit);
	    var page = org.springframework.data.domain.PageRequest.of(0, top);
	    java.time.LocalDate today = java.time.LocalDate.now();

	    List<Event> list = eventRepository.findActiveOrderByStartDateDesc(today, page);

	    return list.stream()
	            .map(e -> new EventListDTO(
	                    e.getEventId(),
	                    e.getTitle(),
	                    e.getStartDate(),
	                    e.getEndDate()
	            ))
	            .toList();
	}


}