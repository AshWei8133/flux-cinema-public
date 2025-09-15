<template>
  <div class="product-detail">
    <div v-if="isLoading" class="loading-message">
      <p>載入中...</p>
    </div>

    <div v-else-if="error" class="error-message">
      <p>無法載入產品資料: {{ error }}</p>
    </div>

    <div v-else-if="product">
      <h2>{{ product.productName }}</h2>
      <p><strong>價格:</strong> NT$ {{ product.price }}</p>
      <p><strong>庫存:</strong> {{ product.stock }} 件</p>
      <p><strong>商品描述:</strong></p>
      <p>{{ product.description }}</p>

      <img v-if="product.imageUrl" :src="product.imageUrl" alt="Product Image" class="product-image" />
      
      <p><strong>上架狀態:</strong> {{ product.isAvailable ? '已上架' : '未上架' }}</p>
      <p><strong>建立時間:</strong> {{ formattedCreationTime }}</p>

    </div>
    
    <button @click="$emit('close')">關閉</button>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useProductStore } from '@/stores/useProductStore.js' // 確保路徑正確

const props = defineProps({
  productId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['close'])
const store = useProductStore()

// 為了方便顯示，使用本地響應式變數
const product = ref(null)
const isLoading = ref(false)
const error = ref(null)

// 格式化建立時間，讓顯示更友善
const formattedCreationTime = computed(() => {
  if (product.value && product.value.creationTime) {
    const date = new Date(product.value.creationTime)
    return date.toLocaleString()
  }
  return '無'
})

// 監聽 productId 的變化，並觸發資料載入
watch(
  () => props.productId,
  async (newId) => {
    if (newId != null) {
      isLoading.value = true
      error.value = null
      try {
        await store.fetchProductById(newId)
        product.value = store.selectedProduct
      } catch (err) {
        error.value = '載入產品失敗'
        console.error('Failed to fetch product:', err)
      } finally {
        isLoading.value = false
      }
    }
  },
  { immediate: true } // 立即執行一次，即使一開始就有值
)
</script>

<style scoped>
.product-detail {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  max-width: 600px;
  margin: auto;
}
.product-image {
  max-width: 100%;
  height: auto;
  margin-top: 15px;
  border-radius: 4px;
}
.loading-message, .error-message {
  text-align: center;
  font-weight: bold;
}
</style>