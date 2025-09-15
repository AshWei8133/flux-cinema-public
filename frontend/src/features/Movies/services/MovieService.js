// src/services/MovieService.js (您可以將其放在 src/services 或 src/api 等資料夾下)
//const MOVIE_BASE_URL = '/movie/movies'; // 後端電影 API 的基本路徑
import httpClient from '@/services/api'

class MovieService {
  /**
   * 獲取所有電影資料
   * @returns {Promise<Array>} 電影資料陣列
   */
  // async getAllMovies() {
  //   const response = await httpClient.get('/movie/movies');
  //   return response;
  // }
  async getAllMovies() {
    const response = await httpClient.get('/admin/movie/movies/all')
    return response
  }

  /**
   * 根據電影 ID 刪除電影
   * @param {number} movieId 電影 ID
   * @returns {Promise<any>} 後端回傳的刪除結果
   */
  async deleteMovie(movieId) {
    // 後端 DELETE /movie/movies/{id} 端點
    const response = await httpClient.delete(`/admin/movie/movies/delete/${movieId}`)
    return response.data
  }

  /**
   * 根據電影 ID 更新電影資料
   * @param {number} movieId 電影 ID
   * @param {object} updatedData 要更新的電影資料物件
   * @returns {Promise<any>} 後端回傳的更新結果
   */
  async updateMovie(movieId, updatedData) {
    // 後端 PUT /movie/movies/{id} 端點
    const response = await httpClient.put(`/admin/movie/movies/update/${movieId}`, updatedData)
    return response.data
  }
  /**
   * 新增電影資料
   * @param {object} movieData 要新增的電影資料物件
   * @returns {Promise<object>} 後端回傳的新增電影實體
   */
  async createMovie(movieData) {
    const response = await httpClient.post('/admin/movie/movies/create', movieData)
    return response.data
  }
  /**
   * 獲取所有 TMDBMovie 資料
   * @returns {Promise<Array>} TMDBMovie 資料陣列
   */
  async getAllTmdbMovies() {
    // 後端 GET /api/tmdb/tmdb-movies 端點
    const response = await httpClient.get('/admin/tmdb/tmdb-movies')
    return response
  }

  /**
   * 匯入選定的 TMDBMovie 電影到本地 Movie 表
   * @param {Array<number>} tmdbMovieIds 要匯入的 TMDB 電影 ID 列表
   * @returns {Promise<any>} 後端回傳的匯入結果
   */
  async importSelectedTmdbMovies(tmdbMovieIds) {
    // 修正點：明確地將陣列作為請求主體發送，並確保 Content-Type 為 application/json
    // Axios 預設會將陣列序列化為 JSON，但明確傳遞空對象作為 config 可以確保其行為
    const response = await httpClient.post('/admin/tmdb/import-selected-to-local', tmdbMovieIds, {
      headers: {
        'Content-Type': 'application/json',
      },
    })
    return response.data
  }
  /**
   * 觸發後端從 TMDB API 匯入電影資料，並回傳後端給的完整結果
   * @param {string} startDate
   * @param {string} endDate
   * @param {string} region
   * @param {number} page
   * @returns {Promise<any>} 後端回傳的 DTO 物件
   */
  async triggerTmdbDiscoverImport(startDate, endDate, region, page) {
    // httpClient.post(...) 的回傳值，已經被你的攔截器處理過，
    // 所以 response 變數直接就是後端回傳的 JSON 資料物件
    const response = await httpClient.post('/admin/tmdb/import-by-date', null, {
      params: {
        startDate,
        endDate,
        region,
        page,
      },
    })

    // 【最終修正】
    // 因為 response 變數本身就是我們想要的資料，所以直接回傳它，
    // 不需要再取用 .data 屬性。
    return response
  }

