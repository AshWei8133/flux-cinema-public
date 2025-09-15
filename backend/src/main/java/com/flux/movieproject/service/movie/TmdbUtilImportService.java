package com.flux.movieproject.service.movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.entity.movie.TMDBActor;
import com.flux.movieproject.model.entity.movie.TMDBDirector;
import com.flux.movieproject.model.entity.movie.TMDBGenre;
import com.flux.movieproject.model.entity.movie.TMDBMovie;
import com.flux.movieproject.model.entity.movie.TMDBMovieActor;
import com.flux.movieproject.model.entity.movie.TMDBMovieActorId;
import com.flux.movieproject.model.entity.movie.TMDBMovieDirector;
import com.flux.movieproject.model.entity.movie.TMDBMovieDirectorId;
import com.flux.movieproject.model.entity.movie.TMDBMovieGenre;
import com.flux.movieproject.model.entity.movie.TMDBMovieGenreId;
import com.flux.movieproject.model.pojo.TMDBActorPojo;
import com.flux.movieproject.model.pojo.TMDBDirectorPojo;
import com.flux.movieproject.model.pojo.TMDBMoviePojo;
import com.flux.movieproject.repository.movie.TMDBActorRepository;
import com.flux.movieproject.repository.movie.TMDBDirectorRepository;
import com.flux.movieproject.repository.movie.TMDBGenreRepository;
import com.flux.movieproject.repository.movie.TMDBMovieActorRepository;
import com.flux.movieproject.repository.movie.TMDBMovieDirectorRepository;
import com.flux.movieproject.repository.movie.TMDBMovieGenreRepository;
import com.flux.movieproject.repository.movie.TMDBMovieRepository;
import com.flux.movieproject.utils.FetchTMDBDataUtil;

import jakarta.annotation.PostConstruct;

/**
 * 使用 FetchTMDBDataUtil 工具類來執行電影匯入的專用服務。 這個服務扮演著「橋樑」和「總指揮」的角色，負責： 1. 呼叫工具類從 TMDB
 * API 獲取經過處理的原始資料 (POJO)。 2. 將 POJO 資料轉換為可存入資料庫的 JPA 實體 (Entity)。 3.
 * 處理電影與導演、演員、類型之間的複雜關聯。
 */
@Service
public class TmdbUtilImportService {

	// --- 注入所有操作資料庫所需的 Repository ---
	@Autowired
	private TMDBMovieRepository tmdbMovieRepository;
	@Autowired
	private TMDBDirectorRepository tmdbDirectorRepository;
	@Autowired
	private TMDBActorRepository tmdbActorRepository;
	@Autowired
	private TMDBGenreRepository tmdbGenreRepository;
	@Autowired
	private TMDBMovieDirectorRepository tmdbMovieDirectorRepository;
	@Autowired
	private TMDBMovieActorRepository tmdbMovieActorRepository;
	@Autowired
	private TMDBMovieGenreRepository tmdbMovieGenreRepository;

	// 注入我們信賴的資料抓取工具類
	@Autowired
	private FetchTMDBDataUtil fetchTMDBDataUtil;

	// 用來緩存類型ID與名稱的對照表，避免重複查詢API
	private Map<Integer, String> genreMap;

	/**
	 * @PostConstruct 註解確保此方法在 Service 物件被建立並完成依賴注入後，立刻執行一次。
	 *                我們在這裡初始化電影類型對照表，這樣就不需要在每次匯入時都去呼叫API，提升效率。
	 */
	@PostConstruct
	public void init() {
		System.out.println("正在初始化電影類型對照表...");
		this.genreMap = FetchTMDBDataUtil.fetchGenresMap();
		System.out.println("電影類型對照表初始化完成！");
	}

