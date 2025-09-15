import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import PublicMovieSessionService from '../services/publicMovieSessionService'
import BookingApiService from '@/features/Ticket/services/bookingApiService'

export const usePublicSeatSelectionStore = defineStore('publicSeatSelection', () => {
    // 數據
    const SESSION_INFO_KEY = 'flux_session_info'
    const TICKET_ORDER_KEY = 'flux_ticket_order'
    const SELECTED_SEATS_KEY = 'flux_selected_seats'
    const PAYMENT_METHOD_KEY = 'flux_payment_method'
    const TEMP_ORDER_ID_KEY = 'flux_temp_order_id'
    const RESERVATION_EXPIRY_KEY = 'flux_reservation_expiry'

    const loadFromSessionStorage = (key, defaultValue) => {
        const savedData = sessionStorage.getItem(key)
        if (savedData) {
            try {
                return JSON.parse(savedData)
            } catch (e) {
                console.error(`Failed to parse ${key} from sessionStorage`, e)
                sessionStorage.removeItem(key)
                return defaultValue
            }
        }
        return defaultValue
    }

    const sessionInfo = ref(loadFromSessionStorage(SESSION_INFO_KEY, null))
    const ticketOrder = ref(loadFromSessionStorage(TICKET_ORDER_KEY, { tickets: [], totalCount: 0, totalPrice: 0 }))
    const selectedSeats = ref(loadFromSessionStorage(SELECTED_SEATS_KEY, []))
    const paymentMethod = ref(loadFromSessionStorage(PAYMENT_METHOD_KEY, null))
    const temporaryOrderId = ref(loadFromSessionStorage(TEMP_ORDER_ID_KEY, null))
    const reservationExpiry = ref(loadFromSessionStorage(RESERVATION_EXPIRY_KEY, null))
    const sessionSeats = ref([])
    const isLoading = ref(false)
    const error = ref(null)

    watch(sessionInfo, (newValue) => {
        if (newValue) {
            sessionStorage.setItem(SESSION_INFO_KEY, JSON.stringify(newValue))
        } else {
            sessionStorage.removeItem(SESSION_INFO_KEY)
        }
    }, { deep: true })

    watch(ticketOrder, (newValue) => {
        sessionStorage.setItem(TICKET_ORDER_KEY, JSON.stringify(newValue))
    }, { deep: true })

    watch(selectedSeats, (newValue) => {
        sessionStorage.setItem(SELECTED_SEATS_KEY, JSON.stringify(newValue))
    }, { deep: true })

    watch(paymentMethod, (newValue) => {
        if (newValue) {
            sessionStorage.setItem(PAYMENT_METHOD_KEY, JSON.stringify(newValue));
        } else {
            sessionStorage.removeItem(PAYMENT_METHOD_KEY);
        }
    });

    watch(temporaryOrderId, (newValue) => {
        sessionStorage.setItem(TEMP_ORDER_ID_KEY, JSON.stringify(newValue));
    });

    watch(reservationExpiry, (newValue) => {
        sessionStorage.setItem(RESERVATION_EXPIRY_KEY, JSON.stringify(newValue));
    });

    const totalTicketCount = computed(() => ticketOrder.value.totalCount)

    const fetchSeatLayout = async (sessionId) => {
        isLoading.value = true
        error.value = null
        sessionSeats.value = []

        try {
            const responseData = await PublicMovieSessionService.getSessionSeatLayout(sessionId)
            sessionInfo.value = responseData.sessionInfo
            sessionSeats.value = responseData.seats
        } catch (err) {
            console.error(`獲取場次(ID: ${sessionId})座位圖失敗:`, err)
            error.value = '無法載入座位資訊，請稍後再試或返回上一頁。'
            clearAllBookingState()
        } finally {
            isLoading.value = false
        }
    }

    /**
     * @修改方法：清除所有訂票相關狀態，用於完全離開流程。
     */
    const clearAllBookingState = () => {
        sessionInfo.value = null
        sessionSeats.value = []
        error.value = null
        selectedSeats.value = []
        ticketOrder.value = { tickets: [], totalCount: 0, totalPrice: 0 }
        paymentMethod.value = null
        temporaryOrderId.value = null
        reservationExpiry.value = null
    }

    /**
     * @新增方法：清除選位及臨時訂單ID，但保留票種資訊，用於流程中往回走。
     */
    const clearSeatsAndOrderData = () => {
        selectedSeats.value = []
        temporaryOrderId.value = null
        reservationExpiry.value = null
    }

    /**
     * @修改方法：取消臨時預訂，並可選擇是否完全清理所有前端狀態。
     * @param {boolean} shouldClearAll - 是否要清除所有與訂票相關的前端狀態。
     */
    async function cancelTemporaryOrder(shouldClearAll = true) {
        const orderId = temporaryOrderId.value
        if (!orderId) {
            if (shouldClearAll) {
                clearAllBookingState()
            } else {
                clearSeatsAndOrderData()
            }
            return
        }

        try {
            await BookingApiService.cancelReservation(orderId)
            console.log(`後端臨時訂單 ${orderId} 已成功取消。`)
        } catch (error) {
            console.error(`取消後端訂單 ${orderId} 失敗:`, error)
        } finally {
            if (shouldClearAll) {
                clearAllBookingState()
            } else {
                clearSeatsAndOrderData()
            }
        }
    }

    function setTicketOrder(orderDetails) {
        ticketOrder.value = orderDetails
        selectedSeats.value = []
    }

    function toggleSeatSelection(seat) {
        const seatId = seat.sessionSeatId
        const index = selectedSeats.value.findIndex((s) => s.sessionSeatId === seatId)

        if (index !== -1) {
            selectedSeats.value.splice(index, 1)
        } else {
            if (selectedSeats.value.length < ticketOrder.value.totalCount) {
                selectedSeats.value.push(seat)
            } else {
                console.warn('已達到選取上限，無法再增加座位。')
            }
        }
    }

    function clearSelectedSeats() {
        selectedSeats.value = []
    }

    function setPaymentMethod(method) {
        paymentMethod.value = method
    }

    async function createReservation(payload) {
        try {
            const response = await BookingApiService.reserveSeats(payload)
            temporaryOrderId.value = response.orderNumber
            reservationExpiry.value = response.reservedExpiredDate
            console.log(`臨時訂單 ${temporaryOrderId.value} 已建立，座位將保留至 ${reservationExpiry.value}`)
            return { success: true }
        } catch (err) {
            console.error('建立臨時預訂失敗:', err)
            const message = err.response?.data?.message || err.response?.data || '預訂座位失敗，請稍後再試。'
            return { success: false, message: message }
        }
    }

    return {
        sessionInfo, sessionSeats, isLoading, error, ticketOrder,
        selectedSeats, paymentMethod, temporaryOrderId, reservationExpiry,
        totalTicketCount,
        // 【修改】將新的方法暴露出去
        fetchSeatLayout, clearAllBookingState, clearSeatsAndOrderData, setTicketOrder, toggleSeatSelection,
        clearSelectedSeats, setPaymentMethod, createReservation, cancelTemporaryOrder,
    }
})