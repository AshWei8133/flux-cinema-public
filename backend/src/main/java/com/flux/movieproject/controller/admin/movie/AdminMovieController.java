package com.flux.movieproject.controller.admin.movie;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.flux.movieproject.model.dto.movie.ActorListDTO;
import com.flux.movieproject.model.dto.movie.AddActorToCastRequestDTO;
import com.flux.movieproject.model.dto.movie.DirectorListDTO;
import com.flux.movieproject.model.dto.movie.GenreListDTO;
import com.flux.movieproject.model.dto.movie.MovieActorRoleDTO;
import com.flux.movieproject.model.dto.movie.MovieListResponseDTO;
import com.flux.movieproject.model.dto.movie.MoviesAllDataDTO;
import com.flux.movieproject.model.entity.movie.Actor;
import com.flux.movieproject.model.entity.movie.Director;
import com.flux.movieproject.model.entity.movie.Genre;
import com.flux.movieproject.model.entity.movie.Movie;
import com.flux.movieproject.repository.movie.MovieRepository;
import com.flux.movieproject.service.movie.MovieService;

import jakarta.persistence.EntityNotFoundException;
import lombok.Data;

@RestController
@RequestMapping("/api/admin/movie")
public class AdminMovieController {
	
	@Autowired
	private final MovieRepository movieRepository;
	private MovieService movieService;
	
	public AdminMovieController( MovieRepository movieRepository,MovieService movieService) {
		this.movieRepository = movieRepository; // 初始化 MovieRepository
		this.movieService = movieService;
	}
	// 為了方便，我們把接收資料的 DTO 直接定義在這裡
	// 您也可以把它獨立成一個 .java 檔案
	@Data
	static class UpdateRoleRequestDTO {
	    private String characterName;
	}

	/**
	 * 取得所有本地 Movie 表中的電影資料
	 * 
	 * @return 包含所有電影資料的列表
	 */
//	@GetMapping("/movies")
//	public ResponseEntity<List<Movie>> getAllLocalMovies() {
//		try {
//			List<Movie> movies = movieRepository.findAll();
//			return ResponseEntity.ok(movies);
//		} catch (Exception e) {
//			System.err.println("取得本地電影資料時發生錯誤：" + e.getMessage());
//			return ResponseEntity.status(500).build();
//		}
//	}
	/**
	 * 取得所有本地 Movie 表中的電影資料
	 * 
	 * @return 包含所有電影資料的列表
	 */
	@GetMapping("/movies/all")
	public ResponseEntity<List<MoviesAllDataDTO>> getAllLocalMovies() {
		try {
			List<MoviesAllDataDTO> movies = movieService.getAllLocalMovies();
			return ResponseEntity.ok(movies);
		} catch (Exception e) {
			System.err.println("取得本地電影資料時發生錯誤：" + e.getMessage());
			return ResponseEntity.status(500).build();
		}
	}

	/**
	 * 根據本地片名模糊搜尋電影
	 * 
	 * @param title 片名關鍵字
	 * @return 符合條件的電影列表
	 */
	@GetMapping("/movies/search/title-local")
	public ResponseEntity<List<Movie>> searchMoviesByLocalTitle(@RequestParam String title) {
		try {
			List<Movie> movies = movieRepository.findByTitleLocalContainingIgnoreCase(title);
			return ResponseEntity.ok(movies);
		} catch (Exception e) {
			System.err.println("根據本地片名搜尋電影時發生錯誤：" + e.getMessage());
			return ResponseEntity.status(500).build();
		}
	}

	/**
	 * 根據英文片名模糊搜尋電影
	 * 
	 * @param title 片名關鍵字
	 * @return 符合條件的電影列表
	 */
	@GetMapping("/movies/search/title-english")
	public ResponseEntity<List<Movie>> searchMoviesByEnglishTitle(@RequestParam String title) {
		try {
			List<Movie> movies = movieRepository.findByTitleEnglishContainingIgnoreCase(title);
			return ResponseEntity.ok(movies);
		} catch (Exception e) {
			System.err.println("根據英文片名搜尋電影時發生錯誤：" + e.getMessage());
			return ResponseEntity.status(500).build();
		}
	}

