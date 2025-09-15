package com.flux.movieproject.controller.fluxapp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.moviesession.movieshowtimes.ShowtimeDTO;
import com.flux.movieproject.model.dto.moviesession.quickbookingform.MovieSessionResponseDTO;
import com.flux.movieproject.model.dto.moviesession.viewseats.SessionSeatLayoutDTO;
import com.flux.movieproject.service.moviesession.MovieSessionService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/movieSession")
public class MovieSessionController {

	@Autowired
	private MovieSessionService movieSessionService;
	
	/**
     * 根據電影ID獲取其未來7天的所有場次
     * * @param movieId 電影的ID
     * @return 場次資訊列表
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<MovieSessionResponseDTO>> getShowtimesByMovie(@PathVariable Integer movieId) {
        List<MovieSessionResponseDTO> showtimes = movieSessionService.findShowtimesByMovieForNext7Days(movieId);
        return ResponseEntity.ok(showtimes);
    }
    
    /**
     * 處理前端獲取特定場次座位圖的請求。
     * @param sessionId 從 URL 路徑中獲取的場次 ID
     * @return 包含場次與座位資訊的 ResponseEntity
     */
    @GetMapping("/{sessionId}/seats")
    public ResponseEntity<?> getSessionSeatLayout(@PathVariable Integer sessionId) {
        try {
            // 呼叫 Service 層來獲取資料
            SessionSeatLayoutDTO layout = movieSessionService.getSessionSeatLayout(sessionId);
            // 成功找到，回傳 200 OK 和資料主體
            return ResponseEntity.ok(layout);
        } catch (EntityNotFoundException e) {
            // 如果 Service 拋出 EntityNotFoundException，表示找不到該場次
            // 回傳 404 Not Found 和錯誤訊息
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // 處理其他可能的伺服器內部錯誤
            // 回傳 500 Internal Server Error 和錯誤訊息
            return ResponseEntity.status(500).body("伺服器內部錯誤: " + e.getMessage());
        }
    }
    
    
    @GetMapping("/{movieId}/showtimes")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesWithSeatStatus(@PathVariable Integer movieId) {
        // 它呼叫的 Service 方法內部已經被我們優化了！
        List<ShowtimeDTO> showtimes = movieSessionService.findShowtimesWithSeatStatus(movieId);
        return ResponseEntity.ok(showtimes);
    }
}
