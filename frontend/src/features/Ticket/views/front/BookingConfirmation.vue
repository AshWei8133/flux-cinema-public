<template>
  <div v-if="isSeatStoreLoading || !isPageReady" class="loading-container">
    <p>正在為您載入訂單資訊...</p>
  </div>

  <div v-else class="confirmation-container">
    <div class="details-column">
      <div class="info-card">
        <h3 class="card-title">訂單明細</h3>

        <div class="card-content">
          <div class="info-group">
            <div class="info-row">
              <span class="label">電影名稱</span>
              <span class="value">{{ sessionInfo?.movieTitle }}</span>
            </div>

            <div class="info-row">
              <span class="label">電影版本</span>
              <span class="value">{{ sessionInfo?.version }}</span>
            </div>

            <div class="info-row">
              <span class="label">影廳</span>
              <span class="value">{{ sessionInfo?.theaterName }}</span>
            </div>

            <div class="info-row">
              <span class="label">場次時間</span>
              <span class="value">{{ formattedShowtime }}</span>
            </div>
          </div>

          <div class="divider"></div>

          <div class="info-group">
            <div v-for="ticket in ticketOrder.tickets" :key="ticket.ticketTypeId" class="info-row">
              <span class="label">{{ ticket.name }} x {{ ticket.quantity }}</span>
              <span class="value">NT$ {{ ticket.price * ticket.quantity }}</span>
            </div>

            <div class="info-row">
              <span class="label">已選座位</span>
              <span class="value seat-list">{{ seatListString }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="info-card">
        <h3 class="card-title">金額計算</h3>

        <div class="card-content">
          <div class="info-row">
            <span class="label">電影票小計</span>
            <span class="value">NT$ {{ subtotal }}</span>
          </div>

          <div class="info-row coupon-row">
            <span class="label">使用優惠券</span>
            <div v-if="isLoadingCoupons" class="coupon-feedback">正在載入優惠券...</div>
            <div v-else-if="couponError" class="coupon-feedback error">{{ couponError }}</div>

            <select v-else v-model="selectedCouponId" class="coupon-select">
              <option :value="null">不使用優惠券</option>

              <option
                v-for="coupon in applicableCoupons"
                :key="coupon.memberCouponId"
                :value="coupon.memberCouponId"
                :disabled="!coupon.usable"
              >
                {{ coupon.couponName }} (滿 {{ coupon.minimumSpend }} 折
                {{ coupon.discountAmount }})
                <template v-if="!coupon.usable">
                  (還差 {{ coupon.minimumSpend - subtotal }} 元)
                </template>
              </option>
            </select>
          </div>

          <div v-if="discountAmount > 0" class="info-row discount-row">
            <span class="label">優惠券折抵</span>
            <span class="value">- NT$ {{ discountAmount }}</span>
          </div>

          <div class="divider"></div>

          <div class="info-row final-total-row">
            <span class="label">應付總額</span>
            <span class="value final-total">NT$ {{ finalTotal }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="actions-column">
      <div class="info-card">
        <h3 class="card-title">訂購人資訊</h3>

        <div class="card-content">
          <div class="info-row">
            <span class="label">會員姓名</span>
            <span class="value">{{ memberInfo?.username }}</span>
          </div>

          <div class="info-row">
            <span class="label">電子郵件</span>
            <span class="value">{{ memberInfo?.email }}</span>
          </div>

          <div class="info-row">
            <span class="label">聯絡電話</span>
            <span class="value">{{ memberInfo?.phone }}</span>
          </div>
        </div>
      </div>

      <div class="info-card">
        <template v-if="paymentMethod === 'online'">
          <h3 class="card-title">信用卡付款</h3>

          <div class="countdown-timer">
            <span v-if="remainingTimeInSeconds > 0">
              請於 <span class="time-value">{{ formattedTime }}</span> 內完成付款
            </span>
            <span v-else class="expired-text">付款已逾時，請返回重新訂票。</span>
          </div>

          <div class="card-content payment-options">
            <div class="payment-option-static">
              <div class="payment-option-content">
                <Icon icon="mdi:credit-card-outline" class="payment-icon" />
                <span>信用卡</span>
              </div>
            </div>

            <p class="notice-text">您即將前往 ECPay 進行安全付款。</p>
          </div>

          <div class="card-footer">
            <button
              class="confirm-btn"
              @click="confirmOrder"
              :disabled="isLoadingPayment || remainingTimeInSeconds <= 0"
            >
              {{ isLoadingPayment ? '付款處理中...' : '確認付款' }}
            </button>
          </div>
        </template>

        <template v-else-if="paymentMethod === 'counter'">
          <h3 class="card-title">臨櫃付款須知</h3>

          <div class="card-content">
            <p class="notice-text">
              您的訂單將為您保留，請於電影開演前
              <strong>30 分鐘</strong> 至影城櫃檯完成付款取票，逾時訂單將自動取消。
            </p>
          </div>

          <div class="card-footer">
            <button class="confirm-btn" @click="confirmOrder">確認預訂</button>
          </div>
        </template>
      </div>
    </div>
  </div>

  <form ref="ecpayForm" :action="ecpayApiUrl" method="POST" style="display: none">
    <input v-for="(value, key) in ecpayParams" :key="key" :name="key" :value="value" />
  </form>
</template>

<script setup>
// ----------------------------------------------------------------
// 依賴引入 (Dependencies)
// ----------------------------------------------------------------
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePublicSeatSelectionStore } from '@/features/MovieSessions/store/usePublicSeatSelectionStore'
import { storeToRefs } from 'pinia'
import { Icon } from '@iconify/vue'
import { useMemberAuthStore } from '@/stores/memberAuth'
import { usePaymentStore } from '../../store/usePaymentStore'
import { useTicketOrderCoupon } from '../../store/useTicketOrderCoupon'

// ----------------------------------------------------------------
// 初始化與實例化
// ----------------------------------------------------------------
const route = useRoute()
const router = useRouter()
const seatSelectionStore = usePublicSeatSelectionStore()
const memberAuthStore = useMemberAuthStore()
const paymentStore = usePaymentStore()
const couponStore = useTicketOrderCoupon() // 【修改】實例化您的 Coupon Store

// ----------------------------------------------------------------
// Pinia Store 狀態管理
// ----------------------------------------------------------------
const {
  temporaryOrderId,
  sessionInfo,
  ticketOrder,
  selectedSeats,
  isLoading: isSeatStoreLoading,
  reservationExpiry,
} = storeToRefs(seatSelectionStore)
const { memberInfo, isAuthenticated } = storeToRefs(memberAuthStore)
const { isLoading: isLoadingPayment, ecpayApiUrl, ecpayParams } = storeToRefs(paymentStore)
// 【修改】從 Coupon Store 中獲取狀態
const {
  applicableCoupons,
  isLoading: isLoadingCoupons,
  error: couponError,
} = storeToRefs(couponStore)

// ----------------------------------------------------------------
// 本地響應式狀態
// ----------------------------------------------------------------
const paymentMethod = ref('')
// const availableCoupons = ref([]) // 【刪除】不再需要本地 ref，將完全由 Store 控制
const selectedCouponId = ref(null)
const ecpayForm = ref(null)
const isPageReady = ref(false)
const remainingTimeInSeconds = ref(0)
let countdownTimer = null

// ----------------------------------------------------------------
// 計算屬性 (Computed Properties)
// ----------------------------------------------------------------

const formattedShowtime = computed(() => {
  if (!sessionInfo.value?.showtime) return 'N/A'
  const date = new Date(sessionInfo.value.showtime)
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
})

const seatListString = computed(() => {
  if (!selectedSeats.value || selectedSeats.value.length === 0) return '尚未選擇座位'
  return selectedSeats.value.map((s) => `${s.seat.rowNumber}排${s.seat.columnNumber}號`).join(', ')
})

const subtotal = computed(() => ticketOrder.value?.totalPrice || 0)

// 【修改】selectedCoupon 現在從 couponStore 的 applicableCoupons 中查找
const selectedCoupon = computed(() => {
  if (!selectedCouponId.value) return null // 後端回傳的 ID 是 memberCouponId，這裡要對應
  return applicableCoupons.value.find((c) => c.memberCouponId === selectedCouponId.value)
})

// 【修改】discountAmount 直接使用後端為我們計算好的值
const discountAmount = computed(() => {
  if (!selectedCoupon.value) return 0

  // 我們現在完全信任後端返回的 isUsable 旗標和 calculatedDiscountAmount
  if (selectedCoupon.value.usable) {
    return selectedCoupon.value.discountAmount
  }

  // 如果因某些原因 (例如 subtotal 發生變化) 導致已選的券變為不可用，
  // 則自動取消選中並回傳 0 折扣
  selectedCouponId.value = null
  return 0
})

const finalTotal = computed(() => {
  const total = subtotal.value - discountAmount.value
  return total > 0 ? total : 0
})

const ecpayItemName = computed(() => {
  if (!ticketOrder.value?.tickets) return '電影票券'
  return ticketOrder.value.tickets.map((t) => `${t.name} x ${t.quantity}`).join('#')
})

const formattedTime = computed(() => {
  const minutes = Math.floor(remainingTimeInSeconds.value / 60)
  const seconds = remainingTimeInSeconds.value % 60
  const formattedMinutes = minutes < 10 ? '0' + minutes : minutes
  const formattedSeconds = seconds < 10 ? '0' + seconds : seconds
  return `${formattedMinutes}:${formattedSeconds}`
})

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------

// 【刪除】舊的假資料方法
// function loadAvailableCoupons() { ... }

/**
 * 處理付款按鈕的點擊事件
 */
async function confirmOrder() {
  if (!temporaryOrderId.value) {
    alert('訂單資訊遺失，請重新訂票。')
    return
  }

  // 統一建立 payload
  const payload = {
    encodedOrderId: temporaryOrderId.value,
    couponId: selectedCouponId.value,
  }

  if (paymentMethod.value === 'online') {
    // 線上付款邏輯保持不變
    const success = await paymentStore.updateOrderAndCheckout(payload)
    if (success) {
      await nextTick()
      if (ecpayForm.value) {
        ecpayForm.value.submit()
      } else {
        console.error('無法找到 ecpayForm 元素！')
      }
    }
  } else {
    // 臨櫃付款邏輯
    // 1. 呼叫 paymentStore 的新 action
    const success = await paymentStore.confirmCounterOrder(payload)

    // 2. 如果後端成功確認訂單
    if (success) {
      alert('訂單已為您預訂！\n請記得在開演前30分鐘至櫃檯付款取票。')

      // 3. 清除所有訂票流程的狀態
      seatSelectionStore.clearAllBookingState()

      // 4. 導航回首頁或訂單查詢頁
      router.push({ name: 'Home' }) // 或 router.push({ name: 'MemberOrders' })
    }
    // 如果失敗，httpClient 攔截器會自動彈出錯誤訊息，使用者可以留在當前頁面重試
  }
}

/**
 * 啟動倒數計時器
 */
function startCountdown() {
  // 清除舊的計時器，避免重複啟動
  if (countdownTimer) {
    clearInterval(countdownTimer)
  } // 將後端回傳的時間字串轉換為 Date 物件

  const expiryDate = new Date(reservationExpiry.value)
  const now = new Date() // 計算到期時間與現在時間的秒數差

  remainingTimeInSeconds.value = Math.max(
    0,
    Math.floor((expiryDate.getTime() - now.getTime()) / 1000),
  ) // 每秒更新一次時間

  countdownTimer = setInterval(() => {
    if (remainingTimeInSeconds.value > 0) {
      remainingTimeInSeconds.value--
    } else {
      // 時間到期，清除計時器
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

// ----------------------------------------------------------------
// 生命週期鉤子 (Lifecycle Hooks)
// ----------------------------------------------------------------

// 【新增】監聽器，當 subtotal 或 sessionId 變化時自動重新獲取優惠券
// 雖然在此頁面 subtotal 通常不變，但這是個非常穩健的做法，以防未來有增加商品等功能
watch(
  [() => subtotal.value, () => sessionInfo.value?.sessionId],
  ([newSubtotal, newSessionId], [oldSubtotal, oldSessionId]) => {
    // 確保我們有必要的資訊，且頁面已準備就緒，才發送請求
    if (isPageReady.value && newSessionId && newSubtotal > 0) {
      // 只有在 subtotal 或 sessionId 真的改變時才重新請求，避免 onMounted 重複觸發
      if (newSubtotal !== oldSubtotal || newSessionId !== oldSessionId) {
        console.log('訂單資訊變更，重新獲取優惠券...')
        couponStore.fetchApplicableCoupons({
          sessionId: newSessionId,
          subtotal: newSubtotal,
        })
      }
    }
  },
  { immediate: false }, // 不在初始時立即執行，讓 onMounted 處理首次加載
)

onMounted(async () => {
  if (!isAuthenticated.value) {
    alert('請先登入會員才能確認訂單。')
    router.push({ name: 'MemberLogin', query: { redirect: route.fullPath } })
    return
  }

  if (!sessionInfo.value && ticketOrder.value.totalCount > 0) {
    console.log('偵測到頁面重新整理，嘗試從 sessionId 恢復場次資訊...')
    const sessionId = route.params.sessionId
    if (sessionId) {
      await seatSelectionStore.fetchSeatLayout(sessionId)
    }
  }

  if (!sessionInfo.value || ticketOrder.value.totalCount === 0) {
    console.warn('缺少訂單資訊，將導回訂票首頁')
    alert('您的訂票資訊已過期或不存在，請重新選擇場次。')
    seatSelectionStore.clearAllBookingState()
    router.push('/')
    return
  } // 1. 從路由參數獲取付款方式，如果沒有則預設為 online

  paymentMethod.value = route.query.payment || 'online' // 2. 如果是線上付款，並且有到期時間，就啟動計時器

  if (paymentMethod.value === 'online' && reservationExpiry.value) {
    startCountdown()
  }

  // 3. 【修改】觸發 action 從後端獲取優惠券資料
  if (sessionInfo.value?.sessionId && subtotal.value > 0) {
    await couponStore.fetchApplicableCoupons({
      sessionId: sessionInfo.value.sessionId,
      subtotal: subtotal.value,
    })
  }
  isPageReady.value = true
})

/**
 * 在組件卸載時 (例如頁面跳轉)，清除計時器，避免記憶體洩漏
 */
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  // 【新增】離開頁面時，也清空 coupon store 的狀態，避免資料污染
  couponStore.clearCouponState()
})
</script>

<style scoped>
/* 原有樣式保持不變 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50vh;
  font-size: 1.2rem;
  color: #ccc;
}
.confirmation-container {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.details-column {
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.actions-column {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: sticky;
  top: 24px;
}
.info-card {
  background-color: #2a2a2a;
  border: 1px solid #3c3c3c;
  border-radius: 8px;
  overflow: hidden;
}
.card-title {
  padding: 16px 20px;
  margin: 0;
  font-size: 18px;
  color: #fff;
  background-color: #333;
  border-bottom: 1px solid #3c3c3c;
}
.card-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card-footer {
  padding: 20px;
  background-color: #333;
  border-top: 1px solid #3c3c3c;
}
.info-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
}
.label {
  color: #aaa;
}
.value {
  color: #fff;
  font-weight: 500;
  text-align: right;
}
.seat-list {
  max-width: 70%;
  line-height: 1.5;
}
.divider {
  height: 1px;
  background-color: #3c3c3c;
  margin: 8px 0;
}
.coupon-select {
  background-color: #1f1f1f;
  color: #fff;
  border: 1px solid #555;
  border-radius: 4px;
  padding: 8px;
  font-size: 14px;
  width: 60%;
}
.coupon-select:disabled {
  opacity: 0.5;
}
/* 【新增】用於顯示加載中或錯誤訊息的樣式 */
.coupon-feedback {
  color: #aaa;
  width: 60%;
  text-align: left;
  padding: 8px;
}
.coupon-feedback.error {
  color: #f56c6c; /* 紅色錯誤文字 */
}

.discount-row .value {
  color: #67c23a;
}
.final-total-row .label {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}
.final-total-row .final-total {
  font-size: 24px;
  font-weight: bold;
  color: #e50914;
}
.payment-options {
  gap: 12px;
}
.payment-option-static {
  display: block;
  border: 2px solid #e50914;
  border-radius: 6px;
  padding: 24px;
  background-color: #333;
  box-shadow: 0 0 10px rgba(229, 9, 20, 0.5);
}
.payment-option-content {
  display: flex;
  align-items: center;
  gap: 12px;
}
.payment-icon {
  font-size: 24px;
  color: #e50914;
}
.notice-text {
  color: #ccc;
  line-height: 1.7;
  font-size: 15px;
  margin: 0;
}
.confirm-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  font-weight: bold;
  background-color: #e50914;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.confirm-btn:hover {
  background-color: #f40612;
}
.confirm-btn:disabled {
  background-color: #555;
  cursor: not-allowed;
}
.countdown-timer {
  text-align: center;
  font-size: 1.1rem;
  font-weight: bold;
  color: #e5e5e5;
  padding-top: 16px;
}
.countdown-timer .time-value {
  color: #e50914;
  font-size: 1.3rem;
  letter-spacing: 1px;
}
.countdown-timer .expired-text {
  color: #aaa;
}
</style>
