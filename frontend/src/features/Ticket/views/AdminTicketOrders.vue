<template>
  <div class="order-management">
    <OrderFilter v-model="searchParams" @search="handleSearch" @clear="clearSearch" />

    <OrderTable
      :orders="orders"
      :loading="isLoading"
      :total="totalOrders"
      :current-page="searchParams.page"
      :page-size="searchParams.pageSize"
      @view-details="viewDetails"
      @page-change="handlePageChange"
    />

    <OrderDetailDrawer
      v-model="drawerVisible"
      :order="selectedOrderDetails.data"
      :loading="selectedOrderDetails.isLoading"
      @update:order="handleOrderUpdate"
    />
  </div>
</template>

<script setup>
// 從 vue 這個核心套件中，引入我們需要的功能
import { ref, reactive, onMounted } from 'vue' // ref 用來處理單一值的響應式資料(像數字、字串、布林)，reactive 用來處理物件或陣列這種複雜結構的響應式資料，onMounted 是生命週期鉤子，會在元件被掛載到畫面上後執行。
// 從 pinia 這個狀態管理套件中，引入 storeToRefs
// storeToRefs 的作用是把 store 裡面的狀態 (state) 拿出來，變成一個個響應式的 ref，這樣我們在 template 裡使用時才不會失去響應性。
import { storeToRefs } from 'pinia'

// 引入我們自己寫的子元件
import OrderFilter from '../components/OrderFilter.vue' // 引入篩選器元件
import OrderTable from '../components/OrderTable.vue' // 引入表格元件
import OrderDetailDrawer from '../components/OrderDetailDrawer.vue' // 引入詳情抽屜元件

// 引入專門用來管理「後台訂單」狀態的 Pinia Store
import { useAdminTicketOrderStore } from '../store/useAdminTicketOrderStore'
// 引入一個處理日期格式的工具函式
import { formatDateString } from '@/utils/dateUtils.js'

// =================================================================
// ===== 狀態管理區：所有資料和狀態的集中地 =====
// =================================================================

// 1. 實例化 store
// 就像是建立一個與「訂單資料庫」溝通的專線電話，orderStore 就是這支電話的話筒。
const orderStore = useAdminTicketOrderStore()

// 2. 從 Store 解構出需要的狀態(state)和方法(action)
// 使用 storeToRefs 確保我們拿到的 orders, totalOrders 等狀態是響應式的。
// 這就像是把電話的幾個常用功能按鈕（例如訂單列表、總數、讀取狀態）接到你的桌上，方便直接取用，而且這些按鈕上的燈號會自動更新。
const { orders, totalOrders, isLoading, selectedOrderDetails } = storeToRefs(orderStore)
// 直接從 store 實例中解構出我們需要「執行」的動作（actions）。
// 這就像是拿到「撥號查詢訂單」和「撥號查詢單筆詳情」的快速撥號鍵。
const { fetchOrders, fetchOrderDetail } = orderStore

// 3. searchParams: 查詢參數的集合包
// 使用 reactive 創建一個響應式物件，專門存放要送給後端 API 的所有查詢條件。
// 把它想像成一張「訂單查詢申請表」，上面有各種欄位讓你填寫。
const searchParams = reactive({
  page: 1, // 目前要查詢的頁碼，預設為第 1 頁
  pageSize: 10, // 每一頁顯示幾筆資料
  orderNumber: '', // 訂單編號查詢欄位
  username: '', // 使用者名稱查詢欄位
  email: '', // 電子郵件查詢欄位
  phone: '', // 手機號碼查詢欄位
  status: '', // 訂單狀態查詢欄位
  paymentType: '', // 付款方式查詢欄位
  dateRange: null, // 日期範圍，這是一個陣列 [startDate, endDate]
})

// 4. drawerVisible: 詳情抽屜的開關
// 使用 ref 創建一個響應式變數來控制抽屜的顯示或隱藏。
// true 代表打開，false 代表關閉。
const drawerVisible = ref(false)

// =================================================================
// ===== 方法 (Methods) 區：定義所有會被觸發的行為 =====
// =================================================================

/**
 * 觸發 API 查詢的核心函式
 * 這是整個頁面的「大腦」，負責收集查詢申請表(searchParams)上的所有資訊，
 * 整理好格式後，按下快速撥號鍵（fetchOrders）通知後端去資料庫撈資料。
 */
