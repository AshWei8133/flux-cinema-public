<template>
  <header class="app-header">
    <div class="header-content">
      <div class="logo-container">
        <router-link to="/">
          <img src="@/assets/images/fluxLogo4.png" alt="Flux Cinema Logo" class="logo-img" />
        </router-link>
      </div>

      <div class="navigation-container">
        <div class="member-area">
          <template v-if="!isLoggedIn">
            <router-link to="/login" class="member-link">登入</router-link>
            <span>/</span>
            <router-link to="/register" class="member-link">註冊</router-link>
          </template>
          <template v-else>
            <span>歡迎，{{ username }}</span>
            <router-link to="/member/profile" class="member-link">會員中心</router-link>
            <a href="#" @click.prevent="handleLogout" class="member-link">登出</a>
          </template>
        </div>

        <nav class="main-nav">
          <!-- 
            【已修改】
            我們將原本的 <router-link> 換成了 Element Plus 的 <el-dropdown> 元件
          -->
          <el-dropdown trigger="hover">
            <!-- 
              這個 span 是使用者在畫面上看到的「找電影」按鈕。
              我們給它加上 'nav-link' class 讓它的樣式和旁邊的連結一樣。
            -->
            <span class="nav-link"> 找電影 </span>
            <!-- 
              #dropdown 插槽裡的內容，就是點擊後會彈出的選單
            -->
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <!-- 每個選項裡面，我們再放回 router-link 來處理頁面跳轉 -->
                  <router-link to="/movie/nowPlaying" class="dropdown-link">現正熱映</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/movie/comingSoon" class="dropdown-link">即將上映</router-link>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <a href="#" @click.prevent="scrollToBooking" class="nav-link">快速訂票</a>

          <el-dropdown trigger="hover">
            <!-- 
              這個 span 是使用者在畫面上看到的「找電影」按鈕。
              我們給它加上 'nav-link' class 讓它的樣式和旁邊的連結一樣。
            -->
            <span class="nav-link"> 活動專區 </span>
            <!-- 
              #dropdown 插槽裡的內容，就是點擊後會彈出的選單
            -->
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <router-link to="/events" class="dropdown-link" @click="closeDropdown">
                    影城活動
                  </router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/coupons" class="dropdown-link" @click="closeDropdown">
                    影城優惠
                  </router-link>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <router-link to="/announcements" class="nav-link">最新消息</router-link>
          <router-link to="/products" class="nav-link">餐飲與商品</router-link>
        </nav>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useMemberAuthStore } from '@/stores/memberAuth'
import { useRoute, useRouter } from 'vue-router'
import { useUiStore } from '@/stores/uiStore'
import { ref } from 'vue'

const dropdownRef = ref(null)

function closeDropdown() {
  dropdownRef.value?.handleClose()
}

// 2. 獲取 Pinia store 的實例
const memberAuthStore = useMemberAuthStore()

// 3. 建立 computed 屬性來響應 store 的變化
//    使用 computed 可以確保當 store 的狀態改變時，這裡的值也會自動更新，從而觸發畫面重新渲染。
const isLoggedIn = computed(() => memberAuthStore.isAuthenticated)

//    使用 computed 並搭配可選串聯 (?.) 可以安全地獲取 memberInfo 中的 username。
//    即使 memberInfo 在登出後變為 null，程式碼也不會報錯。
const username = computed(() => memberAuthStore.memberInfo?.username || '會員')

// 4. 建立登出處理函式
const handleLogout = () => {
  // 直接呼叫 store 中定義好的 logout action
  memberAuthStore.logout()
}

// 獲取 router 和 uiStore 的實例
const router = useRouter()
const route = useRoute()
const uiStore = useUiStore()

// 處理書籤滾動行為
async function scrollToBooking() {
  if (route.name !== 'Home') {
    // 跳轉首頁並攜帶狀態
    await router.push({ name: 'Home' })
  }

  // 無論之前是否在首頁，都透過 uiStore 下達滾動指令
  uiStore.setScrollTarget('#booking-section')
}
</script>

