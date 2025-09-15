// 引入 Vue Router 的核心函式
import { createRouter, createWebHistory } from 'vue-router'

// 引入您拆分出來的路由模組 (這些檔案現在都是 .js 格式)
import adminRoutes from './adminRoutes'
import fluxAppRoutes from './fluxAppRoutes'
import { useAuthStore } from '@/stores/auth'
import { useMemberAuthStore } from '@/stores/memberAuth'
import { usePublicSeatSelectionStore } from '@/features/MovieSessions/store/usePublicSeatSelectionStore'

// 增加一個變數來確保初始化只執行一次
let isInitialized = false

// 將所有路由模組合併成一個陣列
// 通常將較為具體或優先匹配的路由放在前面。
const routes = [
  ...fluxAppRoutes, // 首先處理公開路由 (包括可能有的首頁和 404)
  ...adminRoutes, // 然後處理管理員相關的路由
]

// 創建路由器實例
const router = createRouter({
  history: createWebHistory('/flux/'), // 使用 HTML5 History 模式
  routes, // 使用組合後的路由陣列來創建路由器實例
})

// 全域導航守衛
router.beforeEach(async (to, from, next) => {
  // 在守衛函式內部獲取 store 實例
  // 注意：不能在檔案頂層直接呼叫 useStore()，必須在函式內部
  const authStore = useAuthStore()
  const memberAuthStore = useMemberAuthStore()

  // 實例化訂票流程的 store
  const seatSelectionStore = usePublicSeatSelectionStore()

  // 從 store 中獲取臨時訂單 ID，這是判斷是否已在後端建立預訂的關鍵
  const temporaryOrderId = seatSelectionStore.temporaryOrderId

  // 獲取路由的步驟元數據 (您需要在 fluxAppRoutes.js 的訂票流程子路由中定義 meta: { step: ... })
  const fromStep = from.meta.step || 0
  const toStep = to.meta.step || 0

  // 自動清除訂票流程暫存
  // 判斷是否從訂票流程頁面離開
  const isFromBookingFlow = from.path.startsWith('/booking/')
  // 判斷是否要前往訂票流程頁面
  const isToBookingFlow = to.path.startsWith('/booking/')
  const isGoingBackwards = isFromBookingFlow && isToBookingFlow && fromStep > toStep
  const isLeavingFlow = isFromBookingFlow && !isToBookingFlow

  if (temporaryOrderId) {
    if (isGoingBackwards) {
      const userConfirmation = window.confirm(
        '您確定要放棄當前的選位嗎？\n\n系統將會釋放您已選的座位。'
      )
      if (userConfirmation) {
        try {
          // 往回走，明確傳入 false，只清除座位和臨時訂單ID，保留票數
          await seatSelectionStore.cancelTemporaryOrder(false)
          console.log('臨時訂單已取消，已選票種已保留。')
        } catch (error) {
          console.error('在導航守衛中取消訂單時發生錯誤:', error)
        }
        next()
      } else {
        next(false)
        return
      }
    } else if (isLeavingFlow) {
      const userConfirmation = window.confirm(
        '您確定要離開訂票流程嗎？\n\n您的訂單將會被取消，所有已選資訊都會被清空。'
      )
      if (userConfirmation) {
        try {
          // 【修正】完全離開，明確傳入 true，清空所有狀態
          await seatSelectionStore.cancelTemporaryOrder(true)
          console.log('訂單已成功取消並清空所有狀態。')
        } catch (error) {
          console.error('在導航守衛中取消訂單時發生錯誤:', error)
        }
        next()
      } else {
        next(false)
        return
      }
    }
  }


  const isAdminLoggedIn = authStore.isAuthenticated
  const isMemberLoggedIn = memberAuthStore.isAuthenticated

  const requiresAuth = to.meta.requiresAuth
  const requiredRole = to.meta.role

  // 1. 【關鍵】如果應用程式尚未初始化，則執行一次初始化
  // if (!isInitialized) {
  //   authStore.initializeAuth()
  //   memberAuthStore.initializeAuth()
  //   isInitialized = true // 標記為已初始化
  // }

  // --- 規則 1：需要管理員權限的頁面 ---
  if (requiresAuth && requiredRole === 'admin') {
    if (isAdminLoggedIn) {
      next() // 管理員已登入，放行
    } else {
      next('/admin/login') // 未登入，導向到管理員登入頁
    }
  }
  // --- 規則 2：需要會員權限的頁面 ---
  else if (requiresAuth && requiredRole === 'member') {
    if (isMemberLoggedIn) {
      next() // 會員已登入，放行
    } else {
      next('/login') // 未登入，導向到會員登入頁
    }
  }
  // --- 規則 3：其他所有公開頁面 ---
  else {
    next() // 不需要權限，直接放行
  }
})

// 導出路由器實例
export default router
