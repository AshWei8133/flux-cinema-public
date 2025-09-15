<template>
  <div class="page-container">
    <h1 class="page-title">會員資訊</h1>
  </div>

  <div class="p-6">
    <MemberDetail
      v-if="selectedMemberId"
      :memberId="selectedMemberId"
      @close="closeView"
    ></MemberDetail>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import MemberDetail from '../components/MemberDetail.vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 從路由參數中獲取會員 ID
const selectedMemberId = computed(() => route.params.id)

// 關閉詳情視圖，返回列表頁面
const closeView = () => {
  router.push('/admin/members')
}
</script>

<style scoped>
.page-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1.5rem 0;
  background: linear-gradient(135deg, #f0f4f8, #ffffff);
  border-bottom: 2px solid #e5e7eb;
}

.page-title {
  font-size: 1.75rem;
  font-weight: bold;
  color: #1f2937;
  letter-spacing: 1px;
}

.p-6 {
  max-width: 900px;
  margin: 2rem auto;
  padding: 2rem;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease;
}

.p-6:hover {
  transform: translateY(-3px);
}
</style>
