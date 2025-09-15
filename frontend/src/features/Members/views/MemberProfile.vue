<template>
  <div class="profile-page-wrapper">
    <div class="clapperboard-container">
      <div class="clapper-top">
        <div class="clapper-stripe"></div>
        <div class="clapper-stripe"></div>
        <div class="clapper-stripe"></div>
        <div class="clapper-stripe"></div>
        <div class="clapper-stripe"></div>
        <div class="clapper-stripe"></div>
      </div>
      <div class="clapper-body">
        <div v-if="isLoading && !profile" class="loading-spinner">
          <p>載入中...</p>
        </div>
        <div v-else-if="error && !profile" class="error-message">
          <p>{{ error }}</p>
        </div>

        <div v-else-if="profile" class="profile-layout">
          <!-- 左側區塊 -->
          <div class="left-pane">
            <div class="avatar-section" @click="triggerFileInput">
              <img :src="avatarUrl" alt="Member Avatar" class="avatar-image" />
              <div class="camera-label">CAM</div>
              <input
                type="file"
                ref="fileInput"
                @change="onFileSelected"
                accept="image/*"
                style="display: none"
              />
            </div>

            <div class="actions-menu">
              <button @click="activeView = 'view'" :class="{ active: activeView === 'view' }">
                個人資訊
              </button>

              <button @click="activeView = 'orders'" :class="{ active: activeView === 'orders' }">
                電影收藏
              </button>

              <div class="dropdown-container">
                <button
                  @click="isOrdersDropdownOpen = !isOrdersDropdownOpen"
                  class="dropdown-toggle"
                >
                  <span>訂單紀錄</span>
                  <span class="arrow" :class="{ 'arrow-up': isOrdersDropdownOpen }"></span>
                </button>
                <div v-if="isOrdersDropdownOpen" class="dropdown-menu">
                  <button @click="((activeView = 'ticketOrders'), (isOrdersDropdownOpen = false))">
                    訂票紀錄
                  </button>
                  <button @click="((activeView = 'productOrders'), (isOrdersDropdownOpen = false))">
                    商品購買紀錄
                  </button>
                </div>
              </div>

              <button @click="activeView = 'level'" :class="{ active: activeView === 'level' }">
                會員等級
              </button>

              <button @click="activeView = 'coupon'" :class="{ active: activeView === 'coupon' }">
                我的優惠券
              </button>

              <button @click="goToChangePassword">修改密碼</button>
              <!-- 可在此加入其他功能，例如：查看訂單歷史 -->
            </div>
          </div>

          <!-- 右側區塊 -->
          <div class="right-pane">
            <div class="content-header">
              <h1>{{ viewTitle }}</h1>
              <button
                v-if="activeView === 'view'"
                @click="activeView = 'edit'"
                class="edit-profile-button"
              >
                編輯資料
              </button>
            </div>

            <component
              :is="currentComponent"
              :profile="profile"
              :is-updating="isUpdating"
              @save-changes="handleSaveChanges"
              @cancel-edit="cancelEdit"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed, ref, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { usePublicMemberProfileStore } from '../store/usePublicMemberProfileStore'
import defaultAvatar from '@/assets/images/User.jpg'
import { ElMessage } from 'element-plus'

import ViewProfile from '../components/profile/ViewProfile.vue'
import EditProfile from '../components/profile/EditProfile.vue'
import MovieCollection from '../components/profile/MovieCollection.vue'
import MemberLevelView from '../components/profile/MemberLevelView.vue'
import TicketOrdersView from '../components/profile/TicketOrdersView.vue'
import ProductOrdersView from '../components/profile/ProductOrdersView.vue'
import CouponView from '../components/profile/CouponView.vue'
import MemberCouponList from '@/features/Event/views/front/MemberCouponList.vue'

const route = useRoute()

const router = useRouter()

const profileStore = usePublicMemberProfileStore()
const { profile, isLoading, error, isUploading, isUpdating } = storeToRefs(profileStore)
const {
  fetchProfile,
  updateAvatar,
  updateProfile,
  fetchTicketOrderHistory,
  fetchProductOrderHistory,
} = profileStore

// 控制右側面板顯示哪個視圖
const activeView = ref('view') // 'view', 'edit', or 'orders'
const isOrdersDropdownOpen = ref(false)

const fileInput = ref(null)
const selectedAvatarPreview = ref(null)

const viewMap = {
  view: ViewProfile,
  edit: EditProfile,
  orders: MovieCollection,
  level: MemberLevelView,
  ticketOrders: TicketOrdersView,
  productOrders: ProductOrdersView,
  coupon: MemberCouponList,
}

const currentComponent = computed(() => {
  return viewMap[activeView.value] || ViewProfile
})

const viewTitle = computed(() => {
  const titles = {
    view: '個人資料',
    edit: '編輯個人資料',
    orders: '電影收藏',
    level: '會員等級',
    ticketOrders: '訂票紀錄',
    productOrders: '商品購買紀錄',
    coupon: '我的優惠券',
  }
  return titles[activeView.value] || '會員中心'
})

const triggerFileInput = () => {
  fileInput.value.click()
}

const onFileSelected = async (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedAvatarPreview.value = URL.createObjectURL(file)
    try {
      await updateAvatar(file)
      ElMessage.success('頭像更新成功！')
      selectedAvatarPreview.value = null
    } catch (err) {
      ElMessage.error(error.value || '頭像上傳失敗')
      selectedAvatarPreview.value = null
    }
  }
}

