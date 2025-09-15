package com.flux.movieproject.service.event;

import com.flux.movieproject.model.dto.event.EventEligibilityBatchCreateRequest;
import com.flux.movieproject.model.dto.event.EventEligibilityCreateRequest;
import com.flux.movieproject.model.dto.event.EventEligibilityResponse;
import com.flux.movieproject.model.entity.event.Event;
import com.flux.movieproject.model.entity.event.EventEligibility;
import com.flux.movieproject.model.entity.member.MemberLevel;
import com.flux.movieproject.model.entity.movie.Movie;
import com.flux.movieproject.model.entity.theater.MovieSession;
import com.flux.movieproject.repository.event.EventEligibilityRepository;
import com.flux.movieproject.repository.event.EventRepository;
import com.flux.movieproject.repository.member.MemberLevelRepository;
import com.flux.movieproject.repository.movie.MovieRepository;
import com.flux.movieproject.repository.moviesession.MovieSessionRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class EventEligibilityService {

    @Autowired
    private EventEligibilityRepository eligibilityRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieSessionRepository sessionRepository;

    @Autowired
    private MemberLevelRepository memberLevelRepository;

    /** 建立單筆條件（if/else 版本） */
    public EventEligibilityResponse create(EventEligibilityCreateRequest req) {
        // 取 Event
        Optional<Event> optEvent = eventRepository.findById(req.getEventId());
        if (!optEvent.isPresent()) {
            throw new EntityNotFoundException("Event not found: " + req.getEventId());
        }
        Event event = optEvent.get();

        EventEligibility e = new EventEligibility();
        e.setEvent(event);

        // 取 Movie（可選）
        if (req.getMovieId() != null) {
            Optional<Movie> optMovie = movieRepository.findById(req.getMovieId());
            if (!optMovie.isPresent()) {
                throw new EntityNotFoundException("Movie not found: " + req.getMovieId());
            }
            e.setMovie(optMovie.get());
        }

        // 取 Session（可選）
        if (req.getSessionId() != null) {
            Optional<MovieSession> optSession = sessionRepository.findById(req.getSessionId());
            if (!optSession.isPresent()) {
                throw new EntityNotFoundException("Session not found: " + req.getSessionId());
            }
            e.setSession(optSession.get());
        }

        // 取 MemberLevel（可選）
        if (req.getMemberLevelId() != null) {
            Optional<MemberLevel> optLevel = memberLevelRepository.findById(req.getMemberLevelId());
            if (!optLevel.isPresent()) {
                throw new EntityNotFoundException("MemberLevel not found: " + req.getMemberLevelId());
            }
            e.setMemberLevel(optLevel.get());
        }

        EventEligibility saved = eligibilityRepository.save(e);
        return toResponse(saved);
    }

    /** 依活動查詢（for 迴圈版本） */
    @Transactional(readOnly = true)
    public List<EventEligibilityResponse> findByEventId(Integer eventId) {
        List<EventEligibility> all = eligibilityRepository.findAll();
        List<EventEligibilityResponse> result = new ArrayList<EventEligibilityResponse>();

        for (int i = 0; i < all.size(); i++) {
            EventEligibility item = all.get(i);
            if (item.getEvent() != null && Objects.equals(item.getEvent().getEventId(), eventId)) {
                result.add(toResponse(item));
            }
        }
        return result;
    }

    /** 全部查詢（for 迴圈版本） */
    @Transactional(readOnly = true)
    public List<EventEligibilityResponse> findAll() {
        List<EventEligibility> all = eligibilityRepository.findAll();
        List<EventEligibilityResponse> result = new ArrayList<EventEligibilityResponse>();

        for (int i = 0; i < all.size(); i++) {
            result.add(toResponse(all.get(i)));
        }
        return result;
    }

    /** 刪除（if 檢查版本） */
    public void delete(Integer id) {
        boolean exists = eligibilityRepository.existsById(id);
        if (!exists) {
            throw new EntityNotFoundException("Eligibility not found: " + id);
        }
        eligibilityRepository.deleteById(id);
    }

    /** 批次建立（for 迴圈版本） */
    public List<EventEligibilityResponse> batchCreate(EventEligibilityBatchCreateRequest req) {
        List<EventEligibilityResponse> out = new ArrayList<EventEligibilityResponse>();

        // 同一活動，將三種集合各自展開成多筆
        if (req.getMovieIds() != null) {
            for (int i = 0; i < req.getMovieIds().size(); i++) {
                Integer movieId = req.getMovieIds().get(i);
                out.add(create(one(req.getEventId(), movieId, null, null)));
            }
        }

        if (req.getSessionIds() != null) {
            for (int i = 0; i < req.getSessionIds().size(); i++) {
                Integer sessionId = req.getSessionIds().get(i);
                out.add(create(one(req.getEventId(), null, sessionId, null)));
            }
        }

        if (req.getMemberLevelIds() != null) {
            for (int i = 0; i < req.getMemberLevelIds().size(); i++) {
                Integer levelId = req.getMemberLevelIds().get(i);
                out.add(create(one(req.getEventId(), null, null, levelId)));
            }
        }

        return out;
    }

    // ======= Helpers =======

    /** 建立一個請求物件（for 批次展開使用） */
    private EventEligibilityCreateRequest one(Integer eventId, Integer movieId, Integer sessionId, Integer levelId) {
        EventEligibilityCreateRequest r = new EventEligibilityCreateRequest();
        r.setEventId(eventId);
        r.setMovieId(movieId);
        r.setSessionId(sessionId);
        r.setMemberLevelId(levelId);
        return r;
    }

    /** entity -> 回傳 DTO（if/else 純轉換） */
    private EventEligibilityResponse toResponse(EventEligibility e) {
        Integer eventId = null;
        String eventTitle = null;
        if (e.getEvent() != null) {
            eventId = e.getEvent().getEventId();
            eventTitle = e.getEvent().getTitle();
        }

        Integer movieId = null;
        String movieTitle = null;
        if (e.getMovie() != null) {
            // 注意：這裡使用你專案的命名（movieId），避免 getId() 不存在
            movieId = e.getMovie().getId();
            movieTitle = e.getMovie().getTitleLocal();
        }

        Integer sessionId = null;
        String sessionLabel = null;
        if (e.getSession() != null) {
            sessionId = e.getSession().getSessionId();
            sessionLabel = buildSessionLabel(e.getSession());
        }

        Integer memberLevelId = null;
        String memberLevelName = null;
        if (e.getMemberLevel() != null) {
            memberLevelId = e.getMemberLevel().getMemberLevelId();
            memberLevelName = e.getMemberLevel().getLevelName();
        }

        return EventEligibilityResponse.builder()
                .id(e.getEventEligibilityId())
                .eventId(eventId)
                .eventTitle(eventTitle)
                .movieId(movieId)
                .movieTitle(movieTitle)
                .sessionId(sessionId)
                .sessionLabel(sessionLabel)
                .memberLevelId(memberLevelId)
                .memberLevelName(memberLevelName)
                .build();
    }

    /** 客製化場次標籤 */
    private String buildSessionLabel(MovieSession s) {
        String hall = "";
        if (s.getTheater() != null && s.getTheater().getTheaterName() != null) {
            hall = s.getTheater().getTheaterName();
        }

        String time = "";
        if (s.getStartTime() != null) {
            time = s.getStartTime().toString();
        }

        String label = (hall + " " + time).trim();
        if (label.length() == 0) {
            label = "未提供場次資訊";
        }
        return label;
    }
}
