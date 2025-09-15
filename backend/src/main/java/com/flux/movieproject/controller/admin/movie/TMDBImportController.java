package com.flux.movieproject.controller.admin.movie;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flux.movieproject.model.dto.movie.TMDBMovieResponseDTO;
import com.flux.movieproject.model.dto.movie.TMDBMovieSearchResponseDTO;
import com.flux.movieproject.model.entity.movie.TMDBMovie;
import com.flux.movieproject.repository.movie.TMDBMovieRepository;
import com.flux.movieproject.service.movie.TMDBDataImportService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus; // <-- 確保導入這個



@RestController
@RequestMapping("/api/admin/tmdb")
public class TMDBImportController {

	private final TMDBDataImportService tmdbDataImportService;
	private final TMDBMovieRepository TMDBMovieRepository;

	public TMDBImportController(TMDBDataImportService tmdbDataImportService, TMDBMovieRepository TMDBMovieRepository) {
		this.TMDBMovieRepository = TMDBMovieRepository;
		this.tmdbDataImportService = tmdbDataImportService;
	}

	/**
	 * 手動匯入單一電影資料的 API 端點，將資料存入 TMDBMovie 表
	 * 
	 * @param movieId TMDB 的電影 ID
	 * @return 匯入結果訊息
	 */
	@PostMapping("/import/{movieId}")
	public ResponseEntity<String> importMovie(@PathVariable Integer movieId) {
		try {
			tmdbDataImportService.importMovieData(movieId);
			return ResponseEntity.ok("成功匯入電影 ID: " + movieId + " 到 TMDBMovie 表。");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("匯入電影 ID: " + movieId + " 時發生錯誤：" + e.getMessage());
		}
	}

//	/**
//	 * 手動匯入指定年份和地區上映電影的 API 端點，將資料存入 TMDBMovie 表
//	 * 
//	 * @param year   電影上映年份 (e.g., 2025)
//	 * @param region 電影上映地區代碼 (e.g., TW)
//	 * @return 匯入結果訊息
//	 */
//	@PostMapping("/import-by-year-region")
//	public ResponseEntity<String> importMoviesByYearAndRegion(@RequestParam LocalDate startDate, LocalDate endDate, String region,Integer page) {
//		try {
//			tmdbDataImportService.importMoviesByDateRangeAndRegion(startDate, endDate, region,page);
//			return ResponseEntity.ok("已開始匯入 " + startDate + " 到 "+ endDate + region + " 上映的電影到 TMDBMovie 表。");
//		} catch (Exception e) {
//			return ResponseEntity.status(500).body("匯入電影時發生錯誤：" + e.getMessage());
//		}
//	}
	@PostMapping("/import-by-date")
	public ResponseEntity<TMDBMovieSearchResponseDTO> importMoviesByYearAndRegion(
	        @RequestParam("startDate") LocalDate startDate,
	        @RequestParam("endDate") LocalDate endDate,
	        @RequestParam("region") String region,
	        @RequestParam("page") Integer page) {
	    
	    // 1. 呼叫 Service 並接收結果
	    TMDBMovieSearchResponseDTO responseDto = tmdbDataImportService.importMoviesByDateRangeAndRegion(startDate, endDate, region, page);
	    
	    // 2. 【最關鍵的修正】檢查結果是否為 null
	    if (responseDto != null) {
	        // 只有在確定拿到非 null 的物件時，才回傳 200 OK 和 DTO 物件
	        return ResponseEntity.ok(responseDto);
	    } else {
	        // 如果拿到的是 null，代表底層服務出錯了
	        // 我們應該回傳一個伺服器錯誤狀態 (例如 500)，而不是 200 OK
	        System.err.println("[Controller] Service returned null. Sending 500 Internal Server Error to client.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}

	/**
	 * 手動匯入單一電影資料的 API 端點，將資料從 TMDBMovie 表轉存到本地 Movie 表
	 * 
	 * @param tmdbMovieId TMDB 的電影 ID，作為查詢 TMDBMovie 表的依據
	 * @return 匯入結果訊息
	 */
	@PostMapping("/import-to-local/{tmdbMovieId}")
	public ResponseEntity<String> importToLocalMovie(@PathVariable Integer tmdbMovieId) {
		try {
			tmdbDataImportService.importToLocalMovieTable(tmdbMovieId);
			return ResponseEntity.ok("成功將電影 ID: " + tmdbMovieId + " 從 TMDBMovie 表轉存到本地 Movie 表。");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("將電影 ID: " + tmdbMovieId + " 轉存到本地 Movie 表時發生錯誤：" + e.getMessage());
		}
	}
	/**
	 * 取得所有本地 TMDBMovie 表中的電影資料
	 * 
	 * @return 包含所有電影資料的列表
	 */
	@GetMapping("/tmdbmovies")
	public ResponseEntity<List<TMDBMovie>> getAllImportedMovies() {
		try {
			List<TMDBMovie> tmdbMovies = TMDBMovieRepository.findAll();
			return ResponseEntity.ok(tmdbMovies);
		} catch (Exception e) {
			System.err.println("取得本地電影資料時發生錯誤：" + e.getMessage());
			return ResponseEntity.status(500).build();
		}
	}
	/**
     * 將所有 TMDBMovie 的電影匯入到本地 Movie 表
     * @return 匯入結果訊息
     */
    @PostMapping("/import-all-to-local")
    public ResponseEntity<String> importAllTmdbMoviesToLocal() {
        try {
            tmdbDataImportService.importAllTmdbMoviesToLocal();
            return ResponseEntity.ok("所有 TMDBMovie 資料已成功匯入到本地 Movie 表。");
        } catch (Exception e) {
            System.err.println("批量匯入所有 TMDBMovie 資料到本地 Movie 表時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(500).body("批量匯入所有 TMDBMovie 資料到本地 Movie 表時發生錯誤：" + e.getMessage());
        }
    }
	@PostMapping("/addmovie")
	public String addMovie(@RequestBody String entity) {
		//TODO: process POST request
		
		return entity;
	}
	/**
     * 取得所有 TMDBMovie 資料表中的電影資料
     * @return 包含所有 TMDBMovie 資料的列表
     */
    @GetMapping("/tmdb-movies") // 新增端點：獲取所有 TMDBMovie
    public ResponseEntity<List<TMDBMovie>> getAllTmdbMovies() {
        try {
            List<TMDBMovie> tmdbMovies = tmdbDataImportService.getAllTmdbMovies(); // 呼叫 Service
            return ResponseEntity.ok(tmdbMovies);
        } catch (Exception e) {
            System.err.println("取得 TMDBMovie 資料時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 匯入選定的 TMDBMovie 電影到本地 Movie 表
     * @param tmdbMovieIds 要匯入的 TMDB 電影 ID 列表
     * @return 匯入結果訊息
     */
    @PostMapping("/import-selected-to-local") // 新增端點：匯入選定的 TMDBMovie
    public ResponseEntity<String> importSelectedTmdbMovies(@RequestBody List<Integer> tmdbMovieIds) {
        try {
            tmdbDataImportService.importSelectedTmdbMovies(tmdbMovieIds); // 呼叫 Service
            return ResponseEntity.ok("選定的 TMDBMovie 資料已成功匯入到本地 Movie 表。");
        } catch (Exception e) {
            System.err.println("匯入選定 TMDBMovie 資料時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(500).body("匯入選定 TMDBMovie 資料時發生錯誤：" + e.getMessage());
        }
    }
	
    
    /**
     * 取得所有 TMDBMovie 資料表中的電影資料
     * @return 包含所有 TMDBMovie 資料的列表 (格式化為 DTO)
     */
    @GetMapping("/new-tmdb-movies")
    // 修改回傳類型為 List<TMDBMovieResponseDTO>
    public ResponseEntity<List<TMDBMovieResponseDTO>> getAllTmdbMoviesII() {
        try {
            // 呼叫新的 Service 方法
            List<TMDBMovieResponseDTO> tmdbMovieDTOs = tmdbDataImportService.getAllTmdbMoviesAsDTO();
            return ResponseEntity.ok(tmdbMovieDTOs);
        } catch (Exception e) {
            // 建議在日誌中記錄更詳細的錯誤，方便排查
            System.err.println("取得 TMDBMovie DTO 資料時發生錯誤：" + e.getMessage());
            e.printStackTrace(); // 在開發階段印出完整堆疊
            return ResponseEntity.status(500).build();
        }
    }

	
}
