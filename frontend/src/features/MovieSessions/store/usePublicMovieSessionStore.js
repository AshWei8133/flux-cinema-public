import { defineStore } from "pinia";
import PublicMovieSessionService from "../services/publicMovieSessionService";
import { computed, ref } from "vue";
import { formatDateForBooking, formatTimeForBooking } from '@/utils/dateUtils';

export const usePublicMovieSessionStore = defineStore('publicMovieSession', () => {
    // 用來存放從 API 獲取的「單一電影」的所有原始場次資料
    const currentMovieShowtimes = ref([]);
    const isLoading = ref(false);
    const error = ref(null);

    /**
     * 根據電影 ID，從後端獲取場次資料並更新狀態
     * @param {number} movieId - 要查詢的電影 ID
     */
    const fetchShowtimesByMovieId = async (movieId) => {
        // 開始載入，清空舊資料
        isLoading.value = true;
        error.value = null;
        currentMovieShowtimes.value = [];

        try {
            // 呼叫 Service 發送 API 請求
            const showtimes = await PublicMovieSessionService.getShowtimesByMovieId(movieId);
            // 成功後，更新狀態
            currentMovieShowtimes.value = showtimes;
        } catch (err) {
            console.error(`獲取電影(ID: ${movieId})場次失敗:`, err);
            // 錯誤訊息會由 httpClient 攔截器統一處理並顯示，
            // 這裡我們只記錄 store 自身的錯誤狀態
            error.value = '無法載入場次資料。';
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 提供一個專門用來清空場次資料的 action
     */
    const clearShowtimes = () => {
        currentMovieShowtimes.value = [];
        error.value = null;
    };

    /**
     * 一個可以根據條件查找特定場次的 getter。
     * @param {object} criteria - 篩選條件，包含 version, date, time
     * @returns {object|undefined} - 返回找到的第一個符合條件的場次物件，或 undefined
     */
    const getSessionByCriteria = computed(() => {
        return (criteria) => {
            if (!currentMovieShowtimes.value || !criteria.version || !criteria.date || !criteria.time) {
                return undefined;
            }

            return currentMovieShowtimes.value.find(session => {
                const sessionBusinessDate = formatDateForBooking(session.startTime)?.value;
                const sessionTime = formatTimeForBooking(session.startTime);

                return (
                    session.theater.theaterType.theaterTypeName === criteria.version &&
                    sessionBusinessDate === criteria.date &&
                    sessionTime === criteria.time
                );
            });
        };
    });

    return {
        currentMovieShowtimes,
        isLoading,
        error,
        fetchShowtimesByMovieId,
        clearShowtimes,
        getSessionByCriteria
    };
});