<style scoped>
.app-header {
  background-color: #141414; /* 半透明的深色背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果，增加質感 */
  padding-top: 15px;
  /* 讓導航列固定在頂部 */
  /* position: sticky;  */
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid #333;
}

.header-content {
  display: flex;
  /* 讓左右兩邊的內容分開 */
  justify-content: space-between;
  align-items: center;
  max-width: 95%;
  margin: 0 auto;
}

.logo-container .logo-img {
  width: 180px;
  height: 120px;
  display: block; /* 避免圖片下方有奇怪的空隙 */
}

.navigation-container {
  display: flex;
  flex-direction: column; /* 讓會員區和導航連結垂直排列 */
  align-items: flex-end; /* 讓內容靠右對齊 */
}

.member-area {
  display: flex;
  align-items: center;
  gap: 15px; /* 項目之間的間距 */
  font-size: 18px;
  margin-bottom: 10px; /* 與下方導航的間距 */
  color: #aaa;
}

.member-area .member-link {
  color: #ccc;
  text-decoration: none;
  transition: color 0.3s;
}

.member-area .member-link:hover {
  color: #e50914; /* 經典的影音網站紅色 */
}

.main-nav {
  display: flex;
  gap: 30px;
  /* 【關鍵修正】
    align-items: center 會讓 flex 容器中的所有項目 (包含 el-dropdown 和 router-link)
    在垂直方向上對齊到中心點，解決文字高低不一的問題。
  */
  align-items: baseline;
}

.nav-link {
  color: #ffffff;
  text-decoration: none;
  font-size: 20px;
  font-weight: bold;
  padding: 5px 0;
  position: relative;
  transition: color 0.3s;
  cursor: pointer;
}

/* 做一個滑鼠移上去時，下方出現底線的動畫效果 */
.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #e50914;
  transition: width 0.3s ease-in-out;
}

.nav-link:hover {
  color: #e50914;
}

.nav-link:hover::after {
  width: 100%;
}
/* 【新增】讓下拉選單中的連結樣式更美觀 */
.dropdown-link {
  /* text-decoration: none;
  display: block;
  width: 100%;
  padding: 5px 10px; */

  /* 【已修改】設定背景色和文字顏色 */
  /* background-color: #141414; 使用您網站的深灰色，比純黑更有質感 */
  /* color: #e50914; 紅色文字 */

  /* 【新增】加上一個平滑的顏色過渡動畫 */
  /* transition: background-color 0.3s ease; */
}
</style>
<style>
/* 白話解釋：
  因為 Element Plus 的下拉式選單，在畫面上是獨立於您元件之外的，
  所以我們需要一個沒有 "scoped" 的全域 style 區塊，才能設定到它的樣式。
*/

/* 1. 針對「整個下拉式的框框」(.el-dropdown-menu) */
.el-dropdown-menu {
  background-color: #141414 !important; /* 設定為您網站的深灰色背景 */
  border: 1px solid #333 !important; /* 加上一個深色的邊框，增加質感 */
  border-radius: 4px; /* 加上一點圓角 */
}

/* 2. 針對「框框裡的每一個選項」(.el-dropdown-menu__item) */
.el-dropdown-menu__item {
  /* 預設的文字顏色，我們可以用一個比較柔和的白色 */
  color: #e5e7eb !important;
}

/* 3. 當滑鼠移到選項上時的樣式 */
.el-dropdown-menu__item:hover {
  /* 背景顏色稍微變亮，讓使用者知道現在選的是哪一個 */
  background-color: #333 !important;
  /* 文字顏色變成您網站的紅色，增加互動感 */
  color: #e50914 !important;
}

/* 4. 確保選項裡的連結樣式正確 */
.el-dropdown-menu__item .dropdown-link {
  /* 讓 router-link 的顏色，完全繼承 el-dropdown-menu__item 的顏色 */
  color: inherit;
  text-decoration: none;
  display: block;
  width: 100%;
}
</style>
