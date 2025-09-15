import httpClient from "@/services/api"

class MovieApiService {
    /**
     * 【新增】透過共用 API，獲取所有本地電影的「完整」資料
     * @returns {Promise<Array>} 包含所有本地電影詳細資料的陣列
     */
    async getExistingLocalMovies() {
        try {
            // 呼叫您現有的、用來獲取所有本地電影的 API
            // 請確保這個 URL '/admin/movie/movies/all' 是正確的
            const response = await httpClient.get('/admin/movie/movies/all');
            return response; // 攔截器會自動回傳 data
        } catch (error) {
            console.error('共用 API 獲取本地電影列表失敗:', error);
            throw error;
        }
    }
    /**
     * [API 1] 獲取 tmdb_movie 暫存資料庫中的所有電影
     * 對應後端: GET /api/admin/tmdb/tmdb-movies
     * @returns {Promise<Array>} 電影資料陣列
     */
    getAllTmdbMovies() {
        // 【已修正】API 路徑已更新為您提供的 /admin/tmdb/tmdb-movies
        return httpClient.get('/admin/tmdb/new-tmdb-movies')
    }

    /**
     * [API 2] 觸發後端從 TMDB API 依日期區間匯入新電影
     * 對應後端: POST /api/admin/tmdb-util/import-by-date
     * @param {string} startDate 起始日期 'YYYY-MM-DD'
     * @param {string} endDate 結束日期 'YYYY-MM-DD'
     * @returns {Promise<string>} 後端回傳的成功訊息
     */
    importFromTmdbApiByDate(startDate, endDate) {
        return httpClient.post('/admin/tmdb-util/import-by-date', null, {
            params: {
                startDate,
                endDate,
            },
        })
    }

    /**
     * [API 3] 將使用者勾選的電影，從 tmdb_movie 表匯入到 movie 表
     * 對應後端: POST /api/admin/tmdb/import-selected-to-local
     * @param {Array<number>} tmdbMovieIds 要匯入的電影 ID 陣列
     * @returns {Promise<string>} 後端回傳的成功訊息
     */
    // 【已修正】方法名稱已從 importSelectedToLocal 改為 importSelectedTmdbMovies
    importSelectedTmdbMovies(tmdbMovieIds) {
        // 依據您的 MovieService.js，直接發送陣列即可
        return httpClient.post('/admin/tmdb/import-selected-to-local', tmdbMovieIds)
    }
}

export default new MovieApiService()