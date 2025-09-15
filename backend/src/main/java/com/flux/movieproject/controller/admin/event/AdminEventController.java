package com.flux.movieproject.controller.admin.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.flux.movieproject.model.dto.event.EventAdminListResponse;
import com.flux.movieproject.model.dto.event.EventDTO;
import com.flux.movieproject.model.entity.event.Event;
import com.flux.movieproject.service.event.EventService;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

	@Autowired
	private EventService eventService;

	/**
	 * 獲取所有活動列表，支援標題查詢與排序
	 * 
	 * @param title     可選參數，用於標題的模糊查詢。
	 * @param sortBy    排序欄位，預設為 'startDate'。
	 * @param sortOrder 排序順序 預設為 'desc'。
	 * @return 包含活動列表和總數的 ResponseEntity。
	 */
	@GetMapping
	public ResponseEntity<EventAdminListResponse> getAllEvents(
			@RequestParam(required = false, defaultValue = "") String title,
			@RequestParam(defaultValue = "startDate") String sortBy,
			@RequestParam(defaultValue = "desc") String sortOrder) {

		// 將所有接收到的參數都傳遞給 Service 層進行處理
		EventAdminListResponse response = eventService.findAllEventsWithDetails(title, sortBy, sortOrder);

		return ResponseEntity.ok(response);
	}

	/**
	 * 根據 ID 查詢單一活動。
	 * 
	 * @param eventId 路徑變數，活動的唯一識別碼。
	 * @return 包含活動詳情的 ResponseEntity。
	 */
	@GetMapping("/{eventId}")
	public ResponseEntity<EventDTO> getEventById(@PathVariable Integer eventId) {
		EventDTO eventDTO = eventService.findEventById(eventId);
		return ResponseEntity.ok(eventDTO);
	}

	/**
	 * 新增一筆活動。
	 * 
	 * @param eventDTO 請求主體，包含新增活動所需資料的 DTO。
	 * @return 包含新增後活動資訊的 ResponseEntity，狀態碼為 201 Created。
	 */
	@PostMapping
	public ResponseEntity<Event> createEvent(@RequestBody EventDTO eventDTO) {
		Event savedEvent = eventService.addEvent(eventDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
	}

	/**
	 * 修改一筆活動。
	 * 
	 * @param eventId   活動 ID
	 * @param updateDTO 包含更新內容的活動 DTO
	 * @return 更新後的 EventDTO，如果找不到則回傳 404
	 */
	@PutMapping("/{eventId}")
	public ResponseEntity<EventDTO> updateEvent(@PathVariable("eventId") Integer eventId,
			@RequestBody EventDTO updateDTO) {
		try {
			EventDTO updatedEvent = eventService.updateEvent(eventId, updateDTO);
			return ResponseEntity.ok(updatedEvent);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * 根據 ID 刪除單一活動。
	 * 
	 * @param eventId 路徑變數，欲刪除活動的唯一識別碼。
	 * @return 無內容的 ResponseEntity，狀態碼為 204 No Content。
	 */
	@DeleteMapping("/{eventId}")
	public ResponseEntity<Void> deleteEvent(@PathVariable Integer eventId) {
		eventService.deleteEventById(eventId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{eventId}/image")
	public ResponseEntity<byte[]> getEventImage(@PathVariable Integer eventId) {
		byte[] imageBytes = eventService.findEventImageById(eventId);
		if (imageBytes != null && imageBytes.length > 0) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
