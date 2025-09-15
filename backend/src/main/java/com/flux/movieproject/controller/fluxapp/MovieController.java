package com.flux.movieproject.controller.fluxapp;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.movie.MovieListResponseDTO;
import com.flux.movieproject.model.dto.movie.MoviePublicDetailDTO;
import com.flux.movieproject.service.movie.MovieService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/movie")
public class MovieController {
	@Autowired
	private MovieService movieService;

	/**
	 * 取得特定日期上映中電影資料
	 * 
	 * @param date 查詢的日期
	 * @return 上映中電影列表
	 */
	@GetMapping("/now-playing/{date}")
	public ResponseEntity<List<MovieListResponseDTO>> getNowPlayingMoviesByDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		// 直接將轉換好的 LocalDate 物件傳遞給 Service
		List<MovieListResponseDTO> movies = movieService.findNowPlayingMoviesByDate(date);
		return ResponseEntity.ok(movies);
	}
	/**
	 * 取得特定日期即將上映電影資料
	 * 
	 * @param date 查詢的日期
	 * @return 上映中電影列表
	 */
	@GetMapping("/coming-soon/{date}")
	public ResponseEntity<List<MovieListResponseDTO>> fetchComingSoonMovies(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		// 直接將轉換好的 LocalDate 物件傳遞給 Service
		List<MovieListResponseDTO> movies = movieService.getComingSoonMovies(date);
		return ResponseEntity.ok(movies);
	}
	/**
     * 【已更新】API 端點：根據 ID 獲取單一電影的完整詳細資訊
     * @param id 從 URL 路徑中傳入的電影 ID
     * @return 包含電影、類型、導演和演員的 DTO 物件
     */
    @GetMapping("/{id}")
    public ResponseEntity<MoviePublicDetailDTO> getMovieById(@PathVariable("id") Integer id) {
        try {
            // 1. 呼叫 Service 層去執行商業邏輯，獲取打包好的 DTO
            MoviePublicDetailDTO movieDetail = movieService.getPublicMovieDetailById(id);
            
            // 2. 如果成功，回傳 200 OK 和完整的 DTO
            return ResponseEntity.ok(movieDetail);

        } catch (EntityNotFoundException e) {
            // 3. 如果 Service 說找不到電影，就回傳 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        } catch (Exception e) {
            // 4. 如果發生其他任何未預期的錯誤，就回傳 500
            System.err.println("獲取電影詳細資料時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