	/**
	 * 根據類型名稱搜尋電影
	 * 
	 * @param genreName 類型名稱
	 * @return 符合條件的電影列表
	 */
	@GetMapping("/movies/search/genre")
	public ResponseEntity<List<Movie>> searchMoviesByGenre(@RequestParam String genreName) {
		try {
			List<Movie> movies = movieRepository.findByGenreName(genreName);
			return ResponseEntity.ok(movies);
		} catch (Exception e) {
			System.err.println("根據類型搜尋電影時發生錯誤：" + e.getMessage());
			return ResponseEntity.status(500).build();
		}
	}

	/**
	 * 根據導演姓名搜尋電影
	 * 
	 * @param directorName 導演姓名
	 * @return 符合條件的電影列表
	 */
	@GetMapping("/movies/search/director")
	public ResponseEntity<List<Movie>> searchMoviesByDirector(@RequestParam String directorName) {
		try {
			List<Movie> movies = movieRepository.findByDirectorName(directorName);
			return ResponseEntity.ok(movies);
		} catch (Exception e) {
			System.err.println("根據導演搜尋電影時發生錯誤：" + e.getMessage());
			return ResponseEntity.status(500).build();
		}
	}

	/**
	 * 根據演員姓名搜尋電影
	 * 
	 * @param actorName 演員姓名
	 * @return 符合條件的電影列表
	 */
	@GetMapping("/movies/search/actor")
	public ResponseEntity<List<Movie>> searchMoviesByActor(@RequestParam String actorName) {
		try {
			List<Movie> movies = movieRepository.findByActorName(actorName);
			return ResponseEntity.ok(movies);
		} catch (Exception e) {
			System.err.println("根據演員搜尋電影時發生錯誤：" + e.getMessage());
			return ResponseEntity.status(500).build();
		}
	}
	
