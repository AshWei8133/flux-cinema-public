package com.flux.movieproject.controller.fluxapp.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.flux.movieproject.model.entity.event.Announcement;
import com.flux.movieproject.model.entity.event.AnnouncementListDTO;
import com.flux.movieproject.service.event.AnnouncementService;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementPublicController {

    @Autowired
    private AnnouncementService service;

    // ========== 列表 ==========
    @GetMapping
    public Page<AnnouncementListDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishDate,desc") String sort,
            @RequestParam(required = false) String keyword) {

        String[] parts = sort.split(",");
        Sort s = Sort.by(Sort.Direction.fromString(parts.length > 1 ? parts[1] : "desc"), parts[0]);
        Pageable pageable = PageRequest.of(page, size, s);

        Page<Announcement> src = service.findPublic(pageable, keyword);

        List<AnnouncementListDTO> dtoList = new ArrayList<>();
        for (Announcement a : src.getContent()) {
            dtoList.add(new AnnouncementListDTO(
                    a.getAnnouncementId(),
                    a.getTitle(),
                    a.getPublishDate(),
                    a.getContent() // 摘要/內容（前端你已有節錄）
            ));
        }

        return new PageImpl<>(dtoList, pageable, src.getTotalElements());
    }

    // ========== 單筆詳情 ==========
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementListDTO> getOne(@PathVariable("id") Integer id) {
        return service.findById(id)
                .map(a -> ResponseEntity.ok(new AnnouncementListDTO(
                        a.getAnnouncementId(),
                        a.getTitle(),
                        a.getPublishDate(),
                        a.getContent()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ========== 圖片 Blob ==========
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Integer id) {
        Optional<byte[]> imgOpt = service.getImageById(id);
        if (imgOpt.isEmpty() || imgOpt.get() == null || imgOpt.get().length == 0) {
            return ResponseEntity.notFound().build();
        }
        byte[] body = imgOpt.get();

        MediaType mediaType = detectImageMediaType(body); // 粗略偵測 mime；未知就給 octet-stream
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).cachePublic()) // 可調整快取策略
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.length))
                .body(body);
    }

    // ====== 簡單的圖片格式偵測（PNG/JPEG/GIF/WEBP），其餘回 octet-stream ======
    private static MediaType detectImageMediaType(byte[] bytes) {
        if (bytes.length >= 8 &&
                bytes[0] == (byte)0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return MediaType.IMAGE_PNG; // PNG
        }
        if (bytes.length >= 3 &&
                bytes[0] == (byte)0xFF && bytes[1] == (byte)0xD8 && bytes[2] == (byte)0xFF) {
            return MediaType.IMAGE_JPEG; // JPEG
        }
        if (bytes.length >= 6 &&
                bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' &&
                bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a') {
            return MediaType.IMAGE_GIF; // GIF
        }
        if (bytes.length >= 12 &&
                bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' &&
                bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return MediaType.parseMediaType("image/webp"); // WEBP
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
