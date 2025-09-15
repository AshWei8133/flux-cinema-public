package com.flux.movieproject.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flux.movieproject.model.pojo.TMDBActorPojo;
import com.flux.movieproject.model.pojo.TMDBDirectorPojo;
import com.flux.movieproject.model.pojo.TMDBMoviePojo;
import com.github.houbb.opencc4j.util.ZhConverterUtil;

@Component
public class FetchTMDBDataUtil {

	private static String API_KEY;

	@Value("${tmdb.api.key}")
	public void setApiKey(String apiKey) {
		FetchTMDBDataUtil.API_KEY = apiKey;
	}

	private final static String BASE_URL = "https://api.themoviedb.org/3";
	private final static String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

	public List<TMDBMoviePojo> fetchMovies(LocalDate startDate, LocalDate endDate) {
		String searchUrl = String.format(
				"%s/discover/movie?api_key=%s&language=zh-TW&region=TW&primary_release_date.gte=%s&primary_release_date.lte=%s&sort_by=primary_release_date.asc&with_release_type=3",
				BASE_URL, API_KEY, startDate.toString(), endDate.toString());

		JsonNode root = getResourceFromApiUrl(searchUrl);
		if (root == null || root.get("total_pages") == null)
			return new ArrayList<>();

		int totalPages = root.get("total_pages").asInt(0);
		if (totalPages == 0)
			return new ArrayList<>();
		if (totalPages > 500)
			totalPages = 500;
		System.out.printf("(Util) 找到 %d 頁資料，開始快速掃描電影基本列表...\n", totalPages);

		// --- 第一階段：快速掃描 ---
		List<TMDBMoviePojo> movieList = new ArrayList<>();
		for (int i = 1; i <= totalPages; i++) {
			System.out.printf("\r(Util) 正在快速掃描第 %d / %d 頁...", i, totalPages);
			JsonNode pageRoot = getResourceFromApiUrl(searchUrl + "&page=" + i);
			if (pageRoot != null) {
				parseMoviesFromPage_Fast(pageRoot, movieList);
			}
		}
		System.out.println("\n(Util) 電影基本列表掃描完成！共篩選出 " + movieList.size() + " 部符合基本條件的電影。");
		System.out.println("(Util) 現在開始為這 " + movieList.size() + " 部電影補全詳細資訊並下載海報...");

		// --- 第二階段：補全詳情與下載 ---
		int movieCount = 0;
		for (TMDBMoviePojo movie : movieList) {
			movieCount++;
			System.out.printf("(Util) [%d/%d] 正在處理電影 ID: %d (%s)\n", movieCount, movieList.size(),
					movie.getTmdbMovieId(), movie.getTitleLocal());

			getMovieDetailsAndDownloadPoster(movie);

			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("執行緒被中斷");
			}
		}

