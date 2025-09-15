<!-- OrderSuccessPage.vue -->
<template>
  <div class="order-success-page">
    <div class="success-container">
      <!-- 成功圖示 -->
      <div class="success-icon">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="#67c23a" stroke-width="2" />
          <path
            d="M8 12l2 2 4-4"
            stroke="#67c23a"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </div>

      <!-- 成功訊息 -->
      <h1 class="success-title">訂單建立成功！</h1>

      <!-- 訂單資訊 -->
      <div class="order-info">
        <p class="order-number">訂單編號：#{{ orderId }}</p>
        <p class="order-message">感謝您的購買，我們將盡快為您處理訂單。</p>
      </div>

      <!-- 操作按鈕 -->
      <div class="action-buttons">
        <button @click="viewOrder" class="view-order-btn">查看訂單</button>
        <button @click="continueShopping" class="continue-btn">繼續購物</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 從路由參數取得訂單編號
const orderId = ref('')

onMounted(() => {
  orderId.value = route.params.orderId || ''

  if (!orderId.value) {
    ElMessage.warning('未找到訂單編號')
    router.push({ name: 'Products' })
  }
})

// 查看訂單
const viewOrder = () => {
  router.push({ name: 'MemberProfile', query: { view: 'productOrders' } })
}

// 繼續購物
const continueShopping = () => {
  router.push({ name: 'Products' })
}
</script>

<style scoped>
.order-success-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.success-container {
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.success-icon {
  margin-bottom: 20px;
  animation: checkmark 0.5s ease-in-out;
}

@keyframes checkmark {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-title {
  color: #67c23a;
  font-size: 28px;
  margin-bottom: 20px;
  font-weight: bold;
}

.order-info {
  margin-bottom: 30px;
}

.order-number {
  font-size: 20px;
  color: #303133;
  margin-bottom: 10px;
  font-weight: 500;
}

.order-message {
  color: #606266;
  font-size: 16px;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.view-order-btn,
.continue-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.view-order-btn {
  background: white;
  color: #409eff;
  border: 2px solid #409eff;
}

.view-order-btn:hover {
  background: #409eff;
  color: white;
}

.continue-btn {
  background: #67c23a;
  color: white;
}

.continue-btn:hover {
  background: #85ce61;
}

/* 響應式設計 */
@media (max-width: 480px) {
  .success-container {
    padding: 30px 20px;
  }

  .success-title {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .view-order-btn,
  .continue-btn {
    width: 100%;
  }
}
</style>