  /**
   * 根據 ID 獲取導演詳細資訊 (包含關聯電影)
   * @param {number} directorId 導演 ID
   * @returns {Promise<object>} 導演詳細資訊 DTO
   */
  async getDirectorDetail(directorId) {
    const response = await httpClient.get(`/admin/movie/directors/${directorId}/detail`)
    return response.data
  }
  /**
   * 新增導演
   * @param {object} directorData 要新增的導演資料物件
   * @returns {Promise<object>} 後端回傳的新增導演實體
   */
  async createDirector(directorData) {
    const response = await httpClient.post('/admin/movie/directors', directorData)
    return response.data
  }
  /**
   * 獲取所有導演資料
   * @returns {Promise<Array>} 導演資料陣列
   */
  async getAllDirectors() {
    const response = await httpClient.get('/admin/movie/directors')
    return response
  }
  /**
   * 根據 ID 獲取導演資料
   * @param {number} directorId 導演 ID
   * @returns {Promise<object>} 導演資料物件
   */
  async getDirectorById(directorId) {
    const response = await httpClient.get(`/admin/movie/directors/${directorId}`)
    return response.data
  }
  /**
   * 根據 ID 更新導演資料
   * @param {number} directorId 導演 ID
   * @param {object} updatedData 要更新的導演資料物件
   * @returns {Promise<object>} 後端回傳的更新導演實體
   */
  async updateDirector(directorId, updatedData) {
    const response = await httpClient.put(`/admin/movie/directors/${directorId}`, updatedData)
    return response.data
  }

  /**
   * 根據 ID 獲取演員詳細資訊 (包含關聯電影及角色)
   * @param {number} actorId 演員 ID
   * @returns {Promise<object>} 演員詳細資訊 DTO
   */
  async getActorDetail(actorId) {
    const response = await httpClient.get(`/admin/movie/actors/${actorId}/detail`)
    return response.data
  }
  /**
   * 新增演員
   * @param {object} actorData 演員資料
   * @returns {Promise<object>} 新增後的演員實體
   */
  async createActor(actorData) {
    const response = await httpClient.post('/admin/movie/actors', actorData)
    return response.data
  }
  /**
   * 獲取所有演員列表
   * @returns {Promise<Array>} 演員列表
   */
  async getAllActors() {
    // 後端 GET /api/movie/actors 端點
    const response = await httpClient.get('/admin/movie/actors')
    return response
  }
  /**
   * 更新演員
   * @param {number} actorId 演員 ID
   * @param {object} updatedData 更新資料
   * @returns {Promise<object>} 更新後的演員實體
   */
  async updateActor(actorId, updatedData) {
    const response = await httpClient.put(`/admin/movie/actors/${actorId}`, updatedData)
    return response.data
  }

  /**
   * 根據 ID 獲取類型詳細資訊 (包含關聯電影)
   * @param {number} genreId 類型 ID
   * @returns {Promise<object>} 類型詳細資訊 DTO
   */
  async getGenreDetail(genreId) {
    const response = await httpClient.get(`/admin/movie/genres/${genreId}/detail`)
    return response.data
  }
  /**
   * 新增類型
   * @param {object} genreData 類型資料
   * @returns {Promise<object>} 新增後的類型實體
   */
  async createGenre(genreData) {
    const response = await httpClient.post('/admin/movie/genres', genreData)
    return response.data
  }
  /**
   * 獲取所有類型列表
   * @returns {Promise<Array>} 類型列表
   */
  async getAllGenres() {
    // 後端 GET /api/movie/genres 端點
    const response = await httpClient.get('/admin/movie/genres')
    return response
  }
  /**
   * 更新類型
   * @param {number} genreId 類型 ID
   * @param {object} updatedData 更新資料
   * @returns {Promise<object>} 更新後的類型實體
   */
  async updateGenre(genreId, updatedData) {
    const response = await httpClient.put(`/admin/movie/genres/${genreId}`, updatedData)
    return response.data
  }

  /**
   * 根據 ID 刪除導演
   * @param {number} directorId 導演 ID
   * @returns {Promise<any>} 後端回傳的刪除結果
   */
  async deleteDirector(directorId) {
    const response = await httpClient.delete(`/admin/movie/directors/${directorId}`)
    return response.data
  }
  /**
   * 刪除演員
   * @param {number} actorId 演員 ID
   * @returns {Promise<string>} 刪除結果訊息
   */
  async deleteActor(actorId) {
    const response = await httpClient.delete(`/admin/movie/actors/${actorId}`)
    return response.data
  }
  /**
   * 刪除類型
   * @param {number} genreId 類型 ID
   * @returns {Promise<string>} 刪除結果訊息
   */
  async deleteGenre(genreId) {
    const response = await httpClient.delete(`/admin/movie/genres/${genreId}`)
    return response.data
  }