	/**
	 * 執行電影批次匯入的主方法。 整個方法由 @Transactional 包裹，確保所有資料庫操作要麼全部成功，要麼全部失敗回滾，保證資料一致性。
	 * 
	 * @param startDate 匯入的起始日期
	 * @param endDate   匯入的結束日期
	 */
	@Transactional
	public void importMoviesUsingUtility(LocalDate startDate, LocalDate endDate) {
		// 1. 呼叫工具類，獲取經過完整處理的原始 POJO 數據列表
		List<TMDBMoviePojo> pojoList = fetchTMDBDataUtil.fetchMovies(startDate, endDate);
		if (pojoList == null || pojoList.isEmpty()) {
			System.out.println("Service: 工具類未回傳任何電影資料，任務結束。");
			return;
		}

		System.out.printf("Service: 工具類成功抓取 %d 部電影，開始轉換並存入資料庫...\n", pojoList.size());

		// 2. 遍歷從API抓回來的POJO列表，逐一處理
		for (TMDBMoviePojo pojo : pojoList) {
			// 3. "查找或創建"電影實體：嘗試用ID從資料庫找，如果找不到(orElseGet)，就創建一個新的
			TMDBMovie entity = tmdbMovieRepository.findById(pojo.getTmdbMovieId()).orElseGet(() -> {
				TMDBMovie newEntity = new TMDBMovie();
				newEntity.setTmdbMovieId(pojo.getTmdbMovieId());
				newEntity.setCreateTime(LocalDateTime.now());
				return newEntity;
			});

			// 4. 將 POJO 的資料映射到 Entity 上
			mapPojoToEntity(pojo, entity);

			// 5. 先儲存電影本身，確保它在資料庫中存在，這樣後續才能建立關聯
			tmdbMovieRepository.save(entity);

			// 6. 分別處理電影的關聯資料（導演、演員、類型）
			handleDirectors(entity, pojo.getDirectors());
			handleActors(entity, pojo.getActors());
			handleGenres(entity, pojo.getGenreIds());
		}

		System.out.println("Service: 所有電影資料及關聯已透過工具類成功匯入資料庫！");
	}

	/**
	 * 一個輔助方法，專門負責將 POJO 的欄位值複製到 Entity 的對應欄位。
	 * 
	 * @param pojo   資料來源 (POJO)
	 * @param entity 資料目標 (Entity)
	 */
	private void mapPojoToEntity(TMDBMoviePojo pojo, TMDBMovie entity) {
		entity.setTitleLocal(pojo.getTitleLocal());
		entity.setTitleEnglish(pojo.getTitleEnglish());
		entity.setReleaseDate(pojo.getReleaseDate());
		entity.setOverview(pojo.getOverview());
		entity.setCertification(pojo.getCertification());
		entity.setPosterImage(pojo.getPosterImage());
		entity.setDurationMinutes(pojo.getDurationMinutes());
		entity.setTrailerUrl(pojo.getTrailerUrl());
		entity.setOriginalLanguage(pojo.getOriginalLanguage());
		entity.setPopularity(pojo.getPopularity());
		entity.setVoteAverage(pojo.getVoteAverage());
		entity.setVoteCount(pojo.getVoteCount());
		entity.setUpdateTime(LocalDateTime.now());
	}

	/**
	 * 處理一部電影與多位導演的關聯關係。
	 * 
	 * @param movieEntity   已存入資料庫的電影實體
	 * @param directorPojos 從API抓取的導演POJO列表
	 */
	private void handleDirectors(TMDBMovie movieEntity, List<TMDBDirectorPojo> directorPojos) {
		if (directorPojos == null)
			return;

		// 為了確保資料同步且可重複執行(idempotency)，先刪除此電影所有的舊導演關聯
		tmdbMovieDirectorRepository.deleteByTmdbMovie(movieEntity);

		for (TMDBDirectorPojo pojo : directorPojos) {
			// "查找或創建" 導演主檔紀錄
			TMDBDirector directorEntity = tmdbDirectorRepository.findById(pojo.getId()).orElseGet(() -> {
				TMDBDirector newDirector = new TMDBDirector();
				newDirector.setTmdbDirectorId(pojo.getId());
				newDirector.setName(pojo.getName());
				return tmdbDirectorRepository.save(newDirector); // 存入 tmdb_director 表
			});

			// 創建中間表 (Join Table) 的紀錄來建立電影和導演的關聯
			TMDBMovieDirectorId movieDirectorId = new TMDBMovieDirectorId();
			movieDirectorId.setTmdbMovieId(movieEntity.getTmdbMovieId());
			movieDirectorId.setTmdbDirectorId(directorEntity.getTmdbDirectorId());

			TMDBMovieDirector movieDirector = new TMDBMovieDirector();
			movieDirector.setId(movieDirectorId);
			movieDirector.setTmdbMovie(movieEntity);
			movieDirector.setTmdbDirector(directorEntity);
			tmdbMovieDirectorRepository.save(movieDirector); // 存入 tmdb_movie_director 表
		}
	}

