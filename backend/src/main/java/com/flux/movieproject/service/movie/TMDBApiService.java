package com.flux.movieproject.service.movie;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flux.movieproject.model.dto.movie.TMDBGenreListDTO;
import com.flux.movieproject.model.dto.movie.TMDBMovieCreditsDTO;
import com.flux.movieproject.model.dto.movie.TMDBMovieDTO;
import com.flux.movieproject.model.dto.movie.TMDBMovieSearchResponseDTO;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TMDBApiService {
	@Value("${tmdb.api.base-url}") // 從 application.yml 讀取 API 基礎 URL
    private String baseUrl;

    @Value("${tmdb.api.key}") // 從 application.yml 讀取 API 金鑰
    private String apiKey;

    private final RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper;

    // 透過建構子注入 RestTemplate
    public TMDBApiService(RestTemplate restTemplate ,ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 根據電影 ID 從 TMDB API 取得電影詳細資訊
     * @param movieId TMDB 的電影 ID
     * @return 包含電影詳細資訊的 TMDBMovieDto 物件
     */
    public TMDBMovieDTO fetchMovieById(Integer movieId) {
        String url = String.format("%s/movie/%d?api_key=%s&language=zh-TW", baseUrl, movieId, apiKey);
        try {
            // 發送 GET 請求並將回傳的 JSON 轉換成 TMDBMovieDto 物件
            return restTemplate.getForObject(url, TMDBMovieDTO.class);
        } catch (Exception e) {
            System.err.println("無法從 TMDB API 取得電影資訊：" + e.getMessage());
            return null;
        }
    }

    public TMDBMovieSearchResponseDTO discoverMoviesByDateRangeAndRegion(LocalDate startDate, LocalDate endDate, String region, Integer page) {
    	
    	// 【新增】防呆檢查：如果 page 是 null 或小於 1，就強制把它設為 1
        if (page == null || page < 1) {
            System.out.println(">>> [WARN] Invalid page number detected (" + page + "). Defaulting to page 1.");
            page = 1;
        }
    	System.out.println(">>> [DEBUG] Preparing to send request to TMDB for page: " + page);
    	
        String url = String.format("%s/discover/movie?api_key=%s&language=zh-TW&release_date.gte=%s&release_date.lte=%s&region=%s&with_release_type=3&sort_by=release_date.desc&page=%d",
                baseUrl, apiKey, startDate.toString(), endDate.toString(), region, page);
        try {
            // 1. 改用 getForEntity 來接收回應，並指定我們想要收到「純文字」(String.class)
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            
            // 2. 從回應中取出 JSON 字串
            String jsonBody = responseEntity.getBody();
            
            // 3. 【核心】在後端 Console 印出完整的 JSON 字串！
            System.out.println("----------- TMDB API Raw JSON Response -----------");
            System.out.println(jsonBody);
            System.out.println("--------------------------------------------------");
            
            // 4. 使用我們注入的 objectMapper，手動將 JSON 字串轉換成 DTO 物件
            TMDBMovieSearchResponseDTO responseDto = objectMapper.readValue(jsonBody, TMDBMovieSearchResponseDTO.class);
            
            // 5. 回傳轉換後的 DTO 物件，讓後續程式能正常運作
            return responseDto;

        } catch (Exception e) {
            // 加上 .printStackTrace() 可以印出更詳細的錯誤堆疊，方便除錯
            System.err.println("無法從 TMDB API 查詢或解析電影列表：" + e.getMessage());
            e.printStackTrace(); 
            return null;
        }
    }
        
    /**
     * 根據電影 ID 從 TMDB API 取得電影演職員表 (演員和劇組)
     * @param movieId TMDB 的電影 ID
     * @return 包含演職員資訊的 TMDBMovieCreditsDto 物件
     */
     public TMDBMovieCreditsDTO fetchMovieCredits(Integer movieId) {
    	 String url = String.format("%s/movie/%d/credits?api_key=%s&language=zh-TW", baseUrl, movieId, apiKey);
         try {
             return restTemplate.getForObject(url, TMDBMovieCreditsDTO.class);
         } catch (Exception e) {
             System.err.println("無法從 TMDB API 取得電影演職員表：" + e.getMessage());
             return null;
         }
         }
        

     /**
      * 從 TMDB API 取得所有電影類型列表
      * @return 包含類型列表的 TMDBGenreListDto 物件
      */
      public TMDBGenreListDTO fetchMovieGenres() {
         String url = String.format("%s/genre/movie/list?api_key=%s&language=zh-TW", baseUrl, apiKey);
         try {
             return restTemplate.getForObject(url, TMDBGenreListDTO.class);
         } catch (Exception e) {
             System.err.println("無法從 TMDB API 取得電影類型列表：" + e.getMessage());
             return null;
         }
        
      }
}
//package tw.com.eeit.smalltheater.util;
//
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.math.BigDecimal;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.core5.http.io.HttpClientResponseHandler;
//import org.apache.hc.core5.http.io.entity.EntityUtils;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.houbb.opencc4j.util.ZhConverterUtil;
//
//import tw.com.eeit.smalltheater.model.pojo.TMDBActor;
//import tw.com.eeit.smalltheater.model.pojo.TMDBDirector;
//import tw.com.eeit.smalltheater.model.pojo.TMDBMovie;
//
//public class FetchTMDBData {
// // API_KEY
// private final static String API_KEY = "bae41584391dda412ae913d4454bba77";
//
// // 主要api路徑URL
// private final static String BASE_URL = "https://api.themoviedb.org/3";
//
// // 多條件探索電影URL
// private final static String DISCOVER_MOVIE_BASE_URL = BASE_URL + "/discover/movie?api_key=" + API_KEY;
//
// // 提供資訊為台灣語系關鍵字
// private final static String SHOW_ZhTW_KEYWORD = "language=zh-TW";
//
// // 提供資訊為台灣語系關鍵字
// private final static String REGION_TW_KEYWORD = "region=TW";
//
// // 圖片檔位置基本URL
// private final static String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
//
// /**
//  * 【主程式】取得特定區間TMDB資料
//  * 
//  * @param startDate 起日
//  * @param endDate   迄日
//  * @return 電影資料集
//  */
// public static List<TMDBMovie> fetchMovies(String startDate, String endDate) {
//  // 形成要請求的api URL
//  String searchUrl = DISCOVER_MOVIE_BASE_URL + "&" + SHOW_ZhTW_KEYWORD + "&" + REGION_TW_KEYWORD
//    + "&primary_release_date.gte=" + startDate + "&primary_release_date.lte=" + endDate
//    + "&sort_by=primary_release_date.asc" + "&with_release_type=3";
////  String searchUrl = DISCOVER_MOVIE_BASE_URL  + "&primary_release_date.gte=" + startDate + "&primary_release_date.lte=" + endDate
////    + "&sort_by=primary_release_date.asc";
//  
//  System.out.println("DEBUG: TMDB Discover API URL: " + searchUrl); 
//
//  // 先做第一次查詢取得總頁數 totalPages
//  int totalPages = getResourceFromApiUrl(searchUrl).get("total_pages").asInt(0);
//
//  // 如果沒資料直接提示無資料
//  if (totalPages == 0) {
//   System.out.println("查無資料");
//   return null;
//  }
//
//  System.out.println(startDate + " -> " + endDate + " 總共有 " + totalPages + " 頁資料");
//
//  if (totalPages > 500) {
//   System.out.println("API限制最多只能抓500頁，如需要完整資料請減少查詢區間");
//   totalPages = 500;
//  }
//
//  List<TMDBMovie> movieList = new ArrayList<TMDBMovie>();
//
//  // 新增電影的清單資料至list中
//  fetchMoiveDtl(searchUrl, movieList, totalPages);
//
//  for (TMDBMovie movie : movieList) {
//   // 取得電影的分級及台灣上映日期
//   getCertAndReleaseDate(movie);
//   // 取得導演與演員
//   getDirectorsAndActors(movie);
//   // 取得預告片
//   getTrailerUrl(movie);
//   // 取得片長
//   getRuntime(movie);
//  }
//
//  return movieList;
// }
//
// /**
//  * 取得片長資訊
//  * 
//  * @param movie 要搜尋片長的電影
//  */
// public static void getRuntime(TMDBMovie movie) {
//  String url = BASE_URL + "/movie/" + movie.getTmdbMovieId() + "?api_key=" + API_KEY + "&language=zh-TW";
//
//  JsonNode root = getResourceFromApiUrl(url);
//  if (root.has("runtime") && !root.get("runtime").isNull()) {
//   int runtime = root.get("runtime").asInt();
//   movie.setDurationMinutes(runtime);
//   System.out.println("⏱️ [" + movie.getTitleLocal() + "] 片長：" + runtime + " 分鐘");
//  }
// }
//
// /**
//  * 取得預告片的url
//  * 
//  * @param movie
//  */
// private static void getTrailerUrl(TMDBMovie movie) {
//  String url = BASE_URL + "/movie/" + movie.getTmdbMovieId() + "/videos?api_key=" + API_KEY + "&language=zh-TW";
//
//  JsonNode root = getResourceFromApiUrl(url);
//  JsonNode results = root.get("results");
//
//  if (results != null && results.isArray()) {
//   for (JsonNode video : results) {
//    String site = video.get("site").asText("");
//    String type = video.get("type").asText("");
//    if ("YouTube".equals(site) && "Trailer".equalsIgnoreCase(type)) {
//     String key = video.get("key").asText();
//     String trailerUrl = "https://www.youtube.com/watch?v=" + key;
//     movie.setTrailerUrl(trailerUrl);
//     System.out.println("▶️ [" + movie.getTitleLocal() + "] 預告片：" + trailerUrl);
//     break;
//    }
//   }
//  }
// }
//
// /**
//  * 取得導演及演員資訊
//  * 
//  * @param movie
//  */
// private static void getDirectorsAndActors(TMDBMovie movie) {
//  String creditsUrl = BASE_URL + "/movie/" + movie.getTmdbMovieId() + "/credits?api_key=" + API_KEY + "&language=zh-TW";
//
//  JsonNode root = getResourceFromApiUrl(creditsUrl);
//
//  // 取得導演
//  List<TMDBDirector> directors = new ArrayList<>();
//  JsonNode crew = root.get("crew");
//  if (crew != null && crew.isArray()) {
//   for (JsonNode person : crew) {
//    if ("Director".equals(person.get("job").asText())) {
//     int id = person.get("id").asInt();
//     String name = person.get("name").asText();
//     TMDBDirector director = new TMDBDirector();
//     director.setId(id);
//     director.setName(name);
//     directors.add(director);
//    }
//   }
//  }
//
//  // 取得演員
//  List<TMDBActor> actors = new ArrayList<>();
//  JsonNode cast = root.get("cast");
//  if (cast != null && cast.isArray()) {
//   for (int i = 0; i < cast.size(); i++) {
//    JsonNode actorNode = cast.get(i);
//    int id = actorNode.get("id").asInt();
//    String name = actorNode.get("name").asText();
//    String character = actorNode.has("character") ? actorNode.get("character").asText() : null;
//    Integer order = actorNode.has("order") ? actorNode.get("order").asInt() : i;
//
//    TMDBActor actor = new TMDBActor();
//    actor.setId(id);
//    actor.setName(name);
//    actor.setCharacter(character);
//    actor.setOrder(order);
//
//    actors.add(actor);
//   }
//  }
//
//  // 設定到 movie 實例
//  movie.setDirectors(directors);
//  movie.setActors(actors);
//
//  // ✅ Debug 印出
//  System.out.println(
//    "🎬 [" + movie.getTitleLocal() + "] 導演：" + directors.stream().map(TMDBDirector::getName).toList()
//      + " / 演員：" + actors.stream().map(TMDBActor::getName).toList());
// }
//
// /**
//  * 取得級別(普遍級等)及台灣上映時間(與清單段全球首映不同)
//  * 
//  * @param movie
//  */
// private static void getCertAndReleaseDate(TMDBMovie movie) {
//  // TODO Auto-generated method stub
//  String searchCertAndReleaseDateUrl = BASE_URL + "/movie/" + movie.getTmdbMovieId() + "/release_dates?api_key="
//    + API_KEY;
//
//  JsonNode root = getResourceFromApiUrl(searchCertAndReleaseDateUrl);
//  JsonNode results = root.get("results");
//
//  if (results != null && results.isArray()) {
//   for (JsonNode country : results) {
//    String region = country.get("iso_3166_1").asText();
//    if ("TW".equals(region)) {
//     System.out.println("Region: " + region);
//     JsonNode releaseDates = country.get("release_dates");
//
//     if (releaseDates != null && releaseDates.isArray()) {
//      for (JsonNode entry : releaseDates) {
//       int type = entry.get("type").asInt();
//       if (type == 3 || type == 1) { // 戲院 or 首映
//        String cert = entry.get("certification").asText("");
//        String releaseDateStr = entry.get("release_date").asText();
//
//        if (releaseDateStr != null && releaseDateStr.length() >= 10) {
//         LocalDate releaseDate = LocalDate.parse(releaseDateStr.substring(0, 10));
//         movie.setCertification(cert);
//         movie.setReleaseDate(releaseDate);
//
//         break;
//        }
//       }
//      }
//     }
//     break; // 找到 TW 區域後就不需再跑其他國家
//    }
//   }
//  }
// }
//
// /**
//  * 新增電影清單明細至呼叫方法的list中
//  * 
//  * @param searchUrl  擷取電影資源url
//  * @param movieList  欲新增之list
//  * @param totalPages 總共要抓幾頁
//  */
// private static void fetchMoiveDtl(String searchUrl, List<TMDBMovie> movieList, int totalPages) {
//  for (int i = 1; i <= totalPages; i++) {
//   System.out.println("執行第" + i + "頁資料");
//   JsonNode root = getResourceFromApiUrl(searchUrl + "&page=" + i);
//
//   JsonNode results = root.get("results");
//   for (JsonNode movie : results) {
//    int movieId = movie.get("id").asInt();
//    String title = movie.get("title").asText();
//    String originalTitle = movie.get("original_title").asText();
//    String releaseDateStr = movie.get("release_date").asText(null);
//    LocalDate releaseDate = null;
//    if (!StringUtils.isBlank(releaseDateStr)) {
//     releaseDate = LocalDate.parse(releaseDateStr);
//    }
//
//    // 簡介
//    String overview = movie.get("overview").asText();
//
//    // 類型：genre_ids
//    List<Integer> genreIds = new ArrayList<Integer>();
//
//    JsonNode genreIdsNode = movie.get("genre_ids");
//    if (genreIdsNode != null && genreIdsNode.isArray()) {
//     for (JsonNode idNode : genreIdsNode) {
//      int genreId = idNode.asInt();
//      genreIds.add(genreId);
//     }
//    }
//
//    // 海報 poster_path
//    String posterPath = null;
//    byte[] posterImg = null;
//    JsonNode posterPathNode = movie.get("poster_path");
//
//    if (posterPathNode != null && !posterPathNode.isNull() && posterPathNode.asText().trim().length() > 0) {
//     String relativePath = posterPathNode.asText().trim();
//     posterPath = IMAGE_BASE_URL + relativePath;
//     try {
//      posterImg = downloadImageAsBytes(posterPath);
//     } catch (Exception e) {
//      e.printStackTrace();
//     }
//    }
//
//    // 原始語言
//    String originalLanguage = movie.get("original_language").asText();
//
//    // 熱門指數
//    double popularity = movie.get("popularity").asDouble();
//
//    // 平均評分
//    double voteAverage = movie.get("vote_average").asDouble();
//
//    // 評分人數
//    int voteCount = movie.get("vote_count").asInt();
//
//    // 檢查是否為柯南電影 (方便偵錯)
//    if (movieId == 1396965) {
//     System.out.println("--- 偵測到柯南電影 (ID: 1396965) ---");
//     System.out.println("Title: " + title);
//     System.out.println("Original Title: " + originalTitle);
//     System.out.println("Release Date String: " + releaseDateStr);
//     System.out.println(
//       "Overview: " + overview.length() + " chars, blank: " + StringUtils.isBlank(overview));
//     System.out.println("Poster Path: " + posterPath);
//    }
//
//    // 只保留有上映時間、有簡介、有海報網址、中文名稱
//    if ((title.matches(".*[\\u4E00-\\u9FFF]+.*") || originalTitle.matches(".*[\\u4E00-\\u9FFF]+.*"))
