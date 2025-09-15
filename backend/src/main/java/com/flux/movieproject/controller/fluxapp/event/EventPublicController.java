package com.flux.movieproject.controller.fluxapp.event;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.event.PageResult;
import com.flux.movieproject.model.dto.event.event.EventDetailDTO;
import com.flux.movieproject.model.dto.event.event.EventListDTO;
import com.flux.movieproject.model.entity.event.Event;
import com.flux.movieproject.service.event.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;

    // GET /api/events?keyword=&page=0&size=9&sort=startDate,desc
    @GetMapping
    public PageResult<EventListDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "startDate,desc") String sort,
            @RequestParam(required = false) String keyword
    ) {
        String[] parts = sort.split(",");
        String sortProp = parts[0];
        Sort.Direction dir = parts.length > 1 ? Sort.Direction.fromString(parts[1]) : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortProp));

        Page<Event> p = eventService.list(pageable, keyword);
        Page<EventListDTO> mapped = p.map(e -> new EventListDTO(
                e.getEventId(),
                e.getTitle(),
                e.getStartDate(),
                e.getEndDate()
        ));
        return PageResult.of(mapped);
    }

    // GET /api/events/top2
    @GetMapping("/top2")
    public List<EventListDTO> top2() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<Event> p = eventService.list(pageable, null);
        return p.getContent().stream()
                .map(e -> new EventListDTO(
                        e.getEventId(),
                        e.getTitle(),
                        e.getStartDate(),
                        e.getEndDate()
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public EventDetailDTO detail(@PathVariable Integer id) {
        Event e = eventService.getById(id);

        // 先準備一個變數
        String b64 = null;

        // 把 byte[] image 轉成 Base64
        byte[] img = e.getImage();
        if (img != null && img.length > 0) {
            b64 = Base64.getEncoder().encodeToString(img);
        }

        // 回傳 DTO
        return new EventDetailDTO(
            e.getEventId(),
            e.getTitle(),
            e.getStartDate(),
            e.getEndDate(),
            e.getContent(),
            b64
        );
    }

 // ====== 圖片 Blob（跟公告同樣作法）======
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Integer id) {
        Optional<byte[]> imgOpt = eventService.getImageById(id);
        if (imgOpt.isEmpty() || imgOpt.get() == null || imgOpt.get().length == 0) {
            return ResponseEntity.notFound().build();
        }
        byte[] body = imgOpt.get();
        MediaType mediaType = detectImageMediaType(body);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noStore()) // 開發期直接禁快取
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.length))
                .body(body);
    }

    // ====== 簡單的圖片格式偵測 ======
    private static MediaType detectImageMediaType(byte[] bytes) {
        if (bytes.length >= 8 &&
                bytes[0] == (byte)0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }
        if (bytes.length >= 3 &&
                bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8 && bytes[2] == (byte)0xFF) {
            return MediaType.IMAGE_JPEG;
        }
        if (bytes.length >= 6 &&
                bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' &&
                bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a') {
            return MediaType.IMAGE_GIF;
        }
        if (bytes.length >= 12 &&
                bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
    
 // GET /api/events/latest?limit=5
    @GetMapping("/latest")
    public ResponseEntity<List<EventListDTO>> latest(@RequestParam(defaultValue = "5") int limit) {
        var list = eventService.findLatestActiveEvents(limit);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore()) // 關鍵：避免前台吃到舊快取
                .body(list);
    }

    
}



