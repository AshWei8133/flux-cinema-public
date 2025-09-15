package com.flux.movieproject.controller.admin.moviesession;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.moviesession.MovieSessionDailyOverviewDTO;
import com.flux.movieproject.model.dto.moviesession.MovieSessionDateResponseDTO;
import com.flux.movieproject.model.dto.moviesession.MovieSessionUpdateDTO;
import com.flux.movieproject.service.moviesession.MovieSessionService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * API： 日曆展示相關： 1. 取得指定月份排片狀態 GET
 * /api/admin/movieSession/schedule/status/{year}/{month} 2. 取得指定年月場次各日期概況 GET
 * /api/admin/movieSession/date/{year}/{month}
 */
@RestController
@RequestMapping("/api/admin/movieSession")
public class AdminMovieSessionController {

	@Autowired
	private MovieSessionService movieSessionService;

	/**
	 * 取得特定年月所有日期的排片總覽（包含狀態與詳情）。
	 *
	 * @param year  指定的年（需介於 2000-2099）
	 * @param month 指定的月（需介於 1-12）
	 * @return 該年月所有日期的排片總覽列表，以 ResponseEntity 回應
	 */
	@GetMapping("/monthly-overview/{year}/{month}")
	public ResponseEntity<?> getMonthlyMovieSessionOverview(@PathVariable @Min(2000) @Max(2099) Integer year,
			@PathVariable @Min(1) @Max(12) Integer month) {
		try {
			// 呼叫 Service 層新的整合方法
			List<MovieSessionDailyOverviewDTO> monthlyOverview = movieSessionService
					.getMonthlyMovieSessionOverview(year, month);
			return ResponseEntity.ok(monthlyOverview);

		} catch (Exception e) {
			// 由於錯誤處理是重複的，也可以考慮把它抽出成一個私有方法
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("error", "Failed to retrieve monthly movie session overview.");
			errorResponse.put("message", "Internal server error: " + e.getMessage()); // 這裡加入 e.getMessage() 會更有幫助

			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	/**
	 * 根據指定的營業日期，獲取當天所有的場次資訊。 "營業日" 的具體時間範圍由 MovieSessionService 內部定義 (例如 06:00 至隔日
	 * 02:00)。
	 *
	 * @param date 從 URL 路徑中傳入的日期 (格式需為 YYYY-MM-DD)
	 * @return 包含當日所有場次 DTO 的列表，以及 HTTP 200 OK 狀態
	 */
	@GetMapping("/{date}")
	public ResponseEntity<List<MovieSessionDateResponseDTO>> getSessionsByDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		// 將日期參數傳遞給 Service 層，呼叫核心業務邏輯。
		List<MovieSessionDateResponseDTO> sessions = movieSessionService.findSessionsByDate(date);

		return ResponseEntity.ok(sessions);
	}
	
	@PutMapping("/batch-update/{date}")
	public ResponseEntity<?> batchUpdateMovieSessions(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody List<MovieSessionUpdateDTO> sessionDTOs) {
        
        if (sessionDTOs == null) {
            return ResponseEntity.badRequest().body("請求的排程列表不可為 null");
        }

        try {
            movieSessionService.batchUpdateSessions(date, sessionDTOs);
            // 為了讓前端能方便顯示訊息，回傳一個 Map/Object 格式的 JSON
            return ResponseEntity.ok(Map.of("message", "日期 " + date + " 的排程更新成功！"));
        } catch (Exception e) {
            // 在生產環境中，建議記錄詳細的錯誤日誌
            return ResponseEntity.internalServerError().body(Map.of("error", "更新失敗：" + e.getMessage()));
        }
    }

}
