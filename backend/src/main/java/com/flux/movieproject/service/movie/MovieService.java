package com.flux.movieproject.service.movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.model.dto.movie.ActorDetailDTO;
import com.flux.movieproject.model.dto.movie.ActorListDTO;
import com.flux.movieproject.model.dto.movie.DirectorDetailDTO;
import com.flux.movieproject.model.dto.movie.DirectorListDTO;
import com.flux.movieproject.model.dto.movie.GenreDetailDTO;
import com.flux.movieproject.model.dto.movie.GenreListDTO;
import com.flux.movieproject.model.dto.movie.MovieActorRoleDTO;
import com.flux.movieproject.model.dto.movie.MovieListResponseDTO;
import com.flux.movieproject.model.dto.movie.MoviePublicDetailDTO;
import com.flux.movieproject.model.dto.movie.MovieResponseDTO;
import com.flux.movieproject.model.dto.movie.GenreResponseDTO;
import com.flux.movieproject.model.dto.movie.MovieSimpleDTO;
import com.flux.movieproject.model.dto.movie.MoviesAllDataDTO;
import com.flux.movieproject.model.entity.movie.Actor;
import com.flux.movieproject.model.entity.movie.Director;
import com.flux.movieproject.model.entity.movie.Genre;
import com.flux.movieproject.model.entity.movie.Movie;
import com.flux.movieproject.model.entity.movie.MovieActor;
import com.flux.movieproject.model.entity.movie.MovieGenre;
import com.flux.movieproject.repository.movie.ActorRepository;
import com.flux.movieproject.repository.movie.DirectorRepository;
import com.flux.movieproject.repository.movie.GenreRepository;
import com.flux.movieproject.repository.movie.MovieRepository;

import jakarta.persistence.EntityNotFoundException;

import com.flux.movieproject.model.entity.movie.MovieActorId;
import com.flux.movieproject.model.entity.movie.MovieDirector;
import com.flux.movieproject.model.entity.movie.MovieGenre;
import com.flux.movieproject.repository.movie.MovieActorRepository;
import com.flux.movieproject.repository.movie.MovieGenreRepository;
import com.flux.movieproject.repository.movie.MovieRepository;

@Service
public class MovieService {

	private final MovieRepository movieRepo;
	private final DirectorRepository directorRepo;
	private final ActorRepository actorRepo;
	private final GenreRepository genreRepo;
	private final MovieActorRepository movieActorRepo;
	private final MovieGenreRepository movieGenreRepo;