	/**
     * 根據電影 ID 刪除電影資料
     * @param id 要刪除的電影 ID
     * @return 刪除結果訊息
     */
    @DeleteMapping("/movies/delete/{id}") // 路徑包含 {id}
    public ResponseEntity<String> deleteMovieById(@PathVariable Integer id) { // 修正點：將 @RequestParam 改為 @PathVariable
        try {
            boolean deleted = movieService.deleteMovieById(id); // 呼叫 Service
            if (deleted) {
                return ResponseEntity.ok("電影 ID: " + id + " 已成功刪除。");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到電影 ID: " + id + "，無法刪除。");
            }
        } catch (Exception e) {
            System.err.println("刪除電影 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("刪除電影時發生錯誤：" + e.getMessage());
        }
    }

    /**
     * 【已修正】根據電影 ID 更新電影資料
     * 這個 API 現在是一個真正的 PATCH 更新，只會修改前端有傳送過來的欄位。
     * @param id 要更新的電影 ID
     * @param updates 一個 Map，只包含需要被更新的欄位和對應的新值
     * @return 更新結果
     */
    @PutMapping("/movies/update/{id}")
    public ResponseEntity<String> updateMovieById(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) { // <-- 【核心修正】將接收類型從 Movie 改為 Map<String, Object>
        try {
            // 將收到的 Map 直接傳給 Service 層處理
            Optional<Movie> result = movieService.updateMovieById(id, updates);
            if (result.isPresent()) {
                return ResponseEntity.ok("電影 ID: " + id + " 已成功更新。");
            } else {
                return ResponseEntity.status(404).body("找不到電影 ID: " + id + "，無法更新。");
            }
        } catch (Exception e) {
            System.err.println("更新電影 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(500).body("更新電影時發生錯誤：" + e.getMessage());
        }
    }
    /**
     * 新增電影資料
     * @param movie 要新增的電影實體 (從請求主體取得)
     * @return 新增後的電影實體或錯誤訊息
     */
    @PostMapping("/movies/create") // 修正點：新增端點路徑為 /movies/create
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {
        try {
            Movie newMovie = movieService.createMovie(movie);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMovie);
        } catch (Exception e) {
            System.err.println("新增電影時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據電影 ID 取得海報圖片
     * @param id 電影 ID
     * @return 海報圖片的二進位資料
     */
    @GetMapping("/movies/{id}/poster") // 新增端點：獲取電影海報
    public ResponseEntity<byte[]> getMoviePoster(@PathVariable Integer id) {
        try {
            Optional<Movie> movieOptional = movieRepository.findById(id);
            if (movieOptional.isPresent()) {
                Movie movie = movieOptional.get();
                if (movie.getPosterImage() != null) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG) // 假設圖片是 JPEG 格式
                            .body(movie.getPosterImage());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 沒有海報圖片
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 找不到電影
            }
        } catch (Exception e) {
            System.err.println("取得電影海報 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 獲取特定電影的演員列表及其角色
     * @param movieId 電影 ID
     * @return 該電影的演員及角色列表
     */
    @GetMapping("/movies/{movieId}/cast") // 新增端點：獲取電影卡司
    public ResponseEntity<List<MovieActorRoleDTO>> getMovieCast(@PathVariable Integer movieId) {
        try {
            List<MovieActorRoleDTO> cast = movieService.getMovieCast(movieId);
            return ResponseEntity.ok(cast);
        } catch (Exception e) {
            System.err.println("獲取電影卡司時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新電影中特定演員的角色名稱
     * @param movieId 電影 ID
     * @param actorId 演員 ID
     * @param request 包含新角色名稱的請求 body
     * @return 處理結果
     */
    @PutMapping("/movies/{movieId}/actors/{actorId}")
    public ResponseEntity<String> updateMovieActorRole(
            @PathVariable Integer movieId,
            @PathVariable Integer actorId,
            @RequestBody UpdateRoleRequestDTO request) {

        try {
            if (request == null || request.getCharacterName() == null) {
                return ResponseEntity.badRequest().body("請求中缺少 'characterName' 欄位。");
            }

            // 直接呼叫 Service 層執行更新
            movieService.updateMovieActorRole(movieId, actorId, request.getCharacterName());
            
            return ResponseEntity.ok("角色更新成功。");

        } catch (EntityNotFoundException e) {
            // 【新增】如果 Service 找不到電影或演員，會拋出這個錯誤
            // 我們回傳一個 404 Not Found 錯誤，這比 500 更精確
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            // 處理其他所有未預期的錯誤
            System.err.println("更新角色時發生未預期錯誤：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("更新角色時發生伺服器內部錯誤。");
        }
    }
    

	// --- Director CRUD API ---

    /**
     * 新增導演
     * @param director 要新增的導演實體 (從請求主體取得)
     * @return 新增後的導演實體或錯誤訊息
     */
    @PostMapping("/directors")
    public ResponseEntity<Director> createDirector(@RequestBody Director director) {
        try {
            Director newDirector = movieService.createDirector(director);
            return ResponseEntity.status(HttpStatus.CREATED).body(newDirector);
        } catch (Exception e) {
            System.err.println("新增導演時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 查詢導演
     * @param id 導演 ID
     * @return 導演實體或 404 Not Found
     */
    @GetMapping("/directors/{id}/detail")
    public ResponseEntity<Director> getDirectorById(@PathVariable Integer id) {
        try {
            Optional<Director> director = movieService.getDirectorById(id);
            return director.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            System.err.println("查詢導演 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 查詢所有導演
     * @return 所有導演的列表
     */
    @GetMapping("/directors")
    public ResponseEntity<List<DirectorListDTO>> getAllDirectors() {
        try {
            List<DirectorListDTO> directors = movieService.getAllDirectors();
            return ResponseEntity.ok(directors);
        } catch (Exception e) {
            System.err.println("查詢所有導演時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 更新導演資料
     * @param id 導演 ID
     * @param updatedDirector 包含新資料的 Director 物件
     * @return 更新後的導演實體或錯誤訊息
     */
    @PutMapping("/directors/{id}")
    public ResponseEntity<Director> updateDirector(@PathVariable Integer id, @RequestBody Director updatedDirector) {
        try {
            Optional<Director> result = movieService.updateDirector(id, updatedDirector);
            return result.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            System.err.println("更新導演 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 刪除導演
     * @param id 導演 ID
     * @return 刪除結果訊息
     */
    @DeleteMapping("/directors/{id}")
    public ResponseEntity<String> deleteDirector(@PathVariable Integer id) {
        try {
            boolean deleted = movieService.deleteDirector(id);
            if (deleted) {
                return ResponseEntity.ok("導演 ID: " + id + " 已成功刪除。");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到導演 ID: " + id + "，無法刪除。");
            }
        } catch (Exception e) {
            System.err.println("刪除導演 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("刪除導演時發生錯誤：" + e.getMessage());
        }
    }

    // --- Actor CRUD API ---

    /**
     * 新增演員
     * @param actor 要新增的演員實體 (從請求主體取得)
     * @return 新增後的演員實體或錯誤訊息
     */
    @PostMapping("/actors")
    public ResponseEntity<Actor> createActor(@RequestBody Actor actor) {
        try {
            Actor newActor = movieService.createActor(actor);
            return ResponseEntity.status(HttpStatus.CREATED).body(newActor);
        } catch (Exception e) {
            System.err.println("新增演員時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 查詢演員
     * @param id 演員 ID
     * @return 演員實體或 404 Not Found
     */
    @GetMapping("/actors/{id}/detail")
    public ResponseEntity<Actor> getActorById(@PathVariable Integer id) {
        try {
            Optional<Actor> actor = movieService.getActorById(id);
            return actor.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            System.err.println("查詢演員 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 查詢所有演員
     * @return 所有演員的列表
     */
    @GetMapping("/actors")
    public ResponseEntity<List<ActorListDTO>> getAllActors() {
        try {
            List<ActorListDTO> actors = movieService.getAllActors();
            return ResponseEntity.ok(actors);
        } catch (Exception e) {
            System.err.println("查詢所有演員時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 更新演員資料
     * @param id 演員 ID
     * @param updatedActor 包含新資料的 Actor 物件
     * @return 更新後的演員實體或錯誤訊息
     */
    @PutMapping("/actors/{id}")
    public ResponseEntity<Actor> updateActor(@PathVariable Integer id, @RequestBody Actor updatedActor) {
        try {
            Optional<Actor> result = movieService.updateActor(id, updatedActor);
            return result.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            System.err.println("更新演員 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 刪除演員
     * @param id 演員 ID
     * @return 刪除結果訊息
     */
    @DeleteMapping("/actors/{id}")
    public ResponseEntity<String> deleteActor(@PathVariable Integer id) {
        try {
            boolean deleted = movieService.deleteActor(id);
            if (deleted) {
                return ResponseEntity.ok("演員 ID: " + id + " 已成功刪除。");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到演員 ID: " + id + "，無法刪除。");
            }
        } catch (Exception e) {
            System.err.println("刪除演員 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("刪除演員時發生錯誤：" + e.getMessage());
        }
    }

    // --- Genre CRUD API ---

    /**
     * 新增類型
     * @param genre 要新增的類型實體 (從請求主體取得)
     * @return 新增後的類型實體或錯誤訊息
     */
    @PostMapping("/genres")
    public ResponseEntity<Genre> createGenre(@RequestBody Genre genre) {
        try {
            Genre newGenre = movieService.createGenre(genre);
            return ResponseEntity.status(HttpStatus.CREATED).body(newGenre);
        } catch (Exception e) {
            System.err.println("新增類型時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 查詢類型
     * @param id 類型 ID
     * @return 類型實體或 404 Not Found
     */
    @GetMapping("/genres/{id}/detail")
    public ResponseEntity<Genre> getGenreById(@PathVariable Integer id) {
        try {
            Optional<Genre> genre = movieService.getGenreById(id);
            return genre.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            System.err.println("查詢類型 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 查詢所有類型
     * @return 所有類型的列表
     */
    @GetMapping("/genres")
    public ResponseEntity<List<GenreListDTO>> getAllGenres() { // 修正點：回傳 GenreListDto 列表
        try {
            List<GenreListDTO> genres = movieService.getAllGenres();
            return ResponseEntity.ok(genres);
        } catch (Exception e) {
            System.err.println("查詢所有類型時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 更新類型資料
     * @param id 類型 ID
     * @param updatedGenre 包含新資料的 Genre 物件
     * @return 更新後的類型實體或錯誤訊息
     */
    @PutMapping("/genres/{id}")
    public ResponseEntity<Genre> updateGenre(@PathVariable Integer id, @RequestBody Genre updatedGenre) {
        try {
            Optional<Genre> result = movieService.updateGenre(id, updatedGenre);
            return result.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            System.err.println("更新類型 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根據 ID 刪除類型
     * @param id 類型 ID
     * @return 刪除結果訊息
     */
    @DeleteMapping("/genres/{id}")
    public ResponseEntity<String> deleteGenre(@PathVariable Integer id) {
        try {
            boolean deleted = movieService.deleteGenre(id);
            if (deleted) {
                return ResponseEntity.ok("類型 ID: " + id + " 已成功刪除。");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到類型 ID: " + id + "，無法刪除。");
            }
        } catch (Exception e) {
            System.err.println("刪除類型 ID: " + id + " 時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("刪除類型時發生錯誤：" + e.getMessage());
        }
    }
    /**
     * 獲取特定電影的類型 ID 列表
     * @param movieId 電影 ID
     * @return 該電影的類型 ID 列表
     */
    @GetMapping("/movies/{movieId}/genres") // 修正點：新增獲取電影類型 ID 列表的端點
    public ResponseEntity<List<Integer>> getMovieGenreIds(@PathVariable Integer movieId) {
        try {
            List<Integer> genreIds = movieService.getMovieGenreIds(movieId);
            return ResponseEntity.ok(genreIds);
        } catch (Exception e) {
            System.err.println("獲取電影類型 ID 列表時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新特定電影的類型關聯
     * @param movieId 電影 ID
     * @param genreIds 新的類型 ID 列表
     * @return 更新結果訊息
     */
    @PutMapping("/movies/{movieId}/genres") // 修正點：新增更新電影類型關聯的端點
    public ResponseEntity<String> updateMovieGenres(
            @PathVariable Integer movieId,
            @RequestBody List<Integer> genreIds) { // 接收類型 ID 列表
        try {
            boolean updated = movieService.updateMovieGenres(movieId, genreIds);
            if (updated) {
                return ResponseEntity.ok("電影 ID: " + movieId + " 的類型已成功更新。");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到電影，無法更新類型。");
            }
        } catch (Exception e) {
            System.err.println("更新電影類型時發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新電影類型時發生錯誤：" + e.getMessage());
        }
    }
    /**
     * API 端點：新增一個演員到指定電影的卡司列表中
     * @param movieId 電影的 ID
     * @param request 包含 actorId 和 characterName 的請求內容
     * @return 處理結果的回應
     */
    @PostMapping("/movies/{movieId}/actors")
    public ResponseEntity<?> addActorToCast(
            @PathVariable Integer movieId,
            @RequestBody AddActorToCastRequestDTO request) {
        
        // --- 【偵錯日誌】---
        // 我們在這裡印出 Controller 從前端請求中收到的最原始的資料
        System.out.println("===== [Controller 偵錯] 收到 addActorToCast 請求 =====");
        System.out.println("  - Movie ID from URL: " + movieId);
        
        // 檢查整個 request 物件是否為 null
        if (request != null) {
            System.out.println("  - Actor ID from Request Body: " + request.getActorId());
            // 特別檢查 characterName，前後加上引號以利判斷
            System.out.println("  - Character Name from Request Body: '" + request.getCharacterName() + "'");
        } else {
            System.out.println("  - 嚴重錯誤: Request Body (AddActorToCastRequestDTO) 為 null！");
        }
        System.out.println("======================================================");

        try {
            // 接著才呼叫 Service 層，把接收到的資料傳下去
            movieService.addActorToMovie(movieId, request.getActorId(), request.getCharacterName());
            return ResponseEntity.status(HttpStatus.CREATED).body("成功將演員加入卡司");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("新增演員到卡司時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * API 端點：從指定電影的卡司列表中移除一個演員
     * 這個方法會對應到前端 MovieService.js 中的 removeActorFromMovie 函式
     * @param movieId 電影的ID，會從 URL 路徑中自動抓取
     * @param actorId 演員的ID，也會從 URL 路徑中自動抓取
     * @return 處理結果的回應
     */
    @DeleteMapping("/movies/{movieId}/actors/{actorId}")
    public ResponseEntity<Void> removeActorFromCast(
            @PathVariable Integer movieId,
            @PathVariable Integer actorId) {
        try {
            // 同樣地，把刪除的邏輯交給 Service 層處理
            movieService.removeActorFromMovie(movieId, actorId);
            
            // 如果成功刪除，回傳一個 HTTP 狀態碼 204 (No Content)
            // 這是 RESTful API 的標準做法，表示操作成功，但沒有內容需要回傳給前端
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            // 如果刪除失敗，回傳 500 錯誤
            // 更好的做法是拋出一個自訂的異常，讓全域異常處理器去捕捉
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/now-playing/{date}")
    public ResponseEntity<List<MovieListResponseDTO>> getNowPlayingMoviesByDate(
      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
           // 直接將轉換好的 LocalDate 物件傳遞給 Service
           List<MovieListResponseDTO> movies = movieService.findNowPlayingMoviesByDate(date);
           return ResponseEntity.ok(movies);
       }
    
}