	/**
	 * 處理一部電影與多位演員的關聯關係。
	 * 
	 * @param movieEntity 已存入資料庫的電影實體
	 * @param actorPojos  從API抓取的演員POJO列表
	 */
	private void handleActors(TMDBMovie movieEntity, List<TMDBActorPojo> actorPojos) {
		if (actorPojos == null)
			return;

		// 先清除舊的演員關聯
		tmdbMovieActorRepository.deleteByTmdbMovie(movieEntity);

		for (TMDBActorPojo pojo : actorPojos) {
			// "查找或創建" 演員主檔紀錄
			TMDBActor actorEntity = tmdbActorRepository.findById(pojo.getId()).orElseGet(() -> {
				TMDBActor newActor = new TMDBActor();
				newActor.setTmdbActorId(pojo.getId());
				newActor.setName(pojo.getName());
				return tmdbActorRepository.save(newActor); // 存入 tmdb_actor 表
			});

			// 創建中間表的紀錄
			TMDBMovieActorId movieActorId = new TMDBMovieActorId();
			movieActorId.setTmdbMovieId(movieEntity.getTmdbMovieId());
			movieActorId.setTmdbActorId(actorEntity.getTmdbActorId());

			TMDBMovieActor movieActor = new TMDBMovieActor();
			movieActor.setId(movieActorId);
			movieActor.setTmdbMovie(movieEntity);
			movieActor.setTmdbActor(actorEntity);
			// 中間表還有額外的欄位：角色名稱和順序
			movieActor.setCharacter(pojo.getCharacter());
			movieActor.setOrderNum(pojo.getOrder());
			tmdbMovieActorRepository.save(movieActor); // 存入 tmdb_movie_actor 表
		}
	}

	/**
	 * 處理一部電影與多種類型的關聯關係。
	 * 
	 * @param movieEntity 已存入資料庫的電影實體
	 * @param genreIds    從API抓取的類型ID列表
	 */
	private void handleGenres(TMDBMovie movieEntity, List<Integer> genreIds) {
		if (genreIds == null)
			return;

		// 先清除舊的類型關聯
		tmdbMovieGenreRepository.deleteByTmdbMovie(movieEntity);

		for (Integer genreId : genreIds) {
			// "查找或創建" 類型主檔紀錄
			TMDBGenre genreEntity = tmdbGenreRepository.findById(genreId).orElseGet(() -> {
				TMDBGenre newGenre = new TMDBGenre();
				newGenre.setTmdbGenreId(genreId);
				// 從我們在 init() 初始化的 map 中查找類型名稱
				newGenre.setName(genreMap.getOrDefault(genreId, "未知類型"));
				return tmdbGenreRepository.save(newGenre); // 存入 tmdb_genre 表
			});

			// 創建中間表的紀錄
			TMDBMovieGenreId movieGenreId = new TMDBMovieGenreId();
			movieGenreId.setTmdbMovieId(movieEntity.getTmdbMovieId());
			movieGenreId.setTmdbGenreId(genreEntity.getTmdbGenreId());

			TMDBMovieGenre movieGenre = new TMDBMovieGenre();
			movieGenre.setId(movieGenreId);
			movieGenre.setTmdbMovie(movieEntity);
			movieGenre.setTmdbGenre(genreEntity);
			tmdbMovieGenreRepository.save(movieGenre); // 存入 tmdb_movie_genre 表
		}
	}
}