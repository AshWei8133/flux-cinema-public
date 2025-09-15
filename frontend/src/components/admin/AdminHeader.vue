<template>
  <el-header class="admin-header">
    <div class="header-left">
      <h3 v-if="currentRouteTitle">{{ currentRouteTitle }}</h3>
    </div>
    <div class="header-right">
      <el-button type="info" @click="handleLogout" class="logout-btn">登出</el-button>
    </div>
  </el-header>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const authStore = useAuthStore()
const route = useRoute()

const currentRouteTitle = computed(() => route.meta.title)

const handleLogout = async () => {
  await authStore.logout()
  console.log('執行登出操作...')
}
</script>

<style scoped>
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background-color: #545c64;
  color: #fff;
  padding: 0 15px;
  border-bottom: 1px solid #4a515a;
}
.header-left {
  display: flex;
  align-items: center;
}
.app-name {
  margin-left: 10px;
  font-size: 18px;
  font-weight: bold;
}
.header-right .logout-btn {
  background-color: transparent;
  border: 1px solid #fff;
  color: #fff;
}
.header-right .logout-btn:hover {
  background-color: #606770;
  border-color: #fff;
}
.collapse-btn {
  color: #fff;
  border: 1px solid #fff;
  background-color: transparent;
}
.collapse-btn:hover {
  background-color: #606770;
  border-color: #fff;
}
</style>
