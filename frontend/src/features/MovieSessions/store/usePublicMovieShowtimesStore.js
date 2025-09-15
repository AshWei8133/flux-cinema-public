import { defineStore } from "pinia";
import { ref } from "vue";
import PublicMovieSessionService from "../services/publicMovieSessionService";
// 引入我們剛剛修改過的 Service

export const usePublicMovieShowtimesStore = defineStore('publicMovieShowtimes', () => {
    // =================================================================
    // ===== State (狀態) =====
    // =================================================================
    // 存放從 API 獲取的原始場次資料 (ShowtimeDTO 列表)
    const allShowtimes = ref([]);
    // 控制是否正在載入中
    const isLoading = ref(true);
    // 存放可能發生的錯誤訊息
    const error = ref(null);

    // =================================================================
    // ===== Actions (方法) =====
    // =================================================================

    /**
         * 將 Store 的狀態重置回初始值。
         * 這將在組件被卸載時呼叫，以防止狀態殘留。
         */
    const resetStore = () => {
        allShowtimes.value = [];
        error.value = null;
    };



    /**
     * 根據電影 ID，從後端獲取包含座位狀態的場次資料並更新狀態
     * @param {number} movieId - 要查詢的電影 ID
     */
    const fetchShowtimes = async (movieId) => {
        resetStore()
        // 1. 開始載入，並清空舊資料和錯誤
        isLoading.value = true;
        error.value = null;

        try {
            // 2. 呼叫我們在 Service 中新增的專屬方法
            const showtimesData = await PublicMovieSessionService.getShowtimesWithSeatStatus(movieId);
            // 3. 請求成功後，將獲取的資料存入 state
            allShowtimes.value = showtimesData;
        } catch (err) {
            console.error(`獲取電影(ID: ${movieId})的場次與座位狀態失敗:`, err);
            // 你的 Axios 攔截器已經會顯示 ElMessage 錯誤提示
            // 我們只需在 store 中記錄錯誤狀態即可
            error.value = '無法載入場次資料，請稍後再試。';
        } finally {
            // 4. 無論成功或失敗，最後都結束載入狀態
            isLoading.value = false;
        }
    };



    // 將 state 和 actions 回傳，讓組件可以使用
    return {
        allShowtimes,
        isLoading,
        error,
        fetchShowtimes,
        resetStore
    };
});