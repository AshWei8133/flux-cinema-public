import httpClient from "@/services/api";

const PublicMovieSessionService = {
    /**
     * 根據電影 ID 獲取其未來 7 天的所有場次
     * @param {number} movieId - 電影的 ID
     * @returns {Promise<Array>} 包含場次資訊的陣列
     */
    getShowtimesByMovieId(movieId) {
        return httpClient.get(`/movieSession/movie/${movieId}`);
    },

    /**
     * 根據場次 Session ID 獲取該場次的詳細資訊和完整的座位佈局
     * @param {number} sessionId - 場次的 ID
     * @returns {Promise<object>} 包含 sessionInfo 和 seats 的物件
     */
    getSessionSeatLayout(sessionId) {
        return httpClient.get(`/movieSession/${sessionId}/seats`);
    },

    /**
    * 【此方法為 MovieShowtimes.vue 組件專用】
    * 根據電影 ID 獲取其未來 7 天的場次，包含計算好的座位狀態
    * @param {number} movieId - 電影的 ID
    * @returns {Promise<Array>} 包含場次與座位狀態資訊的陣列 (ShowtimeDTO)
    */
    getShowtimesWithSeatStatus(movieId) {
        // 注意！這裡我們呼叫的是你 Controller 中新的 @GetMapping("/{movieId}/showtimes")
        // 根據你的 Controller 程式碼，它的路徑也在 /api/movieSession 下
        return httpClient.get(`/movieSession/${movieId}/showtimes`);
    }
}



// 暴露service對象
export default PublicMovieSessionService;