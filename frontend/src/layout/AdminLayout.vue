<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '200px'">
      <AdminSidebar :is-collapse="isCollapse" @toggle-collapse="isCollapse = !isCollapse" />
    </el-aside>
    <el-container class="right-container">
      <AdminHeader />
      <el-main>
        <RouterView></RouterView>
      </el-main>
      <AdminFooter />
    </el-container>
  </el-container>
</template>

<script setup>
import AdminFooter from '@/components/admin/AdminFooter.vue'
import AdminHeader from '@/components/admin/AdminHeader.vue'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import { ref } from 'vue'
import { RouterView } from 'vue-router'

const isCollapse = ref(false)
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.right-container {
  display: flex;
  flex-direction: column;
}
.el-main {
  padding: 0;
}
</style>
