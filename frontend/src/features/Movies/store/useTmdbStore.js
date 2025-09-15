import { defineStore } from 'pinia'
import { ref } from 'vue'
import MovieApiService from '../services/MovieApiService'

export const useTmdbStore = defineStore('tmdb', () => {
    // --- State (狀態) ---
    const tmdbMovies = ref([])
    const isLoading = ref(false)
    const error = ref(null)

   // 【已修改】將狀態名稱改為 existingMovieTitles，語意更清晰
    // 這個 Set 現在會用來存放所有已存在於本地 movie 表中的「本地片名 (titleLocal)」。
    const existingMovieTitles = ref(new Set());


    // --- 動作 (Actions) ---

    /**
     * 【已修改】獲取所有已存在於本地的「電影名稱」。
     * 這個版本會共用您現有的 API，並在前端處理資料。
     */
    const fetchExistingMovieTitles = async () => {
        try {
            // 1. 呼叫共用 API，獲取所有本地電影的「完整」資料
            const allLocalMovies = await MovieApiService.getExistingLocalMovies();
            console.log('[偵錯] 從後端收到的原始本地電影列表:', allLocalMovies);

            // 2. 使用 .map() 從完整資料中，只挑出我們需要的 titleLocal
            const titles = allLocalMovies
                .filter(dto => dto.movie && dto.movie.titleLocal) // 安全檢查，確保 movie 和 titleLocal 存在
                .map(dto => dto.movie.titleLocal); // 只取出 titleLocal

            // 3. 將挑出來的「電影名稱」存入我們的 Set 狀態中
            existingMovieTitles.value = new Set(titles);
            console.log(`[Store] 已透過共用 API 成功獲取 ${titles.length} 筆本地已存在的電影名稱。`);
        } catch (error) {
            console.error("共用 API 獲取已存在的電影名稱列表失敗:", error);
            // 即使失敗，也給一個空的 Set，避免程式出錯
            existingMovieTitles.value = new Set();
        }
    };

    /**
     * 【已修改】獲取所有 TMDB 暫存電影
     * 我們現在會同時獲取「暫存區電影」和「已存在電影名稱列表」，讓資料同步
     */
    const fetchAllTmdbMovies = async () => {
        isLoading.value = true;
        try {
            await Promise.all([
                (async () => {
                    const movies = await MovieApiService.getAllTmdbMovies();
                    tmdbMovies.value = movies;
                })(),
                fetchExistingMovieTitles() // 【已修改】呼叫新的方法
            ]);
        } catch (error) {
            console.error("獲取 TMDB 電影列表失敗:", error);
            tmdbMovies.value = [];
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * [Action 2] 從 TMDB API 匯入新電影
     */
    async function importFromApi(startDate, endDate) {
        return await MovieApiService.importFromTmdbApiByDate(startDate, endDate)
    }

    /**
     * [Action 3] 批次匯入選定電影至本地資料庫
     * @param {Array<number>} tmdbMovieIds
     */
    async function importToLocal(tmdbMovieIds) {
        // 【已修正】呼叫修正後的方法名稱
        return await MovieApiService.importSelectedTmdbMovies(tmdbMovieIds)
    }

    return {
        tmdbMovies,
        isLoading,
        error,
        fetchAllTmdbMovies,
        importFromApi,
        importToLocal,
        fetchExistingMovieTitles,
        existingMovieTitles,
    }
})