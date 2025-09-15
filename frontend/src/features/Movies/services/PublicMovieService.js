import httpClient from "@/services/api";

const PublicMovieService = {

    /**
     * (前台用) 根據 ID 獲取單一電影的完整詳細資訊
     * 這個方法會呼叫您 Canvas 中的 @GetMapping("/movie/{id}") API
     * @param {number} id 電影的 ID
     * @returns {Promise<any>} 包含電影完整資訊的 DTO 物件
     */
    async getMovieById(id) {
        try {
            // 1. 使用 httpClient 發送一個 GET 請求到後端 API
            //    我們用反引號 `` 來建立一個模板字串，並將傳入的 id 變數嵌入到 URL 中
            const response = await httpClient.get(`/movie/${id}`);
            
            // 2. 如果請求成功，回傳從後端拿到的資料
            //    (假設您的攔截器會自動處理並回傳 response.data)
            return response; 

        } catch (error) {
            // 3. 如果請求過程中發生任何錯誤 (例如 404 Not Found 或 500 伺服器錯誤)
            //    就在瀏覽器的 Console 中印出詳細的錯誤訊息
            console.error(`獲取電影(ID: ${id})詳細資料失敗:`, error.message);
            
            // 4. 並將錯誤再往上拋，讓呼叫它的 Pinia Store 也能捕捉到這個錯誤
            throw error;
        }
    },
    /**
     * (前台用) 取得所有現正熱映的電影
     * 假設前台的 API 是 /movies/now-playing
     */
    async getNowPlayingMovies(date) {
        try {
            // 呼叫公開的 API，這個 API 不需要 admin token
            const response = await httpClient.get(`/movie/now-playing/${date}`);
            return response; // 攔截器會自動回傳 data
        } catch (error) {
            console.error('獲取前台熱映電影失敗:', error.message);
            throw error;
        }
    },

    async fetchComingSoonMovies(date) {
        try {
            const response = await httpClient.get(`/movie/coming-soon/${date}`);
            return response; // 攔截器會自動回傳 data
        } catch (error) {
            console.error('獲取即將上映電影失敗:', error.message);
            throw error;
        }
    },
    

};

export default PublicMovieService;