const triggerFetchOrders = () => {
  // 準備一個乾淨的參數物件傳給後端。
  // 為了避免把不必要的欄位(例如整個 dateRange 物件)傳給後端，我們建立一個新的物件。
  const paramsToFetch = {
    page: searchParams.page,
    pageSize: searchParams.pageSize,
    orderNumber: searchParams.orderNumber,
    username: searchParams.username,
    email: searchParams.email,
    phone: searchParams.phone,
    status: searchParams.status,
    paymentType: searchParams.paymentType,
  }

  // 特別處理日期範圍：如果使用者有選擇日期，就把它拆成後端看得懂的 startDate 和 endDate。
  if (searchParams.dateRange && searchParams.dateRange.length === 2) {
    // 使用我們引入的工具函式來格式化日期
    paramsToFetch.startDate = formatDateString(searchParams.dateRange[0])
    paramsToFetch.endDate = formatDateString(searchParams.dateRange[1])
  }

  // 一切準備就緒後，呼叫 Store 的 action (fetchOrders)，並把整理好的參數包 (paramsToFetch) 傳過去。
  fetchOrders(paramsToFetch)
}

/**
 * 處理「搜尋」按鈕的點擊事件
 * 當使用者填好篩選條件，按下搜尋時，這個函式會被觸發。
 */
const handleSearch = () => {
  // 重要的步驟：每次進行新的搜尋時，都應該從第一頁開始看結果。
  searchParams.page = 1
  // 呼叫核心函式去跟後端要資料。
  triggerFetchOrders()
}

/**
 * 處理「清除」按鈕的點擊事件
 * 當使用者想要清空所有篩選條件，回到最原始的列表時，觸發此函式。
 */
const clearSearch = () => {
  // 將查詢申請表 (searchParams) 上的所有欄位還原成預設值。
  searchParams.page = 1
  searchParams.orderNumber = ''
  searchParams.username = ''
  searchParams.email = ''
  searchParams.phone = ''
  searchParams.status = ''
  searchParams.paymentType = ''
  searchParams.dateRange = null
  // 清空後，一樣呼叫核心函式，重新取得一次最完整、無篩選的資料。
  triggerFetchOrders()
}

/**
 * 處理分頁變更事件
 * 當使用者點擊表格下方的頁碼按鈕時觸發。
 * @param {number} newPage - 子元件(OrderTable)傳過來的使用者點擊的新頁碼。
 */
const handlePageChange = (newPage) => {
  // 更新我們申請表上的頁碼。
  searchParams.page = newPage
  // 根據新的頁碼，再次呼叫核心函式去取得那一頁的資料。
  triggerFetchOrders()
}

/**
 * 處理「查看詳情」的點擊事件
 * 當使用者在表格中點擊某一列的詳情按鈕時觸發。
 * @param {object} order - 子元件(OrderTable)傳過來的那一整筆訂單的資料。
 */
const viewDetails = (order) => {
  // 呼叫 store 中的 fetchOrderDetail 動作，並告知我們要查詢哪一筆訂單（使用唯一的訂單編號 orderNumber）。
  // store 會去跟後端要這筆訂單的完整詳細資料。
  fetchOrderDetail(order.orderNumber)
  // 將抽屜的開關設定為 true，讓它從旁邊滑出來。
  drawerVisible.value = true
}

/**
 * 處理訂單更新事件
 * 當抽屜元件 (OrderDetailDrawer) 內部發生了資料更新 (例如管理員修改了訂單狀態) 時，
 * 子元件會發出信號，觸發此函式。
 */
const handleOrderUpdate = () => {
  // 為了確保主列表上的資料也是最新的，我們只需要重新呼叫一次核心查詢函式即可。
  // 這會讓頁面上的數據與資料庫中的數據保持同步。
  triggerFetchOrders()
}

// =================================================================
// ===== 生命週期鉤子 (Lifecycle Hooks) 區 =====
// =================================================================

/**
 * onMounted:
 * 這是 Vue 元件生命週期的一個階段，代表「當這個元件第一次被成功渲染到畫面上之後」。
 * 我們通常會在這裡做一些初始化的事情，例如第一次呼叫 API 取得資料。
 */
onMounted(() => {
  // 呼叫核心查詢函式，讓使用者一進到這個頁面，就能立刻看到第一頁的訂單列表。
  triggerFetchOrders()
})
</script>

<style scoped>
/* `scoped` 屬性是一個 Vue 的特色，它會確保這個 <style> 標籤裡的 CSS 樣式只會作用於當前這個元件(.vue 檔案)中的 HTML 元素。
   這就像是給這個元件的樣式加上一個「結界」，不會意外地影響到其他元件的樣式，避免了 CSS 全域污染的問題。 */

/* 針對 .order-management 這個 class 的樣式設定 */
.order-management {
  padding: 20px; /* 在容器的四周加上 20px 的內邊距，讓內容不要貼著邊緣 */
  background-color: #f0f2f5; /* 設定一個淺灰色的背景，提升質感 */
  min-height: 100vh; /* 設定最小高度為 100% 的視窗高度(vh)，確保即使內容很少，背景色也能填滿整個畫面 */
}
</style>