  // --- Movie Actor Role Management API 呼叫 ---

  /**
   * 獲取特定電影的演員列表及其角色
   * @param {number} movieId 電影 ID
   * @returns {Promise<Array>} 該電影的演員及角色列表
   */
  async getMovieCast(movieId) {
    const response = await httpClient.get(`/admin/movie/movies/${movieId}/cast`)
    return response
  }

  /**
   * 更新特定電影中某演員的角色名稱
   * @param {number} movieId 電影 ID
   * @param {number} actorId 演員 ID
   * @param {string} characterName 新的角色名稱
   * @returns {Promise<any>} 後端回傳的更新結果
   */
  async updateMovieActorRole(movieId, actorId, characterName) {
    
    // 【修正1】建立一個符合後端 DTO 結構的 JavaScript 物件
    // 這個物件會被 axios 自動轉換成 JSON 字串：{ "characterName": "新的角色名" }
    const requestBody = {
        characterName: characterName
    };

    // 【修正2】將 requestBody 物件作為 httpClient.put 的第二個參數（也就是 Request Body）發送出去
    const response = await httpClient.put(`/admin/movie/movies/${movieId}/actors/${actorId}`, requestBody);
    
    // 您的攔截器會處理 .data，所以直接回傳 response 即可
    return response;
  }

  /**
   * 獲取特定電影的類型 ID 列表
   * @param {number} movieId 電影 ID
   * @returns {Promise<Array<number>>} 該電影的類型 ID 列表
   */
  async getMovieGenreIds(movieId) {
    const response = await httpClient.get(`/admin/movie/movies/${movieId}/genres`)
    return response.data
  }
  /**
   * 更新特定電影的類型關聯
   * @param {number} movieId 電影 ID
   * @param {Array<number>} genreIds 新的類型 ID 列表
   * @returns {Promise<any>} 後端回傳的更新結果
   */
  async updateMovieGenres(movieId, genreIds) {
    const response = await httpClient.put(`/admin/movie/movies/${movieId}/genres`, genreIds, {
      headers: {
        'Content-Type': 'application/json',
      },
    })
    return response.data
  }
  // 新增一個演員到電影卡司
  async addActorToMovie(movieId, actorId, characterName) {
    // 假設後端 API 端點是 POST /api/movies/{movieId}/actors
    // 並且 request body 需要 { actorId, characterName }
    const response = await httpClient.post(`/admin/movie/movies/${movieId}/actors`, {
      actorId: actorId,
      characterName: characterName,
    })
    return response
  }

  // 從電影卡司中移除一個演員
  async removeActorFromMovie(movieId, actorId) {
    // 假設後端 API 端點是 DELETE /api/movies/{movieId}/actors/{actorId}
    const response = await httpClient.delete(`/admin/movie/movies/${movieId}/actors/${actorId}`)
    return response.data
  }

  // 您可以在這裡添加其他電影相關的 API 呼叫，例如：
  // async getMovieById(movieId) { ... }
  // async createMovie(movieData) { ... }
  // async searchMovies(params) { ... }

  async getNowPlayingMoviesByDate(date) {
    try {
      const response = await httpClient.get(`/admin/movie/now-playing/${date}`)
      // console.log(response);
      return response
    } catch {
      console.error('獲取', date, '現正熱映電影列表失敗:', error.message)
      throw error
    }
  }

  /**
 * 根據電影 ID 獲取海報的原始二進位資料 (Blob)
 */
  async getMoviePoster(movieId) {
    const url = `/admin/movie/movies/${movieId}/poster`;
    // 由於 baseURL 已在 httpClient 中設定，這裡只需提供相對路徑
    // 攔截器同樣會為這個請求加上 token
    return await httpClient.get(url, { responseType: 'blob' });
  }

}

export default new MovieService() // 導出 MovieService 的單例實例