		System.out.println("\n(Util) 所有電影詳細資訊處理完畢！");
		return movieList;
	}

	private static void parseMoviesFromPage_Fast(JsonNode root, List<TMDBMoviePojo> movieList) {
		JsonNode results = root.get("results");
		if (results == null || !results.isArray())
			return;

		for (JsonNode movieNode : results) {
			String title = ZhConverterUtil.toTraditional(movieNode.get("title").asText(""));
			String originalTitle = ZhConverterUtil.toTraditional(movieNode.get("original_title").asText(""));
			String overview = movieNode.get("overview").asText("");
			String posterPath = movieNode.has("poster_path") ? movieNode.get("poster_path").asText(null) : null;

			boolean hasChineseTitle = title.matches(".*[\\u4e00-\\u9fa5]+.*")
					|| originalTitle.matches(".*[\\u4e00-\\u9fa5]+.*");
			boolean hasOverview = !StringUtils.isBlank(overview);
			boolean hasPosterPath = !StringUtils.isBlank(posterPath);

			if (hasChineseTitle && hasOverview && hasPosterPath) {
				TMDBMoviePojo m = new TMDBMoviePojo();
				m.setTmdbMovieId(movieNode.get("id").asInt());
				m.setTitleLocal(title);
				m.setTitleEnglish(originalTitle);
				m.setOverview(ZhConverterUtil.toTraditional(overview));
				m.setPosterPath(posterPath);

				m.setOriginalLanguage(movieNode.get("original_language").asText());
				m.setPopularity(BigDecimal.valueOf(movieNode.get("popularity").asDouble()));
				m.setVoteAverage(BigDecimal.valueOf(movieNode.get("vote_average").asDouble()));
				m.setVoteCount(movieNode.get("vote_count").asInt());

				String releaseDateStr = movieNode.get("release_date").asText(null);
				if (!StringUtils.isBlank(releaseDateStr)) {
					m.setReleaseDate(LocalDate.parse(releaseDateStr));
				}

				List<Integer> genreIds = new ArrayList<>();
				JsonNode genreIdsNode = movieNode.get("genre_ids");
				if (genreIdsNode != null && genreIdsNode.isArray()) {
					for (JsonNode idNode : genreIdsNode)
						genreIds.add(idNode.asInt());
				}
				m.setGenreIds(genreIds);

				movieList.add(m);
			}
		}
	}

	private static void getMovieDetailsAndDownloadPoster(TMDBMoviePojo movie) {
		// 1. 下載海報
		if (movie.getPosterPath() != null) {
			try {
				movie.setPosterImage(downloadImageAsBytes(IMAGE_BASE_URL + movie.getPosterPath()));
			} catch (Exception e) {
				System.err.println(" -> 下載海報失敗 ID: " + movie.getTmdbMovieId());
			}
		}

		// 2. 獲取片長
		String movieUrl = String.format("%s/movie/%d?api_key=%s&language=zh-TW", BASE_URL, movie.getTmdbMovieId(),
				API_KEY);
		JsonNode movieRoot = getResourceFromApiUrl(movieUrl);
		if (movieRoot != null && movieRoot.has("runtime") && !movieRoot.get("runtime").isNull()) {
			movie.setDurationMinutes(movieRoot.get("runtime").asInt());
		}

		// 3. 獲取台灣專屬上映日期與分級 (這會覆寫掉第一階段獲取的全球日期)
		String releaseDateUrl = String.format("%s/movie/%d/release_dates?api_key=%s", BASE_URL, movie.getTmdbMovieId(),
				API_KEY);
		JsonNode releaseRoot = getResourceFromApiUrl(releaseDateUrl);
		if (releaseRoot != null && releaseRoot.has("results")) {
			for (JsonNode country : releaseRoot.get("results")) {
				if ("TW".equals(country.get("iso_3166_1").asText())) {
					JsonNode releaseDates = country.get("release_dates");
					if (releaseDates != null && releaseDates.isArray()) {
						for (JsonNode entry : releaseDates) {
							if (entry.get("type").asInt() == 3 || entry.get("type").asInt() == 1) { // 戲院上映或首映
								String dateStr = entry.get("release_date").asText();
								if (dateStr != null && dateStr.length() >= 10) {
									movie.setReleaseDate(LocalDate.parse(dateStr.substring(0, 10)));
									movie.setCertification(entry.get("certification").asText(""));
									break;
								}
							}
						}
					}
					break;
				}
			}
		}

		// 4. 獲取預告片
		String videoUrl = String.format("%s/movie/%d/videos?api_key=%s&language=zh-TW", BASE_URL,
				movie.getTmdbMovieId(), API_KEY);
		JsonNode videoRoot = getResourceFromApiUrl(videoUrl);
		if (videoRoot != null && videoRoot.has("results")) {
			for (JsonNode video : videoRoot.get("results")) {
				if ("YouTube".equals(video.get("site").asText(""))
						&& "Trailer".equalsIgnoreCase(video.get("type").asText(""))) {
					movie.setTrailerUrl("https://www.youtube.com/watch?v=" + video.get("key").asText());
					break;
				}
			}
		}

		// 5. 獲取演員與導演
		String creditsUrl = String.format("%s/movie/%d/credits?api_key=%s&language=zh-TW", BASE_URL,
				movie.getTmdbMovieId(), API_KEY);
		JsonNode creditsRoot = getResourceFromApiUrl(creditsUrl);
		if (creditsRoot == null)
			return;

		List<TMDBDirectorPojo> directors = new ArrayList<>();
		if (creditsRoot.has("crew")) {
			for (JsonNode person : creditsRoot.get("crew")) {
				if ("Director".equals(person.get("job").asText())) {
					TMDBDirectorPojo dir = new TMDBDirectorPojo();
					dir.setId(person.get("id").asInt());
					dir.setName(person.get("name").asText());
					directors.add(dir);
				}
			}
		}
		movie.setDirectors(directors);

		List<TMDBActorPojo> actors = new ArrayList<>();
		if (creditsRoot.has("cast")) {
			for (JsonNode actorNode : creditsRoot.get("cast")) {
				TMDBActorPojo act = new TMDBActorPojo();
				act.setId(actorNode.get("id").asInt());
				act.setName(actorNode.get("name").asText());
				act.setCharacter(actorNode.get("character").asText(""));
				act.setOrder(actorNode.get("order").asInt());
				actors.add(act);
			}
		}
		movie.setActors(actors);
	}

	private static byte[] downloadImageAsBytes(String imageUrl) throws IOException {
		URL url = new URL(imageUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(15000);
		try (InputStream in = conn.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			return out.toByteArray();
		}
	}

	private static JsonNode getResourceFromApiUrl(String searchUrl) {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(searchUrl);
			HttpClientResponseHandler<String> responseHandler = new HttpClientResponseHandler<>() {
				@Override
				public String handleResponse(ClassicHttpResponse response) throws IOException, HttpException {
					int status = response.getCode();
					if (status >= 200 && status < 300) {
						return EntityUtils.toString(response.getEntity(), "UTF-8");
					} else {
						throw new IOException("Unexpected response status: " + status);
					}
				}
			};
			String responseBody = client.execute(request, responseHandler);
			return new ObjectMapper().readTree(responseBody);
		} catch (Exception e) {
			System.err.println("從 TMDB API 獲取資源時失敗: " + searchUrl);
			System.err.println(" -> 錯誤訊息: " + e.getMessage());
			return null;
		}
	}

	public static Map<Integer, String> fetchGenresMap() {
		String genresUrl = BASE_URL + "/genre/movie/list?api_key=" + API_KEY + "&language=zh-TW";
		JsonNode root = getResourceFromApiUrl(genresUrl);
		Map<Integer, String> genresMap = new HashMap<>();
		if (root != null && root.has("genres")) {
			JsonNode genres = root.get("genres");
			if (genres.isArray()) {
				for (JsonNode genre : genres) {
					genresMap.put(genre.get("id").asInt(), ZhConverterUtil.toTraditional(genre.get("name").asText()));
				}
			}
		}
		return genresMap;
	}
}