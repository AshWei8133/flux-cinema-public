package com.flux.movieproject.service.moviesession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flux.movieproject.enums.MovieSessionStatus;
import com.flux.movieproject.enums.SeatStatus;
import com.flux.movieproject.model.dto.moviesession.MovieSessionDailyOverviewDTO;
import com.flux.movieproject.model.dto.moviesession.MovieSessionDateResponseDTO;
import com.flux.movieproject.model.dto.moviesession.MovieSessionOverviewResponseDTO;
import com.flux.movieproject.model.dto.moviesession.MovieSessionUpdateDTO;
import com.flux.movieproject.model.dto.moviesession.movieshowtimes.ShowtimeDTO;
import com.flux.movieproject.model.dto.moviesession.quickbookingform.MovieSessionResponseDTO;
import com.flux.movieproject.model.dto.moviesession.quickbookingform.TheaterDTO;
import com.flux.movieproject.model.dto.moviesession.quickbookingform.TheaterTypeDTO;
import com.flux.movieproject.model.dto.moviesession.viewseats.SeatDTO;
import com.flux.movieproject.model.dto.moviesession.viewseats.SessionDetailsDTO;
import com.flux.movieproject.model.dto.moviesession.viewseats.SessionSeatLayoutDTO;
import com.flux.movieproject.model.dto.moviesession.viewseats.SessionSeatStatusDTO;
import com.flux.movieproject.model.entity.movie.Movie;
import com.flux.movieproject.model.entity.theater.MovieSession;
import com.flux.movieproject.model.entity.theater.SessionSeat;
import com.flux.movieproject.model.entity.theater.Theater;
import com.flux.movieproject.repository.movie.MovieRepository;
import com.flux.movieproject.repository.moviesession.MovieSessionRepository;
import com.flux.movieproject.repository.theater.TheaterRepository;
import com.flux.movieproject.repository.ticket.SessionSeatRepository;
import com.flux.movieproject.utils.DateProcessUtil;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class MovieSessionService {
	@Autowired
	private MovieSessionRepository movieSessionRepo;
	@Autowired
	private MovieRepository movieRepo;
	@Autowired
	private TheaterRepository theaterRepo;
	@Autowired
	private SessionSeatRepository sessionSeatRepo;

	private Map<LocalDate, List<MovieSession>> getGroupedSessionsByMonth(Integer year, Integer month) {
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);
		// 例如，查詢 8 月份資料，需要查到 9 月 1 號的凌晨 2 點
		LocalDateTime startDateTime = firstDayOfMonth.atTime(6, 0);
		LocalDateTime endDateTime = lastDayOfMonth.plusDays(1).atTime(2, 0);

		List<MovieSession> allSessionsInMonth = movieSessionRepo.findByStartTimeBetween(startDateTime, endDateTime);

		Map<LocalDate, List<MovieSession>> sessionsByDate = new HashMap<>();

		if (allSessionsInMonth == null || allSessionsInMonth.isEmpty()) {
			return sessionsByDate;
		}

		for (MovieSession session : allSessionsInMonth) {
			LocalDateTime startTime = session.getStartTime();
			LocalDate businessDate; // 用來存放此場次應歸屬的「營業日」

			// 核心判斷邏輯：
			// 那麼這個場次就屬於前一天的營業日。
			if (startTime.getHour() < 6) {
				businessDate = startTime.toLocalDate().minusDays(1);
			} else {
				// 否則 (6 點及以後)，它就屬於當天的營業日。
				businessDate = startTime.toLocalDate();
			}

			// 將場次放入對應營業日的 List 中
			// 檢查 Map 裡是否已經有這個營業日的 key
			if (!sessionsByDate.containsKey(businessDate)) {
				// 如果沒有，就為這個營業日建立一個新的空 List
				sessionsByDate.put(businessDate, new ArrayList<>());
			}
			// 將當前場次加入到對應營業日的 List
			sessionsByDate.get(businessDate).add(session);
		}
		return sessionsByDate;
	}

	/**
	 * 取得該年月所有日期的排片總覽列表（包含狀態與詳情）。
	 *
	 * @param year  年份
	 * @param month 月份
	 * @return 包含整個月份每一天排片總覽的列表。
	 */
	public List<MovieSessionDailyOverviewDTO> getMonthlyMovieSessionOverview(Integer year, Integer month) {

		List<MovieSessionDailyOverviewDTO> monthlyOverviewList = new ArrayList<>();

		LocalDate today = LocalDate.now();
		List<LocalDate> monthDates = DateProcessUtil.getMonthDates(year, month);

		Map<LocalDate, List<MovieSession>> sessionsByDate = getGroupedSessionsByMonth(year, month);
		List<Movie> allMovies = movieRepo.findAll();

		for (LocalDate date : monthDates) {
			MovieSessionStatus status = MovieSessionStatus.NONE;
			// 將 Map 的鍵從 String 改為 Integer
			Map<Integer, List<MovieSessionOverviewResponseDTO>> dailySessionsByTheater = new HashMap<>();
			List<MovieSession> sessionsForDay = sessionsByDate.get(date);

			// 判斷狀態 (此處邏輯不變)
			if (date.isBefore(today)) {
				if (sessionsForDay != null && !sessionsForDay.isEmpty()) {
					status = MovieSessionStatus.HISTORY;
				}
			} else if (date.isEqual(today)) {
				status = MovieSessionStatus.TODAY;
			} else {
				Set<Integer> activeMovieIds = new HashSet<>();
				for (Movie movie : allMovies) {
					if (!date.isBefore(movie.getReleaseDate()) && movie.getStatus()) {
						LocalDate offShelfDate = (movie.getOffShelfDate() != null) ? movie.getOffShelfDate()
								: movie.getReleaseDate().plusDays(120);
						if (!date.isAfter(offShelfDate)) {
							activeMovieIds.add(movie.getId());
						}
					}
				}

				Set<Integer> scheduledMovieIds = new HashSet<>();
				if (sessionsForDay != null) {
					for (MovieSession session : sessionsForDay) {
						scheduledMovieIds.add(session.getMovie().getId());
					}
				}

				boolean isPending = false;
				for (Integer activeId : activeMovieIds) {
					if (!scheduledMovieIds.contains(activeId)) {
						isPending = true;
						break;
					}
				}

				if (isPending) {
					status = MovieSessionStatus.PENDING;
				} else if (!activeMovieIds.isEmpty()) {
					status = MovieSessionStatus.COMPLETED;
				}
			}

			// 處理詳細排片資訊
			if (sessionsForDay != null) {
				for (MovieSession session : sessionsForDay) {
					// 取得影廳 ID 作為 Map 的鍵
					Integer theaterId = session.getTheater().getTheaterId();
					if (!dailySessionsByTheater.containsKey(theaterId)) {
						dailySessionsByTheater.put(theaterId, new ArrayList<>());
					}

					MovieSessionOverviewResponseDTO sessionDto = new MovieSessionOverviewResponseDTO();
					sessionDto.setSessionId(session.getSessionId());
					sessionDto.setTitleLocal(session.getMovie().getTitleLocal());
					sessionDto.setStartTime(session.getStartTime());
					sessionDto.setEndTime(session.getEndTime());

					dailySessionsByTheater.get(theaterId).add(sessionDto);
				}
			}

			// 創建並加入每天的總覽 DTO
			MovieSessionDailyOverviewDTO dailyOverviewDTO = new MovieSessionDailyOverviewDTO();
			dailyOverviewDTO.setDate(date);
			dailyOverviewDTO.setStatus(status);
			// 傳入新的 Map
			dailyOverviewDTO.setDailySessionsByTheaterId(dailySessionsByTheater);
			monthlyOverviewList.add(dailyOverviewDTO);
		}

		return monthlyOverviewList;
	}

	/**
	 * 取得特定日期的所有場次
	 * 
	 * @param date
	 * @return
	 */
	public List<MovieSessionDateResponseDTO> findSessionsByDate(LocalDate date) {

		List<MovieSessionDateResponseDTO> response = new ArrayList<>();

		// 營業日的開始時間為 "當天早上 6 點"
		// 例如，傳入 2025-08-21，startDateTime 就是 2025-08-21 06:00:00
		LocalDateTime startDateTime = date.atTime(6, 0);
		// 營業日的結束時間為 "隔天凌晨 2 點"
		// 例如，傳入 2025-08-21，endDateTime 就是 2025-08-22 02:00:00
		LocalDateTime endDateTime = date.plusDays(1).atTime(2, 0);

		// 從資料庫獲取該日期區間內的所有場次
		List<MovieSession> movieSessions = movieSessionRepo.findByStartTimeBetween(startDateTime, endDateTime);

		// 將 List<MovieSession> 轉換為 List<MovieSessionResponseDTO>
		for (MovieSession movieSession : movieSessions) {
			MovieSessionDateResponseDTO dto = new MovieSessionDateResponseDTO();
			dto.setSessionId(movieSession.getSessionId());
			dto.setTheaterId(movieSession.getTheater().getTheaterId());
			dto.setMovieId(movieSession.getMovie().getId());
			dto.setStartTime(movieSession.getStartTime());
			dto.setEndTime(movieSession.getEndTime());

			response.add(dto);
		}

		return response;

	}

	/**
	 * 批次更新特定日期的場次資料
	 * 
	 * @param date        要更新的日期
	 * @param sessionDTOs 要更新的資料
	 */
	@Transactional
	public void batchUpdateSessions(LocalDate date, List<MovieSessionUpdateDTO> sessionDTOs) {
		System.out.println("收到的 DTOs: " + sessionDTOs); // 保持這個日誌，方便觀察
		LocalDateTime startDateTime = date.atTime(6, 0);
		LocalDateTime endDateTime = date.plusDays(1).atTime(2, 0);

		// 步驟 1: 獲取現有場次
		Map<String, MovieSession> existingSessionMap = movieSessionRepo
				.findByStartTimeBetween(startDateTime, endDateTime).stream()
				.collect(Collectors.toMap(
						session -> session.getTheater().getTheaterId() + ":" + session.getStartTime().toString(),
						session -> session));

		// 步驟 2: 區分新增與刪除
		List<MovieSessionUpdateDTO> dtosToAdd = new ArrayList<>();
		for (MovieSessionUpdateDTO dto : sessionDTOs) {
			// 【關鍵】只處理那些沒有 sessionId 的 DTO，代表它們是新拖曳進來的
			// 這樣可以防止意外地將已存在的場次（在 existingSessionMap 中被移除的）再次加入新增列表
			if (dto.getSessionId() == null || dto.getSessionId() <= 0
					|| !existingSessionMap.containsKey(generateKey(dto))) {
				dtosToAdd.add(dto);
			} else {
				existingSessionMap.remove(generateKey(dto));
			}
		}
		List<MovieSession> sessionsToDelete = new ArrayList<>(existingSessionMap.values());

		// 步驟 3: 批次化處理刪除
		if (!sessionsToDelete.isEmpty()) {
			boolean anyOccupied = sessionSeatRepo.existsByMovieSessionInAndStatusNotIn(sessionsToDelete,
					List.of(SeatStatus.AVAILABLE));
			if (anyOccupied) {
				throw new IllegalStateException("無法刪除場次，因為部分待刪除的場次中已有座位被預訂或售出。");
			}
			sessionSeatRepo.deleteByMovieSessionIn(sessionsToDelete);
			movieSessionRepo.deleteAllInBatch(sessionsToDelete);
		}

		// 步驟 4: 批次化處理新增
		if (!dtosToAdd.isEmpty()) {
			// 預先載入 Movies 和 Theaters
			Set<Integer> movieIds = dtosToAdd.stream().map(MovieSessionUpdateDTO::getMovieId)
					.collect(Collectors.toSet());
			Set<Integer> theaterIds = dtosToAdd.stream().map(MovieSessionUpdateDTO::getTheaterId)
					.collect(Collectors.toSet());
			Map<Integer, Movie> movieMap = movieRepo.findAllById(movieIds).stream()
					.collect(Collectors.toMap(Movie::getId, movie -> movie));
			Map<Integer, Theater> theaterMap = theaterRepo.findAllById(theaterIds).stream()
					.collect(Collectors.toMap(Theater::getTheaterId, theater -> theater));

			// 【核心修正】在迴圈中為新的 MovieSession 物件設定所有必要的值
			List<MovieSession> sessionsToAdd = new ArrayList<>();
			for (MovieSessionUpdateDTO dto : dtosToAdd) {
				Movie movie = movieMap.get(dto.getMovieId());
				Theater theater = theaterMap.get(dto.getTheaterId());

				if (movie == null || theater == null) {
					throw new EntityNotFoundException("找不到對應的電影或影廳，ID: " + dto.getMovieId() + "/" + dto.getTheaterId());
				}

				MovieSession newSession = new MovieSession();
				// --- 將 DTO 的值設定到 Entity 物件中 ---
				newSession.setMovie(movie);

				newSession.setTheater(theater);
				newSession.setStartTime(dto.getStartTime());
				newSession.setEndTime(dto.getEndTime()); // 確保這行存在且 dto.getEndTime() 有值
				// ------------------------------------
				sessionsToAdd.add(newSession);
			}

			// (A) 一次性儲存所有新場次
			List<MovieSession> savedNewSessions = movieSessionRepo.saveAll(sessionsToAdd);

			// (B) 使用單一原生 SQL 指令建立所有 SessionSeat
			if (!savedNewSessions.isEmpty()) {
				List<Integer> newSessionIds = savedNewSessions.stream().map(MovieSession::getSessionId)
						.collect(Collectors.toList());
				sessionSeatRepo.bulkCreateSessionSeatsForNewSessions(newSessionIds);
			}
		}
	}
	
	// 輔助方法以避免重複程式碼
	private String generateKey(MovieSessionUpdateDTO dto) {
	    return dto.getTheaterId() + ":" + dto.getStartTime().toString();
	}

	/**
	 * 根據電影ID，查詢未來7天的所有場次資訊。 * @param movieId 電影ID
	 * 
	 * @return 包含場次資訊的DTO列表
	 */
	public List<MovieSessionResponseDTO> findShowtimesByMovieForNext7Days(Integer movieId) {
		// 1. 定義查詢的時間範圍：從「此時此刻」開始，確保不會撈取到已經過去的場次，從今天算起的第 7 個「營業日」的結束時間 (隔天凌晨 2:00)
		LocalDateTime startDateTime = LocalDateTime.now();
		LocalDateTime endDateTime = startDateTime.plusDays(7);

		// 2. 呼叫 Repository 進行資料庫查詢
		List<MovieSession> sessions = movieSessionRepo.findShowtimesForMovie(movieId, startDateTime, endDateTime);

		// 3. 將 List<MovieSession> (Entity) 轉換為 List<MovieSessionResponseDTO> (DTO)
		List<MovieSessionResponseDTO> responseDTOs = sessions.stream().map(session -> {
			// 建立最外層 DTO
			MovieSessionResponseDTO sessionDTO = new MovieSessionResponseDTO();
			sessionDTO.setSessionId(session.getSessionId());
			sessionDTO.setStartTime(session.getStartTime());

			// 建立巢狀的 TheaterDTO
			TheaterDTO theaterDTO = new TheaterDTO();

			// 建立最內層的 TheaterTypeDTO
			TheaterTypeDTO theaterTypeDTO = new TheaterTypeDTO();
			theaterTypeDTO.setTheaterTypeId(session.getTheater().getTheaterType().getTheaterTypeId());
			theaterTypeDTO.setTheaterTypeName(session.getTheater().getTheaterType().getTheaterTypeName());

			// 組裝
			theaterDTO.setTheaterType(theaterTypeDTO);
			sessionDTO.setTheater(theaterDTO);

			return sessionDTO;
		}).toList(); // 使用 .toList() 轉換為 List

		return responseDTOs;
	}

	/**
	 * 根據場次id獲取完整的座位狀態圖資訊
	 * 
	 * @param sessionId 場次識別碼
	 * @return 包含場次詳細資訊和所有座位狀態的 DTO
	 */
	public SessionSeatLayoutDTO getSessionSeatLayout(Integer sessionId) {
		// 第一步：執行高效的 To-One 查詢
		MovieSession movieSession = movieSessionRepo.findSessionWithToOneRelationsById(sessionId)
				.orElseThrow(() -> new EntityNotFoundException("找不到 ID 為 " + sessionId + " 的電影場次"));

		// 第二步：執行專門的 To-Many 查詢
		// 由於 movieSession 已經在 Hibernate 的 Persistence Context (持久化上下文) 中，
		// 這裡查出來的 sessionSeats 會被 Hibernate 自動地 "注入" 回 movieSession 物件的 sessionSeats
		// 集合中。
		// 即使我們沒有手動 movieSession.setSessionSeats(...)，後續操作也能正確取值。
		List<SessionSeat> sessionSeats = movieSessionRepo.findSessionSeatsWithDetailsBySession(movieSession);

		// 將 entity 組合成 DTO
		SessionDetailsDTO sessionInfo = new SessionDetailsDTO(movieSession.getSessionId(),
				movieSession.getMovie().getTitleLocal(), movieSession.getTheater().getTheaterType().getTheaterTypeId(),
				movieSession.getTheater().getTheaterType().getTheaterTypeName(),
				movieSession.getTheater().getTheaterName(), movieSession.getStartTime());

		List<SessionSeatStatusDTO> seats = sessionSeats.stream().map(this::convertSessionSeatToDto)
				.collect(Collectors.toList());

		return new SessionSeatLayoutDTO(sessionInfo, seats);
	}
	
	/**
     * 專為 MovieShowtimes.vue 設計的主服務方法。
     */
    public List<ShowtimeDTO> findShowtimesWithSeatStatus(Integer movieId) {
        
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = startDateTime.plusDays(7);

        // 只查詢 MovieSession 和 ToOne 關聯
        List<MovieSession> sessions = movieSessionRepo.findShowtimesWithToOneRelations(movieId, startDateTime, endDateTime);

        // 如果沒有任何場次，直接回傳空列表，避免後續無效查詢
        if (sessions.isEmpty()) {
            return new ArrayList<>();
        }

        // 根據第一查的結果，去抓取所有相關的 SessionSeat 集合
        // Hibernate 會自動將查出來的 SessionSeat "塞回" sessions 列表裡對應的物件中。
        movieSessionRepo.findSessionSeatsForSessions(sessions);

        // --- 處理數據 ---
        // 此時的 sessions 列表中的每個 MovieSession 物件都已經填滿了 sessionSeats 集合
        return sessions.stream()
                .map(this::convertToDTOWithSeatCount)
                .collect(Collectors.toList());
    }

    /**
     * 輔助方法：(此方法無需修改)
     * 將單一 MovieSession Entity 轉換為 ShowtimeDTO，並執行「座位計算」。
     */
