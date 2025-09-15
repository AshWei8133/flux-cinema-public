import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import MovieSessionService from '../services/movieSessionSerive'

export const useMovieSessionStore = defineStore('movieSession', () => {
  // 數據
  // 儲存從後端回傳的整個月份總覽列表
  const monthlyOverview = ref([])
  // 儲存後端回傳的特定日期場次資料
  const scheduleData = ref([])
  // 新增讀取與錯誤狀態，用於提升 UI 互動體驗
  const isMovieSessionUpdateLoading = ref(false)
  const MovieSessionUpdateError = ref(null)

  // 一個 ref 來追蹤日曆月份資料的載入狀態
  const isLoading = ref(false)

  // 透過 computed 屬性，從總覽列表中提取出 dateStatus
  const dateStatus = computed(() => {
    const statusMap = {}
    if (monthlyOverview.value.length > 0) {
      monthlyOverview.value.forEach((dailyData) => {
        statusMap[dailyData.date] = dailyData.status
      })
    }
    return statusMap
  })

  // 透過 computed 屬性，從總覽列表中提取出 sessions
  const monthlySessions = computed(() => {
    const sessionsMap = {}
    if (monthlyOverview.value.length > 0) {
      monthlyOverview.value.forEach((dailyData) => {
        sessionsMap[dailyData.date] = dailyData.dailySessionsByTheaterId
      })
    }
    return sessionsMap
  })

  // 方法
  const getMonthlyMovieSessionOverview = async (year, month) => {
    isLoading.value = true
    try {
      // 呼叫取得日期場次方法
      const data = await MovieSessionService.getMonthlyMovieSessionOverview(year, month)

      // 將整個回傳列表儲存到狀態中
      monthlyOverview.value = data

      // console.log(year + "年" + month + "月資料載入成功");

      return { success: true }
    } catch (error) {
      const errorMessage = error.response?.data?.message || '取得日曆場次總覽失敗。'
      return { success: false, message: errorMessage }
    } finally {
      // 無論成功或失敗，請求結束後，都將 isLoading 設為 false
      isLoading.value = false
    }
  }

  const getSessionsByDate = async (date) => {
    try {
      // 呼叫 service 取得特定日期所有場次資料
      const data = await MovieSessionService.getSessionsByDate(date)
      scheduleData.value = data

      return { success: true }
    } catch (error) {
      const errorMessage = error.response?.data?.message || '取得特定日期場次資料失敗。'
      return { success: false, message: errorMessage }
    }
  }

  /**
   * 儲存排程變更的 Action
   * @param {string} dateString - 'YYYY-MM-DD'
   * @param {Array} schedulesToSave - 要儲存的排程陣列
   * @returns {Promise<boolean>} - 回傳操作是否成功
   */
  const saveSchedules = async (dateString, schedulesToSave) => {
    isMovieSessionUpdateLoading.value = true
    MovieSessionUpdateError.value = null
    try {
      await MovieSessionService.updateSchedulesByDate(dateString, schedulesToSave)

      // 儲存成功後，必須重新獲取一次當天資料，以同步前端狀態。
      await getSessionsByDate(dateString)

      return true // 回傳 true 表示成功
    } catch (err) {
      MovieSessionUpdateError.value = err.response?.data?.message || '儲存排程失敗。'
      throw err
    } finally {
      isMovieSessionUpdateLoading.value = false
    }
  }

  // return 給應用程式使用
  return {
    // 暴露新的方法和計算屬性
    monthlyOverview,
    dateStatus,
    monthlySessions,
    scheduleData,
    isMovieSessionUpdateLoading,
    MovieSessionUpdateError,
    isLoading,
    getMonthlyMovieSessionOverview,
    getSessionsByDate,
    saveSchedules,
  }
})
