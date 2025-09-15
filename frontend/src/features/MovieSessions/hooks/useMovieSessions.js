/**
 * src/hooks/useMovieSessions.js
 * 處理電影排片資料的 Hook
 */
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useMovieSessionStore } from '@/features/MovieSessions/store/useMovieSessionStore'
import { useTheaterStore } from '@/features/Theaters/store/useTheaterStore'
import { formatTime, formatDateString } from '@/utils/dateUtils'

export function useMovieSessions() {
    const movieSessionStore = useMovieSessionStore()
    const { dateStatus, monthlySessions } = storeToRefs(movieSessionStore)
    const theaterStore = useTheaterStore()
    const { theaters, theaterTypes } = storeToRefs(theaterStore)

    // 建立動態的影廳對照表，方便查詢
    const theaterMap = computed(() => {
        const map = new Map()
        theaters.value.forEach((t) => map.set(t.theaterId, t))
        return map
    })

    const theaterTypeMap = computed(() => {
        const map = new Map()
        theaterTypes.value.forEach((t) => map.set(t.theaterTypeId, t.theaterTypeName))
        return map
    })

    // 根據排片狀態碼，返回對應的中文描述（業務邏輯）
    const getStatusText = (statusClass) => {
        switch (statusClass) {
            case 'COMPLETED':
                return '已完成'
            case 'PENDING':
                return '待排'
            case 'HISTORY':
                return ''
            case 'TODAY':
                return '今天'
            case 'NONE':
                return ''
            default:
                return ''
        }
    }

    // 判斷某個日期是否有排片狀態（業務邏輯）
    const dateHasSchedule = (dateString) => {
        const status = dateStatus.value[dateString]
        return !!status
    }

    // 取得某個日期的排片場次資料並分組
    const getSessions = (date) => {
        const formattedDate = formatDateString(date)
        const dailySessionsById = monthlySessions.value[formattedDate] || {}
        const groupedData = {}

        for (const theaterId in dailySessionsById) {
            const theaterInfo = theaterMap.value.get(Number(theaterId))
            if (!theaterInfo) continue

            const typeName = theaterTypeMap.value.get(theaterInfo.theaterTypeId) || '未知類別'

            if (!groupedData[typeName]) {
                groupedData[typeName] = {}
            }

            groupedData[typeName][theaterInfo.theaterName] = dailySessionsById[theaterId].map((session) => {
                return {
                    ...session,
                    startTime: formatTime(session.startTime),
                    endTime: formatTime(session.endTime),
                    movieTitle: session.titleLocal,
                }
            })
        }
        return groupedData
    }

    // 在元件載入時，發起 API 請求
    onMounted(() => {
        theaterStore.fetchAllTheaters()
        theaterStore.fetchAllTheaterTypes()
        movieSessionStore.getMonthlyMovieSessionOverview(new Date().getFullYear(), new Date().getMonth() + 1)
    })

    return {
        dateStatus,
        getSessions,
        getStatusText,
        dateHasSchedule,
    }
}