private ShowtimeDTO convertToDTOWithSeatCount(MovieSession session) {
        
        // 【修改點 1】重新計算「可售總座位數」
        long sellableTotalSeats = session.getSessionSeats().stream()
                .filter(sessionSeat -> 
                    sessionSeat.getSeat() != null && 
                    !"友善座位".equals(sessionSeat.getSeat().getSeatType()) 
                )
                .count();

        // 【修改點 2】重新計算「實際可售座位數」
        long availableSeats = session.getSessionSeats().stream()
                .filter(sessionSeat -> 
                    sessionSeat.getSeat() != null &&
                    SeatStatus.AVAILABLE.equals(sessionSeat.getStatus()) &&      // 條件一：狀態為可售
                    !"友善座位".equals(sessionSeat.getSeat().getSeatType())       // 條件二：類型不為 "友善座位"
                )
                .count();

        // 組裝成前端需要的 DTO 物件
        return new ShowtimeDTO(
                session.getSessionId(),
                session.getStartTime(),
                session.getTheater().getTheaterType().getTheaterTypeId(),
                session.getTheater().getTheaterType().getTheaterTypeName(),
                (int) sellableTotalSeats,
                availableSeats
        );
    }
	
	
	

	/**
	 * 輔助方法：將單一的 SessionSeat Entity 轉換為 SessionSeatStatusDTO。
	 * 
	 * @param sessionSeat 來源 SessionSeat Entity
	 * @return 轉換後的 SessionSeatStatusDTO
	 */
	private SessionSeatStatusDTO convertSessionSeatToDto(SessionSeat sessionSeat) {
		// 檢查 seat 是否為 null (例如，如果資料庫中有無效的 sessionSeat 記錄)
		if (sessionSeat.getSeat() == null) {
			return null; // 或者拋出異常，視業務邏輯而定
		}

		SeatDTO seatDto = new SeatDTO(sessionSeat.getSeat().getSeatId(), sessionSeat.getSeat().getSeatType(),
				sessionSeat.getSeat().getRowNumber(), sessionSeat.getSeat().getColumnNumber());

		return new SessionSeatStatusDTO(sessionSeat.getSessionSeatId(), sessionSeat.getStatus().name(), seatDto);
	}

}