	@Autowired
	public MovieService(MovieRepository movieRepo, DirectorRepository directorRepo, ActorRepository actorRepo,
			GenreRepository genreRepo, MovieActorRepository movieActorRepo, MovieGenreRepository movieGenreRepo) {
		this.movieRepo = movieRepo;
		this.directorRepo = directorRepo;
		this.actorRepo = actorRepo;
		this.genreRepo = genreRepo;
		this.movieActorRepo = movieActorRepo;
		this.movieGenreRepo = movieGenreRepo;
	}
	/**
     * 查詢所有電影，並將其映射為 MovieResponseDto 列表
     * @return 所有電影的 MovieResponseDto 列表
     */
    @Transactional(readOnly = true)
    public List<MovieResponseDTO> getAllMovies() {
        List<Movie> movies = movieRepo.findAll(); // @EntityGraph 會在此處預先載入 movieGenres
        
        return movies.stream().map(movie -> {
            MovieResponseDTO dto = new MovieResponseDTO();
            dto.setId(movie.getId());
            dto.setTmdbMovieId(movie.getTmdbMovie() != null ? movie.getTmdbMovie().getTmdbMovieId() : null);
            dto.setTitleLocal(movie.getTitleLocal());
            dto.setTitleEnglish(movie.getTitleEnglish());
            dto.setReleaseDate(movie.getReleaseDate());
            dto.setOffShelfDate(movie.getOffShelfDate());
            dto.setCertification(movie.getCertification());
            dto.setOverview(movie.getOverview());
            dto.setTrailerUrl(movie.getTrailerUrl());
            dto.setDurationMinutes(movie.getDurationMinutes());
            // dto.setPosterImage(movie.getPosterImage()); // 不直接在 DTO 中回傳 byte[]
            dto.setOriginalLanguage(movie.getOriginalLanguage());
            dto.setPopularity(movie.getPopularity());
            dto.setVoteAverage(movie.getVoteAverage());
            dto.setVoteCount(movie.getVoteCount());
            dto.setStatus(movie.getStatus());
            // 預覽圖片也不直接回傳 byte[]
            dto.setCreateTime(movie.getCreateTime());
            dto.setUpdateTime(movie.getUpdateTime());

            // 映射關聯類型
            if (movie.getMovieGenres() != null) {
                dto.setGenres(movie.getMovieGenres().stream()
                        .map(movieGenre -> {
                            Genre genre = movieGenre.getGenre();
                            GenreResponseDTO genreDto = new GenreResponseDTO();
                            genreDto.setGenreId(genre.getGenreId());
                            genreDto.setTmdbGenreId(genre.getTmdbGenreId());
                            genreDto.setName(genre.getName());
                            return genreDto;
                        })
                        .collect(Collectors.toList()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 【已修正】根據電影 ID 更新電影資料 (真正的部分更新 - PATCH)
     * 這個版本只會更新在 'updates' Map 中有出現的欄位。
     * @param id      要更新的電影 ID
     * @param updates 一個 Map，只包含需要被更新的欄位和對應的新值
     * @return 更新後的電影資料
     */
    @Transactional
    public Optional<Movie> updateMovieById(Integer id, Map<String, Object> updates) {
        // 1. 根據 ID 從資料庫找出既有的電影物件
        return movieRepo.findById(id).map(existingMovie -> {
            
            // 2. 【核心修正】我們現在逐一檢查 'updates' Map 中有哪些 key
            //    只有當 key 存在時，我們才去更新對應的欄位
            
            if (updates.containsKey("titleLocal")) {
                existingMovie.setTitleLocal((String) updates.get("titleLocal"));
            }
            if (updates.containsKey("titleEnglish")) {
                existingMovie.setTitleEnglish((String) updates.get("titleEnglish"));
            }
            if (updates.containsKey("releaseDate")) {
                // 日期需要從 String 轉換成 LocalDate
                existingMovie.setReleaseDate(LocalDate.parse((String) updates.get("releaseDate")));
            }
            if (updates.containsKey("offShelfDate")) {
                Object dateValue = updates.get("offShelfDate");
                existingMovie.setOffShelfDate(dateValue != null ? LocalDate.parse((String) dateValue) : null);
            }
            if (updates.containsKey("certification")) {
                existingMovie.setCertification((String) updates.get("certification"));
            }
            if (updates.containsKey("overview")) {
                existingMovie.setOverview((String) updates.get("overview"));
            }
            if (updates.containsKey("trailerUrl")) {
                existingMovie.setTrailerUrl((String) updates.get("trailerUrl"));
            }
            if (updates.containsKey("durationMinutes")) {
                existingMovie.setDurationMinutes((Integer) updates.get("durationMinutes"));
            }
            if (updates.containsKey("status")) {
                existingMovie.setStatus((Boolean) updates.get("status"));
            }

            // 處理圖片欄位，需要將 Base64 字串解碼回 byte[]
            updateImageField(updates, "posterImage", existingMovie::setPosterImage);
            updateImageField(updates, "previewImage1", existingMovie::setPreviewImage1);
            updateImageField(updates, "previewImage2", existingMovie::setPreviewImage2);
            updateImageField(updates, "previewImage3", existingMovie::setPreviewImage3);
            updateImageField(updates, "previewImage4", existingMovie::setPreviewImage4);

            // 3. 更新「更新時間」
            existingMovie.setUpdateTime(LocalDateTime.now());

            // 4. 因為這個方法有 @Transactional 註解，JPA 會自動儲存變更
            return existingMovie;
        });
    }
    
    // 輔助方法，專門用來處理圖片更新的邏輯
    private void updateImageField(Map<String, Object> updates, String fieldName, java.util.function.Consumer<byte[]> setter) {
        if (updates.containsKey(fieldName)) {
            Object value = updates.get(fieldName);
            if (value instanceof String) {
                // 如果傳來的是 Base64 字串，就解碼
                setter.accept(Base64.getDecoder().decode((String) value));
            } else if (value == null) {
                // 如果傳來的是 null，就清空欄位
                setter.accept(null);
            }
        }
        // 如果 key 不存在，就什麼都不做，保持原樣
    }

	/**
	 * 根據電影 ID 刪除電影資料
	 * 
	 * @param id 要刪除的電影 ID
	 * @return true 如果成功刪除，false 如果找不到或刪除失敗
	 */
	@Transactional
	public boolean deleteMovieById(Integer id) {
		if (movieRepo.existsById(id)) {
			movieRepo.deleteById(id);
			return true;
		}
		return false;
	}
	// --- Director CRUD ---

	/**
	 * 新增導演
	 * 
	 * @param director 要新增的導演實體
	 * @return 新增後的導演實體
	 */
	@Transactional
	public Director createDirector(Director director) {
		return directorRepo.save(director);
	}

	/**
	 * 根據 ID 查詢導演
	 * 
	 * @param id 導演 ID
	 * @return 導演實體或 Optional.empty()
	 */
	public Optional<Director> getDirectorById(Integer id) {
		return directorRepo.findById(id);
	}

	/**
	 * 查詢所有導演，並包含其關聯電影的簡化資訊
	 * 
	 * @return 所有導演的 DirectorListDto 列表
	 */
	@Transactional(readOnly = true)
	public List<DirectorListDTO> getAllDirectors() {
		return directorRepo.findAll().stream().map(director -> {
			DirectorListDTO dto = new DirectorListDTO();
			dto.setDirectorId(director.getDirectorId());
			dto.setTmdbDirectorId(director.getTmdbDirectorId());
			dto.setName(director.getName());
			dto.setDirectorSummary(director.getDirectorSummary());

			// 映射關聯電影 (MovieDirector -> Movie -> MovieSimpleDto)
			// 這裡會觸發延遲載入，所以需要 @Transactional
			if (director.getMovieDirectors() != null) {
				dto.setAssociatedMovies(director.getMovieDirectors().stream().map(movieDirector -> {
					Movie movie = movieDirector.getMovie(); // 獲取關聯的 Movie 實體
					MovieSimpleDTO movieDto = new MovieSimpleDTO();
					movieDto.setId(movie.getId());
					movieDto.setTitleLocal(movie.getTitleLocal());
					movieDto.setTitleEnglish(movie.getTitleEnglish());
					movieDto.setReleaseDate(movie.getReleaseDate());
					return movieDto;
				}).collect(Collectors.toList()));
			}
			return dto;
		}).collect(Collectors.toList());
	}

	/**
	 * 更新導演資料 (只更新非 null 的欄位)
	 * 
	 * @param id              要更新的導演 ID
	 * @param updatedDirector 包含新資料的 Director 物件
	 * @return 更新後的導演實體或 Optional.empty()
	 */
	@Transactional
	public Optional<Director> updateDirector(Integer id, Director updatedDirector) {
		Optional<Director> directorOptional = directorRepo.findById(id);
		if (directorOptional.isPresent()) {
			Director existingDirector = directorOptional.get();
			if (updatedDirector.getName() != null) {
				existingDirector.setName(updatedDirector.getName());
			}
			if (updatedDirector.getDirectorSummary() != null) {
				existingDirector.setDirectorSummary(updatedDirector.getDirectorSummary());
			}
			// tmdbDirectorId 通常不更新，因為它是外部系統的 ID
			return Optional.of(directorRepo.save(existingDirector));
		}
		return Optional.empty();
	}

	/**
	 * 根據 ID 刪除導演
	 * 
	 * @param id 導演 ID
	 * @return true 如果成功刪除，false 如果找不到
	 */
	@Transactional
	public boolean deleteDirector(Integer id) {
		if (directorRepo.existsById(id)) {
			directorRepo.deleteById(id);
			return true;
		}
		return false;
	}

	/**
	 * 根據 ID 查詢導演的詳細資訊，包含關聯的電影列表
	 * 
	 * @param id 導演 ID
	 * @return 導演詳細資訊 DTO 或 Optional.empty()
	 */
	@Transactional(readOnly = true)
	public Optional<DirectorDetailDTO> getDirectorDetail(Integer id) {
		return directorRepo.findById(id).map(director -> {
			DirectorDetailDTO dto = new DirectorDetailDTO();
			dto.setDirectorId(director.getDirectorId());
			dto.setTmdbDirectorId(director.getTmdbDirectorId());
			dto.setName(director.getName());
			dto.setDirectorSummary(director.getDirectorSummary());

			// 映射關聯電影
			// 這裡會觸發延遲載入，所以需要 @Transactional
			if (director.getMovieDirectors() != null) {
				dto.setAssociatedMovies(director.getMovieDirectors().stream().map(movieDirector -> {
					Movie movie = movieDirector.getMovie(); // 獲取關聯的 Movie 實體
					MovieSimpleDTO movieDto = new MovieSimpleDTO();
					movieDto.setId(movie.getId());
					movieDto.setTitleLocal(movie.getTitleLocal());
					movieDto.setTitleEnglish(movie.getTitleEnglish());
					movieDto.setReleaseDate(movie.getReleaseDate());
					return movieDto;
				}).collect(Collectors.toList()));
			}
			return dto;
		});
	}

	// --- Actor CRUD ---

	/**
	 * 新增演員
	 * 
	 * @param actor 要新增的演員實體
	 * @return 新增後的演員實體
	 */
	@Transactional
	public Actor createActor(Actor actor) {
		return actorRepo.save(actor);
	}

	/**
	 * 根據 ID 查詢演員
	 * 
	 * @param id 演員 ID
	 * @return 演員實體或 Optional.empty()
	 */
	public Optional<Actor> getActorById(Integer id) {
		return actorRepo.findById(id);
	}

	/**
	 * 查詢所有演員
	 * 
	 * @return 所有演員的列表
	 */
	@Transactional(readOnly = true) // 修正點：加上 @Transactional(readOnly = true)
	public List<ActorListDTO> getAllActors() { // 修正點：回傳 ActorListDto 列表
		return actorRepo.findAll().stream().map(actor -> {
			ActorListDTO dto = new ActorListDTO();
			dto.setActorId(actor.getActorId());
			dto.setTmdbActorId(actor.getTmdbActorId());
			dto.setName(actor.getName());
			dto.setBiography(actor.getBiography());

			// 映射關聯電影及角色
			// 這裡會觸發延遲載入，所以需要 @Transactional
			if (actor.getMovieActors() != null) {
				dto.setAssociatedMovies(actor.getMovieActors().stream().map(movieActor -> {
					Movie movie = movieActor.getMovie();
					MovieActorRoleDTO roleDto = new MovieActorRoleDTO();
					roleDto.setMovieId(movie.getId());
					roleDto.setMovieTitleLocal(movie.getTitleLocal());
					roleDto.setMovieTitleEnglish(movie.getTitleEnglish());
					roleDto.setMovieReleaseDate(movie.getReleaseDate());
					roleDto.setCharacterName(movieActor.getCharacterName()); // 獲取飾演角色名
					roleDto.setOrderNum(movieActor.getOrderNum()); // 獲取出場順序
					return roleDto;
				}).collect(Collectors.toList()));
			}
			return dto;
		}).collect(Collectors.toList());
	}

	/**
	 * 更新演員資料 (只更新非 null 的欄位)
	 * 
	 * @param id           要更新的演員 ID
	 * @param updatedActor 包含新資料的 Actor 物件
	 * @return 更新後的演員實體或 Optional.empty()
	 */
	@Transactional
	public Optional<Actor> updateActor(Integer id, Actor updatedActor) {
		Optional<Actor> actorOptional = actorRepo.findById(id);
		if (actorOptional.isPresent()) {
			Actor existingActor = actorOptional.get();
			if (updatedActor.getName() != null) {
				existingActor.setName(updatedActor.getName());
			}
			if (updatedActor.getBiography() != null) {
				existingActor.setBiography(updatedActor.getBiography());
			}
			return Optional.of(actorRepo.save(existingActor));
		}
		return Optional.empty();
	}

	/**
	 * 根據 ID 刪除演員
	 * 
	 * @param id 演員 ID
	 * @return true 如果成功刪除，false 如果找不到
	 */
	@Transactional
	public boolean deleteActor(Integer id) {
		if (actorRepo.existsById(id)) {
			actorRepo.deleteById(id);
			return true;
		}
		return false;
	}

	/**
	 * 根據 ID 查詢演員的詳細資訊，包含關聯的電影及角色列表
	 * 
	 * @param id 演員 ID
	 * @return 演員詳細資訊 DTO 或 Optional.empty()
	 */
	@Transactional(readOnly = true)
	public Optional<ActorDetailDTO> getActorDetail(Integer id) {
		return actorRepo.findActorWithDetailsByActorId(id).map(actor -> {
			ActorDetailDTO dto = new ActorDetailDTO();
			dto.setActorId(actor.getActorId());
			dto.setTmdbActorId(actor.getTmdbActorId());
			dto.setName(actor.getName());
			dto.setBiography(actor.getBiography());

			if (actor.getMovieActors() != null) {
				dto.setAssociatedMovies(actor.getMovieActors().stream().map(movieActor -> {
					Movie movie = movieActor.getMovie();
					MovieActorRoleDTO roleDto = new MovieActorRoleDTO();
					roleDto.setMovieId(movie.getId());
					roleDto.setMovieTitleLocal(movie.getTitleLocal());
					roleDto.setMovieTitleEnglish(movie.getTitleEnglish());
					roleDto.setMovieReleaseDate(movie.getReleaseDate());
					roleDto.setCharacterName(movieActor.getCharacterName());
					roleDto.setOrderNum(movieActor.getOrderNum());
					return roleDto;
				}).collect(Collectors.toList()));
			}
			return dto;
		});
	}

	// --- Genre CRUD ---

	/**
	 * 新增類型
	 * 
	 * @param genre 要新增的類型實體
	 * @return 新增後的類型實體
	 */
	@Transactional
	public Genre createGenre(Genre genre) {
		return genreRepo.save(genre);
	}

	/**
	 * 根據 ID 查詢類型
	 * 
	 * @param id 類型 ID
	 * @return 類型實體或 Optional.empty()
	 */
	public Optional<Genre> getGenreById(Integer id) {
		return genreRepo.findById(id);
	}

//    /**
//     * 查詢所有類型
//     * @return 所有類型的列表
//     */
//    public List<Genre> getAllGenres() {
//        return genreRepo.findAll();
//    }
    /**
     * 查詢所有類型
     * @return 所有類型的列表
     */
    @Transactional(readOnly = true)
    public List<GenreListDTO> getAllGenres() { // 修正點：回傳 GenreListDto 列表
        List<Genre> genres = genreRepo.findAll();
        // 修正點：顯式觸發 movieGenres 集合的載入
        for (Genre genre : genres) {
            if (genre.getMovieGenres() != null) {
                genre.getMovieGenres().size(); // 簡單訪問以觸發載入
                // 也可以遍歷關聯的 Movie 實體，確保它們被載入
                for (MovieGenre mg : genre.getMovieGenres()) {
                    mg.getMovie().getTitleLocal(); // 訪問關聯 Movie 的屬性以確保其載入
                }
            }
        }
        // 修正點：將 Genre 實體映射為 GenreListDto
        return genres.stream().map(genre -> {
            GenreListDTO dto = new GenreListDTO();
            dto.setGenreId(genre.getGenreId());
            dto.setTmdbGenreId(genre.getTmdbGenreId());
            dto.setName(genre.getName());

            if (genre.getMovieGenres() != null) {
                dto.setAssociatedMovies(genre.getMovieGenres().stream()
                        .map(movieGenre -> {
                            Movie movie = movieGenre.getMovie();
                            MovieSimpleDTO movieDto = new MovieSimpleDTO();
                            movieDto.setId(movie.getId());
                            movieDto.setTitleLocal(movie.getTitleLocal());
                            movieDto.setTitleEnglish(movie.getTitleEnglish());
                            movieDto.setReleaseDate(movie.getReleaseDate());
                            return movieDto;
                        })
                        .collect(Collectors.toList()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

	/**
	 * 更新類型資料 (只更新非 null 的欄位)
	 * 
	 * @param id           要更新的類型 ID
	 * @param updatedGenre 包含新資料的 Genre 物件
	 * @return 更新後的類型實體或 Optional.empty()
	 */
	@Transactional
	public Optional<Genre> updateGenre(Integer id, Genre updatedGenre) {
		Optional<Genre> genreOptional = genreRepo.findById(id);
		if (genreOptional.isPresent()) {
			Genre existingGenre = genreOptional.get();
			if (updatedGenre.getName() != null) {
				existingGenre.setName(updatedGenre.getName());
			}
			return Optional.of(genreRepo.save(existingGenre));
		}
		return Optional.empty();
	}

	/**
	 * 根據 ID 刪除類型
	 * 
	 * @param id 類型 ID
	 * @return true 如果成功刪除，false 如果找不到
	 */
	@Transactional
	public boolean deleteGenre(Integer id) {
		if (genreRepo.existsById(id)) {
			genreRepo.deleteById(id);
			return true;
		}
		return false;
	}

	/**
	 * 根據 ID 查詢類型的詳細資訊，包含關聯的電影列表
	 * 
	 * @param id 類型 ID
	 * @return 類型詳細資訊 DTO 或 Optional.empty()
	 */
	@Transactional(readOnly = true)
	public Optional<GenreDetailDTO> getGenreDetail(Integer id) {
		return genreRepo.findById(id).map(genre -> {
			GenreDetailDTO dto = new GenreDetailDTO();
			dto.setGenreId(genre.getGenreId());
			dto.setTmdbGenreId(genre.getTmdbGenreId());
			dto.setName(genre.getName());

			if (genre.getMovieGenres() != null) {
				dto.setAssociatedMovies(genre.getMovieGenres().stream().map(movieGenre -> {
					Movie movie = movieGenre.getMovie();
					MovieSimpleDTO movieDto = new MovieSimpleDTO();
					movieDto.setId(movie.getId());
					movieDto.setTitleLocal(movie.getTitleLocal());
					movieDto.setTitleEnglish(movie.getTitleEnglish());
					movieDto.setReleaseDate(movie.getReleaseDate());
					return movieDto;
				}).collect(Collectors.toList()));
			}
			return dto;
		});
	}

	/**
	 * 新增電影資料
	 * 
	 * @param movie 要新增的電影實體
	 * @return 新增後的電影實體
	 */
	@Transactional
	public Movie createMovie(Movie movie) {
		// 設定建立時間和更新時間
		movie.setCreateTime(LocalDateTime.now());
		movie.setUpdateTime(LocalDateTime.now());

		// 修正點：簡化 posterImage 處理邏輯，依賴 Jackson 自動解碼
		if (movie.getPosterImage() != null) {
			if (movie.getPosterImage() instanceof byte[]) { // 如果 Jackson 已經解碼成功，它就是 byte[]
				System.out.println("成功接收新增電影的 posterImage，長度: " + ((byte[]) movie.getPosterImage()).length);
			} else { // 如果不是 byte[] (例如，Jackson 解碼失敗或傳入 null)
				movie.setPosterImage(null);
				System.err.println("新增電影 posterImage 接收到非預期類型或為空，已設為 null。");
			}
		} else { // 如果 movie.getPosterImage() 本身就是 null
			System.out.println("新增電影的 posterImage 為 null。");
		}

		return movieRepo.save(movie);
	}
	// --- Movie Actor Role Management ---
    /**
     * 獲取特定電影的演員列表及其角色
     * @param movieId 電影 ID
     * @return 該電影的演員及角色列表
     */
    @Transactional(readOnly = true)
    public List<MovieActorRoleDTO> getMovieCast(Integer movieId) {
        Movie movie= movieRepo.findById(movieId)
        .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + movieId + " 的電影"));

        // 2. 【已更新】使用 Java Stream API 來處理電影的卡司列表，並填充您指定的 MovieActorRoleDTO
        return movie.getMovieActors().stream()
                .map(movieActor -> {
                    // 建立一個新的 DTO 物件
                    MovieActorRoleDTO dto = new MovieActorRoleDTO();
                    
                    // 從關聯的 Movie 物件中填充電影相關資訊
                    dto.setMovieId(movie.getId());
                    dto.setMovieTitleLocal(movie.getTitleLocal());
                    dto.setMovieTitleEnglish(movie.getTitleEnglish());
                    dto.setMovieReleaseDate(movie.getReleaseDate());
                    
                    // 從關聯的 Actor 物件中填充演員相關資訊
                    dto.setActorId(movieActor.getActor().getActorId());
                    dto.setActorName(movieActor.getActor().getName());
                    
                    // 從 MovieActor 實體本身填充角色資訊
                    dto.setCharacterName(movieActor.getCharacterName());
                    dto.setOrderNum(movieActor.getOrderNum());
                    
                    return dto;
                })
                .collect(Collectors.toList()); // 最後，將所有建立好的 DTO 物件收集成一個 List 並回傳
    }
	



	

    /**
     * 業務邏輯：更新特定電影中某演員的角色名稱
     * @param movieId 電影 ID
     * @param actorId 演員 ID
     * @param newCharacterName 新的角色名稱
     */
    @Transactional
    public void updateMovieActorRole(Integer movieId, Integer actorId, String newCharacterName) {
        
        // 【修正】在更新前，先用 findById 方法去資料庫裡尋找這筆關聯紀錄
        // findByMovieIdAndActorId 是您在 Repository 中定義好的方法
        MovieActor movieActorToUpdate = movieActorRepo.findByMovieIdAndActorId(movieId, actorId)
                // 如果找不到，就拋出一個 EntityNotFoundException 錯誤，
                // 這個錯誤會被上面的 Controller 接住，並回傳 404 給前端
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("在電影 ID %d 的卡司中找不到演員 ID %d 的關聯紀錄", movieId, actorId)
                ));

        // 如果成功找到了，才更新它的角色名稱
        movieActorToUpdate.setCharacterName(newCharacterName);

        // 因為這個方法是在一個 @Transactional (交易) 中，
        // 當方法成功結束時，JPA/Hibernate 會自動偵測到 movieActorToUpdate 物件的變更，
        // 並自動產生 UPDATE SQL 語句，將變更存入資料庫，所以我們不需要手動呼叫 save()。
    }

	/**
	 * 獲取特定電影的類型 ID 列表
	 * 
	 * @param movieId 電影 ID
	 * @return 該電影的類型 ID 列表
	 */
	@Transactional(readOnly = true)
	public List<Integer> getMovieGenreIds(Integer movieId) {
		Optional<Movie> movieOptional = movieRepo.findById(movieId);
		if (movieOptional.isPresent()) {
			Movie movie = movieOptional.get();
			if (movie.getMovieGenres() != null) {
				movie.getMovieGenres().size(); // 觸發載入
				return movie.getMovieGenres().stream().map(movieGenre -> movieGenre.getGenre().getGenreId())
						.collect(Collectors.toList());
			}
		}
		return List.of();
	}

	/**
	 * 更新特定電影的類型關聯
	 * 
	 * @param movieId  電影 ID
	 * @param genreIds 新的類型 ID 列表
	 * @return true 如果更新成功，false 如果找不到電影
	 */
	@Transactional
	public boolean updateMovieGenres(Integer movieId, List<Integer> genreIds) {
		Optional<Movie> movieOptional = movieRepo.findById(movieId);
		if (movieOptional.isPresent()) {
			Movie movie = movieOptional.get();

			// 清除舊的關聯
			movieGenreRepo.deleteByMovieId(movieId); // 呼叫新的 repository 方法刪除舊關聯
			movie.getMovieGenres().clear(); // 清空集合

			// 建立新的關聯
			if (genreIds != null && !genreIds.isEmpty()) {
				for (Integer genreId : genreIds) {
					Genre genre = genreRepo.findById(genreId)
							.orElseThrow(() -> new RuntimeException("更新電影類型時找不到類型 ID: " + genreId));
					MovieGenre movieGenre = new MovieGenre(movie, genre);
					movie.getMovieGenres().add(movieGenre); // 添加到實體集合中
				}
			}
			movieRepo.save(movie); // 保存 Movie 實體，級聯操作會保存新的 MovieGenre
			return true;
		}
		return false;
	}

	/**
	 * 透過日期找出當日正在上映的電影
	 * 
	 * @param date 上映日期
	 * @return 當日上映中電影基本資訊
	 */
	@Transactional(readOnly = true)
	public List<MovieListResponseDTO> findNowPlayingMoviesByDate(LocalDate date) {

		List<MovieListResponseDTO> nowPlayingMovies = new ArrayList<>();

		// 從資料庫取得候選上映中候選電影列表
		List<Movie> candidateMovies = movieRepo.findCandidateMoviesForNowPlaying(date);

		// 如果內容為空返回一個空字串
		if (candidateMovies == null || candidateMovies.isEmpty()) {
			return nowPlayingMovies;
		}

		// 遍歷電影候選列表
		for (Movie movie : candidateMovies) {
			// 取出上映日期及下檔日期
			LocalDate releaseDate = movie.getReleaseDate();
			LocalDate offShelfDate = movie.getOffShelfDate();

			// 先判斷 offShelfDate 有沒有值，沒值就讓他等於 releaseDate + 60天
			if (offShelfDate == null || offShelfDate.isBefore(releaseDate)) {
				offShelfDate = releaseDate.plusDays(150);
			}

			// 判斷 date 是否在上檔和下檔之間，是的話就做成 DTO 放到給前端的 list 中
			// 這裡只判斷下檔日期，因為抓取資料庫方法已經判定過上映日期
			if (!date.isAfter(offShelfDate)) {
				MovieListResponseDTO dto = new MovieListResponseDTO();
				dto.setId(movie.getId());
				dto.setTitleLocal(movie.getTitleLocal());
				dto.setDurationMinutes(movie.getDurationMinutes());
				dto.setOverview(movie.getOverview());
				dto.setPosterImage(movie.getPosterImage());
				dto.setTrailerUrl(movie.getTrailerUrl());
				
				nowPlayingMovies.add(dto);
			}
		}
		return nowPlayingMovies;
	}

	/**
     * 【已改寫】透過日期找出即將上映的電影
     * 這個方法現在更簡潔、更高效，且職責更清晰。
     * @param fromDate 查詢的起始日期 (例如，今天的日期)
     * @return 從指定日期開始，所有即將上映的電影列表
     */
    @Transactional(readOnly = true)
    public List<MovieListResponseDTO> getComingSoonMovies(LocalDate fromDate) {

        // 1. 直接呼叫 Repository 中定義好的查詢方法。
        //    所有複雜的篩選 (status=true, releaseDate >= fromDate) 和排序，
        //    都已經交給資料庫去高效地完成了。
        List<Movie> movies = movieRepo.findMoviesForComingSoon(fromDate);

        // 2. 使用 Java Stream API，將從資料庫拿到的 Movie 實體列表，
        //    轉換成前端需要的 MovieListResponseDTO 列表。
        //    這比傳統的 for 迴圈更簡潔、可讀性更高。
        return movies.stream()
                .map(movie -> {
                    // 對於列表中的每一個 movie 物件...
                    MovieListResponseDTO dto = new MovieListResponseDTO();
                    
                    // ...我們建立一個新的 DTO，並把需要的資料複製過去
                    dto.setId(movie.getId());
                    dto.setTitleLocal(movie.getTitleLocal());
                    dto.setDurationMinutes(movie.getDurationMinutes());
                    dto.setOverview(movie.getOverview());
                    dto.setPosterImage(movie.getPosterImage());
                    dto.setTrailerUrl(movie.getTrailerUrl());
                    // 如果您的 DTO 還有其他欄位，也可以在這裡設定
                    
                    return dto;
                })
                .collect(Collectors.toList()); // 最後，將所有 DTO 收集成一個 List 並回傳
    }
    
	public List<MoviesAllDataDTO> getAllLocalMovies() {
		List<MoviesAllDataDTO> dtos = new ArrayList<>();
		
		
		List<Movie> movies = movieRepo.findAll();
		for (Movie movie : movies) {
			Set<MovieGenre> movieGenres = movie.getMovieGenres();
			List<Genre> genres= new ArrayList<>(); 
			for (MovieGenre movieGenre : movieGenres) {
				genres.add(movieGenre.getGenre());
			}
			
			MoviesAllDataDTO dto = new MoviesAllDataDTO();
			dto.setMovie(movie);
			dto.setGenres(genres);
			
			dtos.add(dto);
		}
		return dtos;
	}
	/**
     * 商業邏輯：新增一個演員到指定電影的卡司列表中
     * @param movieId 電影的 ID
     * @param actorId 演員的 ID
     * @param characterName 飾演的角色名稱
     */
    @Transactional
    public void addActorToMovie(Integer movieId, Integer actorId, String characterName) {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + movieId + " 的電影"));
        Actor actor = actorRepo.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + actorId + " 的演員"));
        boolean exists = movieActorRepo.findByMovieIdAndActorId(movieId, actorId).isPresent();
        if (exists) {
            throw new IllegalStateException("演員 " + actor.getName() + " 已經在電影 " + movie.getTitleLocal() + " 的卡司中了");
        }
        
        MovieActor newMovieActor = new MovieActor(movie, actor, characterName, null);
        movieActorRepo.save(newMovieActor);
    }

    /**
     * 商業邏輯：從指定電影的卡司列表中移除一個演員
     * @param movieId 電影的 ID (【修正】類型從 Long 改為 Integer)
     * @param actorId 演員的 ID (類型為 Integer，保持不變)
     */
    @Transactional
    public void removeActorFromMovie(Integer movieId, Integer actorId) {
        // 1. 使用您在 Repository 中定義好的方法，直接根據 ID 找出要刪除的關聯實體
        MovieActor movieActorToRemove = movieActorRepo.findByMovieIdAndActorId(movieId, actorId)
                .orElseThrow(() -> new EntityNotFoundException("在電影 ID " + movieId + " 的卡司中找不到演員 ID " + actorId));

        // 2. 如果找到了，就從資料庫中刪除這筆紀錄
        movieActorRepo.delete(movieActorToRemove);
    }
    /**
     * 【已更新】商業邏輯：獲取單一電影的完整公開詳細資訊
     * @param movieId 電影 ID
     * @return 包含電影、類型、導演和演員的 DTO 物件
     */
    @Transactional(readOnly = true)
    public MoviePublicDetailDTO getPublicMovieDetailById(Integer movieId) {
        // 1. 根據傳入的 movieId，從資料庫中找出對應的 Movie 物件
        //    【重要】這裡假設您的 MovieRepository 有一個 findByIdWithDetails 的方法，
        //    它會使用 JOIN FETCH 一次性地載入所有關聯資料，以避免 N+1 查詢問題。
        Movie movie = movieRepo.findById(movieId) // 或者使用 findByIdWithDetails(movieId)
                .orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + movieId + " 的電影"));

        // 2. 從找到的 movie 物件中，獲取它關聯的所有電影類型 (Genre)
        List<Genre> genres = movie.getMovieGenres().stream()
                              .map(MovieGenre::getGenre) // 從 MovieGenre 關聯中取出 Genre 物件
                              .collect(Collectors.toList());

        // 3. 【已新增】從 movie 物件中，獲取它關聯的所有導演 (Director)
        List<Director> directors = movie.getMovieDirectors().stream()
                                    .map(MovieDirector::getDirector) // 從 MovieDirector 關聯中取出 Director 物件
                                    .collect(Collectors.toList());

        // 4. 【已新增】從 movie 物件中，獲取它關聯的所有演員 (Actor)
        List<Actor> actors = movie.getMovieActors().stream()
                                .map(MovieActor::getActor) // 從 MovieActor 關聯中取出 Actor 物件
                                .collect(Collectors.toList());

        // 5. 將所有撈取到的資料，一起打包成一個新的 DTO 物件並回傳
        return new MoviePublicDetailDTO(movie, genres, directors, actors);
    }
}
