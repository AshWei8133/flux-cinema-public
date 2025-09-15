<template>
  <el-menu
    class="admin-sidebar"
    background-color="#545c64"
    text-color="#fff"
    active-text-color="#ffd04b"
    :router="true"
    :default-active="currentPath"
    :collapse="props.isCollapse"
  >
    <div class="sidebar-header">
      <span v-show="!isCollapse">後台管理系統</span>
      <el-button @click="emit('toggle-collapse')" :icon="Menu" circle class="collapse-btn" />
    </div>

    <!-- <el-menu-item index="/admin/dashboard">
      <el-icon><HomeFilled /></el-icon>
      <span>首頁</span>
    </el-menu-item> -->

    <el-sub-menu index="member">
      <template #title>
        <el-icon><User /></el-icon>
        <span>會員管理</span>
      </template>
      <el-menu-item index="/admin/members">會員維護</el-menu-item>
      <el-menu-item index="/admin/memberlevels">會員等級維護</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="/admin/movies">
      <template #title>
        <el-icon><Film /></el-icon>
        <span>電影管理</span>
      </template>
      <el-menu-item index="/admin/movies">電影維護</el-menu-item>
      <el-menu-item index="/admin/movies/TMDBMovies/new">TMDB維護</el-menu-item>
      <!-- <el-menu-item index="/admin/movies/TMDBMovies">TMDB維護</el-menu-item> -->
      <el-menu-item index="/admin/movies/Directors">導演維護</el-menu-item>
      <el-menu-item index="/admin/movies/Actors">演員維護</el-menu-item>
      <el-menu-item index="/admin/movies/Genre">類型維護</el-menu-item>
      <!-- <el-menu-item index="/admin/movies/ShowMoviePlaying">前台測試</el-menu-item> -->
    </el-sub-menu>

    <el-sub-menu index="theater">
      <template #title>
        <el-icon><VideoCameraFilled /></el-icon>
        <span>影廳管理</span>
      </template>

      <el-menu-item index="/admin/theaters">影廳維護</el-menu-item>
      <el-menu-item index="/admin/theaterTypes">影廳類型管理</el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/admin/movieSessions">
      <el-icon><Timer /></el-icon>
      <span>場次管理</span>
    </el-menu-item>

    <el-sub-menu index="ticket">
      <template #title>
        <el-icon><Tickets /></el-icon>
        <span>票券管理</span>
      </template>

      <el-menu-item index="/admin/tickets">票種管理</el-menu-item>
      <el-menu-item index="/admin/tickets/priceRule">票價規則管理</el-menu-item>
      <el-menu-item index="/admin/tickets/order">票價訂單管理</el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/admin/announcements">
      <el-icon><Calendar /></el-icon>
      <span>公告管理</span>
    </el-menu-item>

    <el-sub-menu index="/admin/events">
      <template #title>
        <el-icon><Place /></el-icon>
        <span>活動管理</span>
      </template>

      <el-menu-item index="/admin/events">活動列表</el-menu-item>
      <el-menu-item index="/admin/eventCategories">活動類型</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="coupon">
      <template #title>
        <el-icon><Ticket /></el-icon>
        <span>優惠券管理</span>
      </template>

      <el-menu-item index="/admin/coupons/design">優惠券設計</el-menu-item>
      <el-menu-item index="/admin/coupons">優惠券列表</el-menu-item>
      <el-menu-item index="/admin/coupon-categories">優惠券類型</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="products">
      <template #title>
        <el-icon><Goods /></el-icon>
        <span>產品管理</span>
      </template>

      <el-menu-item index="/admin/products">產品列表</el-menu-item>
      <el-menu-item index="/admin/products/restockList">產品補貨</el-menu-item>
      <el-menu-item index="/admin/products/removeList">已下架產品</el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/admin/orders">
      <el-icon><Wallet /></el-icon>
      <span>訂單管理</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Film,
  HomeFilled,
  User,
  Timer,
  Menu,
  Ticket,
  Calendar,
  Place,
  Goods,
  VideoCameraFilled,
  Wallet,
  Tickets,
} from '@element-plus/icons-vue' // 引入 Element Plus 圖標

// 數據
const route = useRoute()
const props = defineProps(['isCollapse'])
const emit = defineEmits(['toggle-collapse'])

// computed
// 根據當前路由路徑動態設置 active 菜單項
const currentPath = computed(() => route.path)
</script>

<style scoped>
.admin-sidebar {
  height: 100vh;
  overflow-y: auto;
}

.sidebar-header {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  height: 60px; /* 設置固定高度，避免收合時高度變化 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px; /* 調整內邊距，讓內容不會太貼邊 */
  border-bottom: 1px solid #4a515a;
}

/* 展開狀態下的樣式 */
.admin-sidebar:not(.el-menu--collapse) .sidebar-header {
  justify-content: space-between;
}

/* 針對收合狀態下的樣式 */
.admin-sidebar.el-menu--collapse .sidebar-header {
  justify-content: center; /* 讓按鈕在收合時置中 */
}

/* 調整按鈕的樣式 */
/* .collapse-btn { */
/* 按鈕樣式可以在這裡進一步調整，例如大小、顏色等 */
/* } */
</style>
