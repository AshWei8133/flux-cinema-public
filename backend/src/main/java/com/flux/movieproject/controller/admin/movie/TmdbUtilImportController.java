package com.flux.movieproject.controller.admin.movie;

import com.flux.movieproject.service.movie.TmdbUtilImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/tmdb-util")
public class TmdbUtilImportController {
	@Autowired
	private TmdbUtilImportService tmdbUtilImportService;

	/**
	 * API 端點，用來觸發後端批次匯入電影資料。
	 * 
	 * @param startDate 欲匯入電影的上映起始日期 (格式: YYYY-MM-DD)
	 * @param endDate   欲匯入電影的上映結束日期 (格式: YYYY-MM-DD)
	 * @return 一個 ResponseEntity 物件，其中包含操作結果的訊息字串
	 */
	@PostMapping("/import-by-date")
	public ResponseEntity<String> importMovies(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
		// 使用 try-catch 區塊來捕捉服務層可能拋出的任何例外，確保 API 的穩定性
		try {
			// 將請求參數直接傳遞給 Service 層的核心方法執行
			tmdbUtilImportService.importMoviesUsingUtility(startDate, endDate);

			// 如果 Service 成功執行完畢，回傳一個成功的訊息和 HTTP 200 OK 狀態碼
			String message = String.format("已成功觸發後端任務(使用工具類)：匯入 %s 到 %s 之間的所有台灣電影。", startDate, endDate);
			return ResponseEntity.ok(message);
		} catch (Exception e) {
			// 如果 Service 執行過程中發生錯誤
			e.printStackTrace(); // 在後端控制台印出詳細的錯誤堆疊，方便除錯
			// 回傳一個包含錯誤訊息的字串和 HTTP 500 Internal Server Error 狀態碼
			return ResponseEntity.status(500).body("使用工具類匯入過程中發生伺-服器內部錯誤：" + e.getMessage());
		}
	}
}