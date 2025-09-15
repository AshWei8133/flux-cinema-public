package com.flux.movieproject.controller.admin.event;

import com.flux.movieproject.model.dto.event.EventEligibilityBatchCreateRequest;
import com.flux.movieproject.model.dto.event.EventEligibilityCreateRequest;
import com.flux.movieproject.model.dto.event.EventEligibilityResponse;
import com.flux.movieproject.service.event.EventEligibilityService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/event-eligibility")
public class AdminEventEligibilityController {

    private final EventEligibilityService service;

    public AdminEventEligibilityController(EventEligibilityService service) {
        this.service = service;
    }

    /** 建立單筆 eligibility */
    @PostMapping
    public ResponseEntity<EventEligibilityResponse> create(@RequestBody EventEligibilityCreateRequest req) {
      return ResponseEntity.ok(service.create(req));
    }

    /** 批次建立（同一活動，可同時傳 movieIds/sessionIds/memberLevelIds 的任意集合） */
    @PostMapping("/batch")
    public ResponseEntity<List<EventEligibilityResponse>> batch(@RequestBody EventEligibilityBatchCreateRequest req) {
      return ResponseEntity.ok(service.batchCreate(req));
    }

    /** 依活動查詢 */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventEligibilityResponse>> byEvent(@PathVariable Integer eventId) {
      return ResponseEntity.ok(service.findByEventId(eventId));
    }

    /** 全部查詢 */
    @GetMapping
    public ResponseEntity<List<EventEligibilityResponse>> all() {
      return ResponseEntity.ok(service.findAll());
    }

    /** 刪除 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
      service.delete(id);
      return ResponseEntity.noContent().build();
    }

    /** 簡單的 404 轉換（可改用 @ControllerAdvice 全域處理） */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
      return ResponseEntity.status(404).body(ex.getMessage());
    }
}
