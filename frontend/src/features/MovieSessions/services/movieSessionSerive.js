import httpClient from "@/services/api";

const MovieSessionService = {
    /*取得特定年月的各日期場次狀態資料*/
    async getMonthlyMovieSessionOverview(year, month) {
        try {
            const response = await httpClient.get(`/admin/movieSession/monthly-overview/${year}/${month}`)
            return response;
        } catch (error) {
            console.error('取得日曆場次總覽失敗', error.message);
            throw error;
        }
    },

    async getSessionsByDate(date) {
        try {
            const response = await httpClient.get(`/admin/movieSession/${date}`)
            return response;
        } catch (error) {
            console.error('取得特定日期所有場次失敗', error.message);
            throw error;
        }
    },

    /**
     * 批次更新指定日期的所有電影場次
     * @param {string} date - 日期字串 'YYYY-MM-DD'
     * @param {Array} schedules - 當天所有的排程物件陣列
     * @returns {Promise<any>}
     */
    async updateSchedulesByDate(date, schedules) {
        try {
            const data = await httpClient.put(`/admin/movieSession/batch-update/${date}`, schedules);
            return data;
        } catch (error) {
            console.error(`更新日期 ${date} 的排程失敗`, error.message);
            throw error;
        }
    }
}



// 暴露service對象
export default MovieSessionService;