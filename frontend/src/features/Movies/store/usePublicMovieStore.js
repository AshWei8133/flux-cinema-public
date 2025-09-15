// 檔案: src/features/Movies/store/usePublicMovieStore.js

import { defineStore } from 'pinia';
import { ref } from 'vue';
import PublicMovieService from '../services/PublicMovieService'; // <-- 請確認您的 Service 路徑

/**
 * 這個 Store 專門用來管理前台公開頁面的電影資料
 */
export const usePublicMovieStore = defineStore('publicMovies', () => {

    // 【已新增】用來存放當前選擇的、單一電影的詳細資料
    const selectedMovie = ref(null);
    // --- 狀態 (State) ---

    const nowPlayingMovies = ref([]);
    // 【必須存在】用來存放「即將上映」的電影列表
    const comingSoonMovies = ref([]);

    const isLoading = ref(false);
    const error = ref(null);


    // --- 動作 (Actions) ---

    const fetchNowPlayingMovies = async () => {
        isLoading.value = true;
        error.value = null;
        try {
            const today = new Date();
            const year = today.getFullYear();
            const month = String(today.getMonth() + 1).padStart(2, '0');
            const day = String(today.getDate()).padStart(2, '0');
            const dateString = `${year}-${month}-${day}`;

            const movies = await PublicMovieService.getNowPlayingMovies(dateString);
            nowPlayingMovies.value = movies;
        } catch (err) {
            console.error("獲取前台熱映電影失敗:", err);
            error.value = '無法載入電影資料，請稍後再試。';
        } finally {
            isLoading.value = false;
        }
    };

    const fetchComingSoonMovies = async () => {
        isLoading.value = true;
        error.value = null;
        try {
            const today = new Date();
            const year = today.getFullYear();
            const month = String(today.getMonth() + 1).padStart(2, '0');
            const day = String(today.getDate()).padStart(2, '0');
            const dateString = `${year}-${month}-${day}`;

            const movies = await PublicMovieService.fetchComingSoonMovies(dateString);
            comingSoonMovies.value = movies;
        } catch (err) {
            console.error("獲取即將上映電影失敗:", err);
            error.value = '無法載入即將上映的電影，請稍後再試。';
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 【已新增】根據 ID 獲取單一電影的詳細資料
     * 這個方法會呼叫您 Canvas 中的 PublicMovieService.getMovieById
     * @param {number} id 電影的 ID
     */
    async function fetchMovieById(id) {
        // 1. 開始載入，並清除之前的錯誤和舊的電影資料
        isLoading.value = true;
        selectedMovie.value = null;
        error.value = null;
        try {
            // 2. 呼叫您 Canvas 中的 Service 方法，並等待後端回傳資料
            const movieData = await PublicMovieService.getMovieById(id);

            // 3. 檢查是否有成功拿到資料
            if (movieData) {
                // 4. 如果有，就把拿到的電影詳細資料存入 selectedMovie 這個狀態中
                selectedMovie.value = movieData;
                console.log(selectedMovie.value);
            } else {
                // 如果 Service 回傳了空值，就拋出一個錯誤
                throw new Error('找不到該電影');
            }
        } catch (e) {
            // 5. 如果在過程中發生任何錯誤，就記錄下來
            console.error(`載入電影(ID: ${id})詳細資料時發生錯誤:`, e);
            error.value = '無法載入電影詳細資料。';
        } finally {
            // 6. 無論成功或失敗，最後都要結束載入狀態
            isLoading.value = false;
        }
    }

    /**
         * 將當前選擇的電影相關狀態重置回初始值。
         * 這在切換不同電影頁面時非常重要，可以防止舊資料短暫顯示。
         */
    function clearSelectedMovie() {
        selectedMovie.value = null;
        // 如果需要，也可以重置 loading 和 error 狀態
        // isLoading.value = false;
        // error.value = null;
    }


    // 將所有狀態和方法回傳出去，讓 Vue 元件可以使用
    return {
        nowPlayingMovies,
        comingSoonMovies,
        selectedMovie, // <-- 導出新狀態
        isLoading,
        error,
        fetchNowPlayingMovies,
        fetchComingSoonMovies,
        fetchMovieById, // <-- 導出新方法
        clearSelectedMovie
    };
});
