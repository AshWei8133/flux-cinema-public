package com.flux.movieproject.service.movie;

import com.flux.movieproject.model.dto.movie.ActorDTO;
import com.flux.movieproject.model.dto.movie.DirectorDTO;
import com.flux.movieproject.model.dto.movie.GenreDTO;
import com.flux.movieproject.model.dto.movie.TMDBMovieCreditsDTO;
import com.flux.movieproject.model.dto.movie.TMDBMovieDTO;
import com.flux.movieproject.model.dto.movie.TMDBMovieResponseDTO;
import com.flux.movieproject.model.dto.movie.TMDBMovieSearchResponseDTO;
import com.flux.movieproject.model.entity.movie.Actor;
import com.flux.movieproject.model.entity.movie.Director;
import com.flux.movieproject.model.entity.movie.Genre;
import com.flux.movieproject.model.entity.movie.Movie;
import com.flux.movieproject.model.entity.movie.MovieActor;
import com.flux.movieproject.model.entity.movie.MovieDirector;
import com.flux.movieproject.model.entity.movie.MovieGenre;
import com.flux.movieproject.model.entity.movie.TMDBMovie;
import com.flux.movieproject.repository.movie.ActorRepository;
import com.flux.movieproject.repository.movie.DirectorRepository;
import com.flux.movieproject.repository.movie.GenreRepository;
import com.flux.movieproject.repository.movie.MovieActorRepository;
import com.flux.movieproject.repository.movie.MovieDirectorRepository;
import com.flux.movieproject.repository.movie.MovieGenreRepository;
import com.flux.movieproject.repository.movie.MovieRepository;
import com.flux.movieproject.repository.movie.TMDBMovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TMDBDataImportService {

	@Value("${tmdb.api.image-host}") // 從設定檔讀取圖片基底網址
	private String imageHost;

	private final TMDBApiService tmdbApiService;
	private final TMDBMovieRepository tmdbMovieRepository;
	private final MovieRepository movieRepository;
	private final RestTemplate restTemplate;
	private final DirectorRepository directorRepository; // 注入 DirectorRepository
	private final ActorRepository actorRepository; // 注入 ActorRepository
	private final GenreRepository genreRepository; // 注入 GenreRepository
	private final MovieDirectorRepository movieDirectorRepository; // 注入 MovieDirectorRepository
	private final MovieActorRepository movieActorRepository; // 注入 MovieActorRepository
	private final MovieGenreRepository movieGenreRepository; // 注入 MovieGenreRepository

	@Autowired
	public TMDBDataImportService(TMDBApiService tmdbApiService, TMDBMovieRepository tmdbMovieRepository,
			MovieRepository movieRepository, RestTemplate restTemplate, DirectorRepository directorRepository,
			ActorRepository actorRepository, GenreRepository genreRepository,
			MovieDirectorRepository movieDirectorRepository, MovieActorRepository movieActorRepository,
			MovieGenreRepository movieGenreRepository) {
		this.tmdbApiService = tmdbApiService;
		this.tmdbMovieRepository = tmdbMovieRepository;
		this.movieRepository = movieRepository;
		this.restTemplate = restTemplate;
		this.directorRepository = directorRepository;
		this.actorRepository = actorRepository;
		this.genreRepository = genreRepository;
		this.movieDirectorRepository = movieDirectorRepository;
		this.movieActorRepository = movieActorRepository;
		this.movieGenreRepository = movieGenreRepository;
	}

	/**
	 * 從 TMDB API 匯入電影資料到 TMDBMovie 資料表
	 * 
	 * @param movieId TMDB 的電影 ID
	 */
	@Transactional
	public void importMovieData(Integer movieId) {
		TMDBMovieDTO TMDBMovieDTO = tmdbApiService.fetchMovieById(movieId);

		if (TMDBMovieDTO == null) {
			System.err.println("無法匯入電影資料，因為 API 回傳為空。");
			return;
		}

		TMDBMovie tmdbMovie;
		if (tmdbMovieRepository.existsById(TMDBMovieDTO.getId())) {
			tmdbMovie = tmdbMovieRepository.findById(TMDBMovieDTO.getId()).get();
			System.out.println("電影 ID: " + movieId + " 已存在，將進行更新。");
		} else {
			tmdbMovie = new TMDBMovie();
			tmdbMovie.setTmdbMovieId(TMDBMovieDTO.getId());
			tmdbMovie.setCreateTime(LocalDateTime.now());
			System.out.println("電影 ID: " + movieId + " 不存在，將新增。");
		}

		tmdbMovie.setTitleLocal(TMDBMovieDTO.getTitle());
		tmdbMovie.setTitleEnglish(TMDBMovieDTO.getOriginalTitle());
		tmdbMovie.setReleaseDate(TMDBMovieDTO.getReleaseDate());
		tmdbMovie.setOverview(TMDBMovieDTO.getOverview());
		tmdbMovie.setDurationMinutes(TMDBMovieDTO.getRuntimeMinutes());
		tmdbMovie.setOriginalLanguage(TMDBMovieDTO.getOriginalLanguage());
		tmdbMovie.setPopularity(TMDBMovieDTO.getPopularity());
		tmdbMovie.setTrailerUrl(TMDBMovieDTO.getTrailerUrl());
		tmdbMovie.setVoteAverage(TMDBMovieDTO.getVoteAverage());
		tmdbMovie.setVoteCount(TMDBMovieDTO.getVoteCount());
		tmdbMovie.setUpdateTime(LocalDateTime.now());

		if (TMDBMovieDTO.getPosterPath() != null && !TMDBMovieDTO.getPosterPath().isEmpty()) {
			try {
				byte[] posterBytes = downloadPosterImage(TMDBMovieDTO.getPosterPath());
				tmdbMovie.setPosterImage(posterBytes);
				System.out.println("成功下載電影海報： " + TMDBMovieDTO.getPosterPath());
			} catch (Exception e) {
				System.err.println("下載海報圖片時發生錯誤：" + e.getMessage());
			}
		}

		try {
			tmdbMovieRepository.save(tmdbMovie);
			System.out.println("成功匯入電影 ID: " + movieId);
		} catch (Exception e) {
			System.err.println("匯入電影 ID: " + movieId + " 時發生錯誤：" + e.getMessage());
		}
	}

	/**
	 * 從 TMDBMovie 資料表將資料匯入到本地的 Movie 資料表，並處理導演、演員、類型 如果電影已存在於本地 Movie 表，則略過匯入。
	 * 
	 * @param tmdbMovieId TMDB 的電影 ID
	 */
	@Transactional
	public void importToLocalMovieTable(Integer tmdbMovieId) {
		// 檢查本地 Movie 表中是否已存在此電影
		Optional<Movie> existingMovie = movieRepository.findByTmdbMovieId(tmdbMovieId);

		if (existingMovie.isPresent()) {
			System.out.println("電影 ID: " + tmdbMovieId + " 已存在於本地 Movie 表，將略過匯入。");
			return; // 修正點：如果電影已存在，則直接返回，略過後續操作
		}

		TMDBMovie tmdbMovie = tmdbMovieRepository.findById(tmdbMovieId)
				.orElseThrow(() -> new RuntimeException("在 TMDBMovie 資料表中找不到電影 ID: " + tmdbMovieId));

		Movie localMovie = new Movie();
		localMovie.setCreateTime(LocalDateTime.now());
		localMovie.setTmdbMovie(tmdbMovie); // 設定 OneToOne 關聯
		System.out.println("電影 ID: " + tmdbMovieId + " 不存在於本地 Movie 表，將新增。");

		// 將 TMDBMovie 的資料複製到 Movie 實體
		localMovie.setTitleLocal(tmdbMovie.getTitleLocal());
		localMovie.setTitleEnglish(tmdbMovie.getTitleEnglish());
		localMovie.setReleaseDate(tmdbMovie.getReleaseDate());
		// 1. 獲取原始分級
		String rawCertification = tmdbMovie.getCertification();
		// 2. 呼叫輔助方法進行標準化
		String standardizedCert = standardizeCertification(rawCertification);
		// 3. 設定標準化後的分級
		localMovie.setCertification(standardizedCert);
		localMovie.setOverview(tmdbMovie.getOverview());
		localMovie.setTrailerUrl(tmdbMovie.getTrailerUrl());
		localMovie.setDurationMinutes(tmdbMovie.getDurationMinutes());
		localMovie.setPosterImage(tmdbMovie.getPosterImage());
		localMovie.setOriginalLanguage(tmdbMovie.getOriginalLanguage());
		localMovie.setPopularity(tmdbMovie.getPopularity().doubleValue());
		localMovie.setVoteAverage(tmdbMovie.getVoteAverage().doubleValue());
		localMovie.setVoteCount(tmdbMovie.getVoteCount());
		localMovie.setStatus(false); // 預設為下架
		localMovie.setUpdateTime(LocalDateTime.now());

		// 儲存 Movie 實體，以便後續關聯使用其 ID
		movieRepository.save(localMovie);
		System.out.println("成功將電影 ID: " + tmdbMovieId + " 匯入到本地 Movie 表。");

		// --- 匯入導演、演員、類型資料 ---
		// 1. 匯入類型 (Genres)
		TMDBMovieDTO TMDBMovieDTO = tmdbApiService.fetchMovieById(tmdbMovieId); // 重新獲取 DTO 以取得類型
		if (TMDBMovieDTO != null && TMDBMovieDTO.getGenres() != null) {
			for (TMDBMovieDTO.TMDBGenreDto genreDto : TMDBMovieDTO.getGenres()) {
				Genre genre = genreRepository.findByTmdbGenreId(genreDto.getId()).orElseGet(() -> {
					Genre newGenre = new Genre();
					newGenre.setTmdbGenreId(genreDto.getId());
					newGenre.setName(genreDto.getName());
					return genreRepository.save(newGenre);
				});

				// 建立電影與類型的關聯
				MovieGenre movieGenre = new MovieGenre(localMovie, genre);
				movieGenreRepository.save(movieGenre);
				System.out.println("關聯電影 " + localMovie.getId() + " 與類型 " + genre.getName());
			}
		}

		// 2. 匯入導演和演員 (Credits)
		TMDBMovieCreditsDTO creditsDto = tmdbApiService.fetchMovieCredits(tmdbMovieId);
		if (creditsDto != null) {
			// 處理導演
			for (TMDBMovieCreditsDTO.TMDBCrewDto crewDto : creditsDto.getCrew()) {
				if ("Director".equals(crewDto.getJob())) {
					Director director = directorRepository.findByTmdbDirectorId(crewDto.getId()).orElseGet(() -> {
						Director newDirector = new Director();
						newDirector.setTmdbDirectorId(crewDto.getId());
						newDirector.setName(crewDto.getName());
						// newDirector.setDirectorSummary(crewDto.getBiography()); // 如果 TMDB API 有提供簡介
						return directorRepository.save(newDirector);
					});

					// 建立電影與導演的關聯
					MovieDirector movieDirector = new MovieDirector(localMovie, director);
					movieDirectorRepository.save(movieDirector);
					System.out.println("關聯電影 " + localMovie.getId() + " 與導演 " + director.getName());
				}
			}

			// 處理演員
			for (TMDBMovieCreditsDTO.TMDBCastDto castDto : creditsDto.getCast()) {
				Actor actor = actorRepository.findByTmdbActorId(castDto.getId()).orElseGet(() -> {
					Actor newActor = new Actor();
					newActor.setTmdbActorId(castDto.getId());
					newActor.setName(castDto.getName());
					// newActor.setBiography(castDto.getBiography()); // 如果 TMDB API 有提供簡介
					return actorRepository.save(newActor);
				});

				// 建立電影與演員的關聯
				MovieActor movieActor = new MovieActor(localMovie, actor, castDto.getCharacter(),
						castDto.getOrderNum());
				movieActorRepository.save(movieActor);
				System.out.println("關聯電影 " + localMovie.getId() + " 與演員 " + actor.getName() + " (角色: "
						+ castDto.getCharacter() + ")");
			}
		}
	}

//    /**
//     * 根據日期範圍和地區批量匯入電影資料到 TMDBMovie 資料表
//     * @param startDate 上映起始日期 (e.g., 2025-01-01)
//     * @param endDate 上映結束日期 (e.g., 2025-12-31)
//     * @param region 電影上映地區
//     * @param page 
//     */
//    @Transactional
//    public void importMoviesByDateRangeAndRegion(LocalDate startDate, LocalDate endDate, String region,Integer page) {
//        TMDBMovieSearchResponseDTO response = tmdbApiService.discoverMoviesByDateRangeAndRegion(startDate, endDate, region,page);
//
//        if (response == null || response.getResults() == null) {
//            System.err.println("無法匯入電影，因為 API 回傳結果為空。");
//            return;
//        }
//
//        System.out.printf("發現 %d 部符合條件的電影，開始匯入...\n", response.getResults().size());
//
//        for (TMDBMovieDTO TMDBMovieDto : response.getResults()) {
//            if (TMDBMovieDto.getId() != null) {
//                importMovieData(TMDBMovieDto.getId());
//            }
//        }
//    }
	// 檔案路徑: com/flux/movieproject/service/movie/TMDBDataImportService.java
	@Transactional
	// 【修正1】將返回類型從 void 改為 TMDBMovieSearchResponseDTO
	public TMDBMovieSearchResponseDTO importMoviesByDateRangeAndRegion(LocalDate startDate, LocalDate endDate,
			String region, Integer page) {
		TMDBMovieSearchResponseDTO response = tmdbApiService.discoverMoviesByDateRangeAndRegion(startDate, endDate,
				region, page);

		if (response == null || response.getResults() == null) {
			System.err.println("無法匯入電影，因為 API 回傳結果為空。");

			// 雖然結果為空，但最好還是回傳一個空的 response 物件，避免讓 Controller 收到 null
			// return new TMDBMovieSearchResponseDTO();
			// 或者直接回傳 null，讓 Controller 處理
			return null;
		}

		System.out.printf("發現 %d 部符合條件的電影，開始匯入...\n", response.getResults().size());

		for (TMDBMovieDTO tmdbMovieDto : response.getResults()) {
			if (tmdbMovieDto.getId() != null) {
				importMovieData(tmdbMovieDto.getId());
			}
		}

		// 【修正2】在方法的最後，將 response 物件回傳出去
		return response;
	}

	/**
	 * 根據海報路徑下載圖片
	 * 
	 * @param posterPath TMDB 的海報路徑
	 * @return 圖片的二進位資料
	 */
	private byte[] downloadPosterImage(String posterPath) {
		String imageUrl = imageHost + posterPath;
		ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);
		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			return response.getBody();
		}
		throw new RuntimeException("無法下載圖片：" + imageUrl);
	}

	/**
	 * 刪除 TMDBMovie 資料表中的所有資料 (保留欄位)
	 */
	@Transactional
	public void clearAllTmdbMovies() {
		try {
			tmdbMovieRepository.deleteAll();
			System.out.println("成功刪除 TMDBMovie 資料表中的所有資料。");
		} catch (Exception e) {
			System.err.println("刪除 TMDBMovie 資料時發生錯誤：" + e.getMessage());
			throw new RuntimeException("無法清除 TMDBMovie 資料：" + e.getMessage());
		}
	}

	/**
	 * 將所有 TMDBMovie 的電影匯入到本地 Movie 表 這個方法會遍歷所有 TMDBMovie 資料，並逐一匯入到本地 Movie 表
	 */
	@Transactional
	public void importAllTmdbMoviesToLocal() {
		List<TMDBMovie> allTmdbMovies = tmdbMovieRepository.findAll();
		System.out.printf("發現 %d 部 TMDBMovie 資料，開始批量匯入到本地 Movie 表...\n", allTmdbMovies.size());

		for (TMDBMovie tmdbMovie : allTmdbMovies) {
			try {
				// 這裡會呼叫 importToLocalMovieTable，該方法內部已包含存在性檢查和略過邏輯
				importToLocalMovieTable(tmdbMovie.getTmdbMovieId());
			} catch (Exception e) {
				System.err.println("批量匯入電影 ID: " + tmdbMovie.getTmdbMovieId() + " 到本地 Movie 表時發生錯誤：" + e.getMessage());
				// 這裡可以選擇是否拋出異常中斷，或者繼續處理其他電影
				// throw new RuntimeException("批量匯入失敗，請檢查日誌", e);
			}
		}
		System.out.println("所有 TMDBMovie 資料批量匯入到本地 Movie 表完成。");
	}

	/**
	 * 取得所有 TMDBMovie 資料表中的電影資料
	 * 
	 * @return 包含所有 TMDBMovie 資料的列表
	 */
	public List<TMDBMovie> getAllTmdbMovies() {
		return tmdbMovieRepository.findAll();
	}

	/**
	 * 將選定的 TMDBMovie 電影匯入到本地 Movie 表
	 * 
	 * @param tmdbMovieIds 要匯入的 TMDB 電影 ID 列表
	 */
	@Transactional
	public void importSelectedTmdbMovies(List<Integer> tmdbMovieIds) {
		System.out.printf("收到 %d 個選定的 TMDBMovie ID，開始匯入到本地 Movie 表...\n", tmdbMovieIds.size());
		for (Integer tmdbMovieId : tmdbMovieIds) {
			try {
				importToLocalMovieTable(tmdbMovieId); // 呼叫現有的匯入單一電影方法
			} catch (Exception e) {
				System.err.println("匯入選定電影 ID: " + tmdbMovieId + " 到本地 Movie 表時發生錯誤：" + e.getMessage());
				// 這裡可以選擇是否拋出異常中斷，或者繼續處理其他電影
			}
		}
		System.out.println("選定的 TMDBMovie 資料匯入到本地 Movie 表完成。");
	}

	/**
	 * 【新方法】取得所有 TMDBMovie 並轉換為 DTO
	 * 
	 * @return 包含所有電影資料 DTO 的列表
	 */
	@Transactional(readOnly = true) // 加上 Transactional 確保在轉換過程中 session 是開啟的，才能讀取延遲載入的屬性
	public List<TMDBMovieResponseDTO> getAllTmdbMoviesAsDTO() {
		List<TMDBMovie> movies = tmdbMovieRepository.findAll();
		// 使用 Java Stream API 來進行轉換，非常優雅
		return movies.stream().map(this::convertToDto) // 對每個 movie 執行 convertToDto 方法
				.collect(Collectors.toList()); // 將結果收集成一個 List
	}

	/**
	 * 輔助方法：將單一 TMDBMovie Entity 轉換為 TMDBMovieResponseDTO
	 * 
	 * @param movie Entity 物件
	 * @return DTO 物件
	 */
	private TMDBMovieResponseDTO convertToDto(TMDBMovie movie) {
		TMDBMovieResponseDTO dto = new TMDBMovieResponseDTO();

		// 1. 複製基本屬性
		dto.setTmdbMovieId(movie.getTmdbMovieId());
		dto.setTitleLocal(movie.getTitleLocal());
		dto.setTitleEnglish(movie.getTitleEnglish());
		dto.setReleaseDate(movie.getReleaseDate());
		// 1. 獲取原始分級
		String rawCertification = movie.getCertification();
		// 2. 呼叫輔助方法進行標準化
		String standardizedCert = standardizeCertification(rawCertification);
		// 3. 設定標準化後的分級
		dto.setCertification(standardizedCert);
		dto.setOverview(movie.getOverview());
		dto.setTrailerUrl(movie.getTrailerUrl());
		dto.setDurationMinutes(movie.getDurationMinutes());
		dto.setOriginalLanguage(movie.getOriginalLanguage());
		dto.setPopularity(movie.getPopularity());
		dto.setVoteAverage(movie.getVoteAverage());
		dto.setVoteCount(movie.getVoteCount());
		dto.setCreateTime(movie.getCreateTime());
		dto.setUpdateTime(movie.getUpdateTime());

		byte[] posterImageBytes = movie.getPosterImage();
		if (posterImageBytes != null && posterImageBytes.length > 0) {
			// 將 byte[] 陣列進行 Base64 編碼
			String base64EncodedImage = Base64.getEncoder().encodeToString(posterImageBytes);

			// 組合成瀏覽器看得懂的 Data URI 格式
			// 格式為 data:[<MIME type>][;base64],<data>
			// 假設圖片都是 jpeg 格式
			dto.setPosterImageBase64("data:image/jpeg;base64," + base64EncodedImage);
		} else {
			// 如果沒有圖片，可以給 null 或一個預設的 Base64 圖片
			dto.setPosterImageBase64(null);
		}

		// 2. 轉換關聯的 Director
		// 這裡會觸發 Hibernate 的延遲載入，去資料庫撈取導演資料
//		Set<DirectorDTO> directorDTOs = movie.getTmdbMovieDirectors().stream().map(tmdbMovieDirector -> {
//			DirectorDTO directorDto = new DirectorDTO();
//			// 注意：要從中間表取出真正的 Director 物件
//			directorDto.setTmdbDirectorId(tmdbMovieDirector.getTmdbDirector().getTmdbDirectorId());
//			directorDto.setName(tmdbMovieDirector.getTmdbDirector().getName());
//			return directorDto;
//		}).collect(Collectors.toSet());
//		dto.setDirectors(directorDTOs);

		// 3. 轉換關聯的 Genre
//		Set<GenreDTO> genreDTOs = movie.getTmdbMovieGenres().stream().map(tmdbMovieGenre -> {
//			GenreDTO genreDto = new GenreDTO();
//			genreDto.setTmdbGenreId(tmdbMovieGenre.getTmdbGenre().getTmdbGenreId());
//			genreDto.setName(tmdbMovieGenre.getTmdbGenre().getName());
//			return genreDto;
//		}).collect(Collectors.toSet());
//		dto.setGenres(genreDTOs);

		// 4. 轉換關聯的 Actor
//		Set<ActorDTO> actorDTOs = movie.getTmdbMovieActors().stream().map(tmdbMovieActor -> {
//			ActorDTO actorDto = new ActorDTO();
//			actorDto.setTmdbActorId(tmdbMovieActor.getTmdbActor().getTmdbActorId());
//			actorDto.setName(tmdbMovieActor.getTmdbActor().getName());
//			actorDto.setCharacter(tmdbMovieActor.getCharacter());
//			actorDto.setOrderNum(tmdbMovieActor.getOrderNum());
//			return actorDto;
//		}).collect(Collectors.toSet());
//		dto.setActors(actorDTOs);

		return dto;
	}

	/**
	 * 【新增的輔助方法】 根據 TMDB 的原始分級文字，將其標準化為前端使用的代碼。
	 * 
	 * @param rawCertification 從 tmdb_movie 表讀取到的原始分級字串
	 * @return 標準化後的分級代碼 (G, P, PG, R, N)
	 */
	private String standardizeCertification(String rawCertification) {
		String defaultValue = "待確定"; // 預設為 "待確定"

		if (rawCertification == null || rawCertification.trim().isEmpty()) {
			return defaultValue;
		}

		String upperCaseCert = rawCertification.toUpperCase();

		if (upperCaseCert.contains("普遍級") || upperCaseCert.contains("普") || upperCaseCert.equals("G")) {
			return "普遍級";
		}
		if (upperCaseCert.contains("保護級") || upperCaseCert.contains("護") || upperCaseCert.contains("P")
				|| upperCaseCert.contains("6+")) {
			return "保護級";
		}
		if (upperCaseCert.contains("輔導級") || upperCaseCert.contains("輔") || upperCaseCert.contains("PG")
				|| upperCaseCert.contains("12+") || upperCaseCert.contains("15+")) {
			return "輔導級";
		}
		if (upperCaseCert.contains("限制級") || upperCaseCert.contains("限") || upperCaseCert.contains("R")
				|| upperCaseCert.contains("18+")) {
			return "限制級";
		}

		return defaultValue;
	}

}
