/**
 * src/hooks/useCalendar.js
 * 處理日曆導航和狀態的 Hook
 * 這個 Hook 就像是日曆的「大腦」，專門處理日期的選擇、切換月份，
 * 以及根據日期狀態回傳對應的樣式。
 */
import { ref, watch, computed } from 'vue'
import { isSameMonth, formatDateString, getChineseDate } from '@/utils/dateUtils'
import { storeToRefs } from 'pinia'
import { useMovieSessionStore } from '@/features/MovieSessions/store/useMovieSessionStore'

export function useCalendar() {
    // 透過 Pinia 取得電影場次資料的 Store
    const movieSessionStore = useMovieSessionStore()
    const { dateStatus, isLoading } = storeToRefs(movieSessionStore)

    // ====================數據資料==================================

    const todayDate = new Date() // 儲存「今天」這個日期，作為固定參考點，不受響應式影響。

    // 儲存目前選擇的日期，預設為今天。
    const selectedDate = ref(new Date())
    // 根據 selectedDate 計算出的年份。
    const selectedYear = ref(selectedDate.value.getFullYear())
    // 根據 selectedDate 計算出的月份 (+1 是因為 getMonth() 回傳 0-11)。
    const selectedMonth = ref(selectedDate.value.getMonth() + 1)

    // 控制年份/月份選擇器是否顯示的開關。【目前是設定點選日曆上面的年月可以開啟選擇器】
    const showYearMonthSelector = ref(false)
    const yearList = ref([]) // 儲存可供選擇的年份列表，例如從今年往前推 5 年，往後推 5 年。
    const tempYear = ref(selectedDate.value.getFullYear()) // 用於年份/月份選擇器，暫存使用者選擇的年份。
    const tempMonth = ref(selectedDate.value.getMonth() + 1) // 用於年份/月份選擇器，暫存使用者選擇的月份。

    // 初始化年份列表，從今年往前和往後各 5 年。
    for (let i = todayDate.getFullYear() - 5; i <= todayDate.getFullYear() + 5; i++) {
        yearList.value.push(i)
    }

    //=====================================================================

    // =================計算屬性（Computed）================================

    // 判斷當前日曆顯示的月份是否為本月。
    const isTodayMonth = computed(() => isSameMonth(selectedDate.value, todayDate))

    //=====================================================================

    // ==================方法（Methods）====================================

    // -----------月份導航方法----------------

    /**
     * 切換至上個月
     */
    const prevMonth = () => {
        const currentDate = selectedDate.value;
        // 建立新日期時，直接指定為目標月份的 1 號
        const newDate = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
        selectedDate.value = newDate;
        showYearMonthSelector.value = false;
    }

    /**
     * 切換下個月
     */
    const nextMonth = () => {
        const currentDate = selectedDate.value;
        // 建立新日期時，直接指定為目標月份的 1 號
        const newDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1);
        selectedDate.value = newDate;
        showYearMonthSelector.value = false;
    }

    /**
     * 切換至今天的年月
     */
    const today = () => {
        selectedDate.value = todayDate // 直接將日期設定回今天。
        showYearMonthSelector.value = false // 隱藏選擇器。
    }

    // ----------------------------------------

    // ---------操作年/月選擇器方法----------------
    /**
     * 開啟或關閉年/月選擇器
     */
    const toggleYearMonthSelector = () => {
        // 切換年份/月份選擇器的顯示狀態。
        showYearMonthSelector.value = !showYearMonthSelector.value
        // 如果選擇器準備顯示(開啟)，將暫存的年份和月份設定為當前顯示的年份和月份，
        // 這樣使用者打開選擇器時，會從當前月份開始。
        if (showYearMonthSelector.value) {
            tempYear.value = selectedYear.value
            tempMonth.value = selectedMonth.value
        }
    }

    /**
     * 確認年/月選擇器日期並提交
     */
    const confirmChange = () => {
        // 確認使用者在選擇器中的選擇，並更新日曆。
        selectedYear.value = tempYear.value
        selectedMonth.value = tempMonth.value
        // 建立一個新日期物件，月份要 -1，因為 setDate 的月份是從 0 開始。
        const newDate = new Date(selectedYear.value, selectedMonth.value - 1, 1)
        selectedDate.value = newDate // 更新日期，觸發 watch 監聽。
        showYearMonthSelector.value = false // 隱藏選擇器。
    }

    /**
     * 取消年/月日期選擇器
     */
    const cancelChange = () => {
        // 取消變更，直接隱藏選擇器。
        showYearMonthSelector.value = false
    }

    // ----------------------------------------

    // -----取得日曆單元格(日期)的排片狀態----------
    // 根據日期的狀態返回 CSS 類別
    // 這個函式接收 Element Plus 日曆元件傳入的日期 data，並回傳一個物件，
    // 鍵名是 CSS 類別，值為 true/false 來決定是否應用該類別。
    const cellClasses = (data) => {
        // 將取得日曆單元格物件(data)中日期，並將日期格式化為YYYY-MM-DD
        const formattedDate = formatDateString(data.date)
        // 判斷該日期的排片狀態【從store中取得】
        const status = dateStatus.value[formattedDate]
        // 判斷日期是否與當前日曆顯示的月份相同，避免上個月和下個月的日期被誤判為歷史日期。
        const isCurrentMonthCell = isSameMonth(data.date, selectedDate.value)

        return {
            // 【當 isLoading 為 true 時，為儲存格加上 'is-loading' class
            'is-loading': isLoading.value,
            'is-today-highlight': status === 'TODAY', // 今天
            'is-history-date': status === 'HISTORY', // 歷史日期
            'is-pending-date': status === 'PENDING', // 待排片
            'is-completed-date': status === 'COMPLETED', // 已排片
            'is-none-date': status === 'NONE', // 無狀態
            // 判斷是否為「過去日期」，這裡特別檢查是否在同一個月，以免跨月份的日期也被歸類。
            'is-past-date': isCurrentMonthCell && data.date < todayDate,
        }
    }
    // ----------------------------------------

    //=====================================================================

    // ========監聽器（Watcher）：監聽 selectedDate 屬性的變化================
    watch(selectedDate, (newDate) => {
        // 當 selectedDate 改變時，更新年份和月份狀態。
        selectedYear.value = newDate.getFullYear()
        selectedMonth.value = newDate.getMonth() + 1
        // 並呼叫 Pinia Store 的 Action，從後端取得該月份的電影場次總覽資料。(包含狀態及排片概況)
        movieSessionStore.getMonthlyMovieSessionOverview(selectedYear.value, selectedMonth.value)
    })

    //=====================================================================


    // 暴露給其他元件使用
    return {
        selectedDate,
        selectedYear,
        selectedMonth,
        showYearMonthSelector,
        tempYear,
        tempMonth,
        yearList,
        isTodayMonth,
        prevMonth,
        nextMonth,
        today,
        toggleYearMonthSelector,
        confirmChange,
        cancelChange,
        isLoading,
        cellClasses,
        getChineseDate, // 由於 getChineseDate 是從外部引入，這裡也需要回傳才能在元件中使用。
    }
}