const handleSaveChanges = async (updatedData) => {
  try {
    await updateProfile(updatedData)
    ElMessage.success('個人資料更新成功')
    activeView.value = 'view'
  } catch {
    ElMessage.error(error.value || '個人資料更新失敗')
  }
}

const cancelEdit = () => {
  activeView.value = 'view'
}

const goToChangePassword = () => {
  router.push({ name: 'MemberChangePassword' })
}

watch(activeView, (newView) => {
  if (newView === 'ticketOrders') {
    fetchTicketOrderHistory()
  } else if (newView === 'productOrders') {
    // 【新增】當切換到商品訂單視圖時，觸發對應的 action
    profileStore.fetchProductOrderHistory()
  }
})

const avatarUrl = computed(() => {
  return (
    selectedAvatarPreview.value ||
    (profile.value?.profilePhoto
      ? `data:image/jpeg;base64,${profile.value.profilePhoto}`
      : defaultAvatar) ||
    defaultAvatar
  )
})

onMounted(() => {
  fetchProfile()

  // 在組件掛載後，檢查路由的 query 參數
  const viewFromQuery = route.query.view

  // 檢查 viewFromQuery 是否存在，並且是 viewMap 中定義的有效 key
  if (viewFromQuery && Object.keys(viewMap).includes(viewFromQuery)) {
    // 如果有效，就將 activeView 設置為 query 傳來的值
    activeView.value = viewFromQuery

    // 如果跳轉的是下拉選單中的項目，自動展開下拉選單
    if (viewFromQuery === 'ticketOrders' || viewFromQuery === 'productOrders') {
      isOrdersDropdownOpen.value = true
    }
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@700&family=Share+Tech+Mono&display=swap');

.profile-page-wrapper {
  background-color: #121212;
  padding: 3rem 1rem;
  min-height: 100vh;
  box-sizing: border-box;
}

.clapperboard-container {
  width: 100%;
  margin: 0;
  background-color: #1a1a1a;
  border: 3px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.8);
  font-family: 'Share Tech Mono', monospace;
  color: #fff;
}

.clapper-top {
  display: flex;
  transform: rotate(-2deg);
  margin-left: -10px;
  margin-top: -5px;
  margin-bottom: 10px;
  z-index: 0;
}

.clapper-stripe {
  padding: 15px 25px;
  background: repeating-linear-gradient(-45deg, #000, #000 20px, #fff 20px, #fff 40px);
}

.clapper-body {
  padding: 1.5rem 2rem;
  position: relative;
  z-index: 1;
}

.loading-spinner,
.error-message {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
  text-transform: uppercase;
}

.profile-layout {
  display: flex;
  gap: 2rem;
}

.left-pane {
  flex: 0 0 200px; /* Fixed width for the left pane */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.avatar-section {
  width: 180px;
  height: 180px;
  border: 1px solid #555;
  padding: 1rem;
  position: relative;
  transition:
    transform 0.2s ease-out,
    box-shadow 0.2s ease-out;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-section:hover {
  transform: scale(1.03);
  box-shadow: 0 0 25px rgba(255, 255, 255, 0.1);
  border-color: #888;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: grayscale(100%);
  border-radius: 4px;
  transition: filter 0.3s ease-in-out;
}

.avatar-section:hover .avatar-image {
  filter: grayscale(0%);
}

.camera-label {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background-color: #fff;
  color: #000;
  padding: 5px 10px;
  font-size: 1rem;
  border-radius: 2px;
}

.save-avatar-button {
  width: 100%;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.actions-menu {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.actions-menu button {
  width: 100%;
  border: 1px solid #555;
  background-color: transparent;
  color: #ccc;
  padding: 12px;
  text-align: left;
  font-size: 1rem;
  cursor: pointer;
  transition:
    background-color 0.2s,
    color 0.2s;
  border-radius: 4px;
}

.actions-menu button:hover,
.actions-menu button.active {
  background-color: #4a90e2;
  color: white;
  border-color: #4a90e2;
}

.right-pane {
  flex: 1;
  border-left: 2px solid #ccc;
  padding-left: 2rem;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #555;
}

.edit-profile-button {
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 0.9rem;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.edit-profile-button:hover {
  background-color: #357abd;
}

.content-header h1 {
  font-family: 'Inter', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin: 0;
  padding: 0;
  border: none;
}

/* Dropdown Styles */
.dropdown-container {
  position: relative;
  width: 100%;
}

.dropdown-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.arrow {
  border: solid #ccc;
  border-width: 0 2px 2px 0;
  display: inline-block;
  padding: 3px;
  transform: rotate(45deg);
  transition: transform 0.2s;
}

.arrow-up {
  transform: rotate(-135deg);
}

.dropdown-menu {
  position: relative; /* Changed from absolute for in-flow display */
  width: 100%;
  z-index: 10;
  background-color: #2c2c2c; /* Slightly different background */
  border-radius: 4px;
  margin-top: 0.25rem;
  overflow: hidden;
  border-left: 2px solid #4a90e2; /* Accent line */
}

.dropdown-menu button {
  background-color: transparent; /* Inherit from parent */
  text-align: left;
  padding-left: 2rem; /* Indent sub-items */
  border: none; /* Remove individual borders */
  color: #bbb; /* Lighter text for sub-items */
}

.dropdown-menu button:hover {
  background-color: #4a90e2; /* Keep hover effect */
  color: white;
}
</style>
