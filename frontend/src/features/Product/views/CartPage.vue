<template>
  <div class="cart-page">
    <h1 class="page-title">我的購物車</h1>

    <div v-if="isLoading" class="state-message">
      <p>購物車商品載入中...</p>
    </div>

    <div v-else-if="error" class="state-message error-message">
      <p>載入失敗：{{ error }}</p>
    </div>

    <div v-else-if="!hasLocalItems && !hasUnavailableItems" class="state-message no-items">
      <p>您的購物車目前是空的。</p>
      <button @click="goToProducts" class="go-to-products-button">前往選購產品</button>
    </div>

    <div v-else class="cart-content">
      <div v-if="hasLocalItems" class="cart-items-list">
        <h2>可購買的商品</h2>
        <div
          v-for="item in availableCartItems"
          :key="item.cartItemId"
          class="cart-item"
          :class="{ 'stock-exceeded': item.quantity > item.stock }"
        >
          <img
            :src="item.imageUrl"
            :alt="item.productName"
            class="item-image"
          />
          <div class="item-details">
            <h3 class="item-name">{{ item.productName }}</h3>
            <p class="item-price">單價: NT$ {{ formatNumber(item.productPrice) }}</p>
            <div class="item-quantity-controls">
              <button
                @click="updateLocalQuantity(item.cartItemId, item.quantity - 1)"
                :disabled="item.quantity <= 1 || isSyncing"
                class="quantity-btn"
              >-</button>

              <input
                type="number"
                v-model.number="item.quantity"
                min="1"
                @change="validateQuantity(item)"
                class="quantity-input"
                :disabled="isSyncing"
              />

              <button
                @click="updateLocalQuantity(item.cartItemId, item.quantity + 1)"
                class="quantity-btn"
                :disabled="isSyncing || item.quantity >= item.stock"
              >+</button>
            </div>
            <p v-if="item.quantity > item.stock" class="stock-warning">
              ⚠️ 數量超過庫存！庫存剩餘 **{{ item.stock }}**
            </p>
          </div>
          <div class="item-subtotal-actions">
            <p class="item-subtotal">
              小計: NT$ {{ formatNumber(item.productPrice * item.quantity) }}
            </p>
            <button
              @click="removeLocalItem(item.cartItemId)"
              class="remove-button"
              :disabled="isSyncing"
            >移除</button>
          </div>
        </div>
      </div>

      <div v-if="hasUnavailableItems" class="unavailable-items-list">
        <h2>已下架或無庫存的商品</h2>
        <div
          v-for="item in unavailableItems"
          :key="item.cartItemId"
          class="cart-item unavailable-item"
        >
          <img
            :src="item.imageUrl"
            :alt="item.productName"
            class="item-image"
          />
          <div class="item-details">
            <h3 class="item-name">{{ item.productName }}</h3>
            <p class="item-price">單價: NT$ {{ formatNumber(item.productPrice) }}</p>
            <p class="item-status">此商品已下架或無庫存</p>
          </div>
          <div class="item-subtotal-actions">
            <button
              @click="removeLocalItem(item.cartItemId)"
              class="remove-button remove-unavailable"
              :disabled="isSyncing"
            >移除</button>
          </div>
        </div>
      </div>

      <div class="cart-summary">
        <div class="summary-row">
          <span>可購買商品總數:</span>
          <span>{{ availableCartItems.length }} 項</span>
        </div>
        <div class="summary-row">
          <span>可購買商品總量:</span>
          <span>{{ localTotalQuantity }} 件</span>
        </div>
        <div class="summary-row total-price">
          <span>總計金額:</span>
          <span class="price-display">NT$ {{ formatNumber(localTotalPrice) }}</span>
        </div>
        <div v-if="hasStockIssues" class="unsaved-notice">
          <p>⚠️ 結帳前請先調整數量，有商品庫存不足</p>
        </div>
        <button
          @click="goToCheckout"
          class="checkout-button"
          :disabled="isSyncing || !hasLocalItems || hasStockIssues"
        >
          {{ isSyncing ? '同步中...' : '前往結帳' }}
        </button>

        <button
          @click="clearCart"
          class="clear-cart-button"
          :disabled="isSyncing"
        >清空購物車</button>

        <button @click="continueShopping" class="continue-button">
          繼續購物
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { usePublicCartStore } from '../stores/usePublicCartStore.js'
import { useMemberAuthStore } from '@/stores/memberAuth.js'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = usePublicCartStore()
const memberAuthStore = useMemberAuthStore()
const { cartItems, isLoading, error } = storeToRefs(cartStore)

// 本地購物車狀態
const localCartItems = ref([])
const originalCartItems = ref([])
const isSyncing = ref(false)

// 新增：已下架或無庫存的商品清單
const unavailableItems = computed(() => {
  return localCartItems.value.filter(item => item.stock <= 0)
})

// 新增：可購買的商品清單
const availableCartItems = computed(() => {
  return localCartItems.value.filter(item => item.stock > 0)
})

// 計算屬性
const hasLocalItems = computed(() => availableCartItems.value.length > 0)
const hasUnavailableItems = computed(() => unavailableItems.value.length > 0)

// 新增：檢查是否有任何商品數量超過庫存
const hasStockIssues = computed(() => {
  return availableCartItems.value.some(item => item.quantity > item.stock)
})

// 本地總數量（只計算可購買的商品）
const localTotalQuantity = computed(() => {
  return availableCartItems.value.reduce((total, item) => total + item.quantity, 0)
})

// 本地總價格（只計算可購買的商品）
const localTotalPrice = computed(() => {
  return availableCartItems.value.reduce((total, item) => {
    return total + (item.productPrice * item.quantity)
  }, 0)
})

// 格式化數字（加入千分位）
const formatNumber = (num) => {
  return Math.round(num).toLocaleString('zh-TW')
}

// 更新本地數量（不呼叫 API）
const updateLocalQuantity = (cartItemId, newQuantity) => {
  const item = localCartItems.value.find(i => i.cartItemId === cartItemId)
  if (item) {
    if (newQuantity > item.stock) {
      ElMessage.warning(`「${item.productName}」庫存不足，數量已達上限 ${item.stock}。`)
    }
    if (newQuantity < 1) {
      newQuantity = 1
    }

    item.quantity = newQuantity
  }
}

// 驗證數量輸入
const validateQuantity = (item) => {
  if (!item.quantity || item.quantity < 1) {
    item.quantity = 1
  }
  if (item.quantity > item.stock) {
    ElMessage.warning(`「${item.productName}」庫存不足，數量不能超過 ${item.stock}。`)
  }
  item.quantity = Math.round(item.quantity)
}

// 移除項目（直接呼叫 API）
const removeLocalItem = async (cartItemId) => {
  const item = localCartItems.value.find(i => i.cartItemId === cartItemId)
  if (!item) return

  try {
    await ElMessageBox.confirm(
      `確定要移除「${item.productName}」嗎？`,
      '確認移除',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    isSyncing.value = true
    await cartStore.removeCartItemById(cartItemId)
    await cartStore.fetchCartItems()
    ElMessage.success('商品已成功移除')
  } catch (error) {
    if (error !== 'cancel') {
        ElMessage.error('移除商品失敗，請稍後再試。')
    }
  } finally {
    isSyncing.value = false
  }
}

// 清空購物車
const clearCart = async () => {
  try {
    await ElMessageBox.confirm(
      '確定要清空整個購物車嗎？此操作將立即生效。',
      '確認清空',
      {
        confirmButtonText: '確定清空',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    isSyncing.value = true
    await cartStore.clearTheCart()
    await cartStore.fetchCartItems()
    ElMessage.success('購物車已清空')
  } catch {
    // 使用者取消
  } finally {
    isSyncing.value = false
  }
}

// 前往結帳（只處理數量變更）
const goToCheckout = async () => {
  if (!memberAuthStore.isAuthenticated) {
    ElMessage.warning('請先登入會員才能結帳')
    router.push({ name: 'Login' })
    return
  }

  if (!hasLocalItems.value) {
    ElMessage.warning('購物車沒有可購買的商品')
    return
  }

  if (hasStockIssues.value) {
    ElMessage.warning('購物車內有商品數量超過庫存，請先調整後再結帳。')
    return
  }
  
  const unsavedQuantityChanges = availableCartItems.value.some(item => {
      const original = originalCartItems.value.find(o => o.cartItemId === item.cartItemId)
      return original && original.quantity !== item.quantity
  })

  if (unsavedQuantityChanges) {
    isSyncing.value = true
    const syncErrors = []

    for (const item of availableCartItems.value) {
      const original = originalCartItems.value.find(o => o.cartItemId === item.cartItemId)
      if (original && original.quantity !== item.quantity) {
        try {
          if (item.quantity > item.stock) {
            syncErrors.push(`「${item.productName}」數量超過庫存，無法同步。`);
            continue;
          }
          await cartStore.updateProductQuantityAction(item.cartItemId, item.quantity)
        } catch (err) {
          syncErrors.push(`更新 ${item.productName} 數量失敗`)
        }
      }
    }
    isSyncing.value = false

    if (syncErrors.length > 0) {
      ElMessage.warning(`部分更新失敗: ${syncErrors.join(', ')}`)
      const result = await ElMessageBox.confirm(
        '部分商品更新失敗，是否仍要繼續結帳？',
        '提示',
        {
          confirmButtonText: '繼續結帳',
          cancelButtonText: '返回修正',
          type: 'warning'
        }
      ).catch(() => false)

      if (!result) return
    }
  }

  router.push({ name: 'Check' })
}

const goToProducts = () => {
  router.push({ name: 'Products' })
}

const continueShopping = () => {
  router.push({ name: 'Products' })
}

watch(cartItems, (newItems) => {
    localCartItems.value = JSON.parse(JSON.stringify(newItems))
    originalCartItems.value = JSON.parse(JSON.stringify(newItems))
}, { deep: true })

onMounted(async () => {
  await cartStore.fetchCartItems()
  localCartItems.value = JSON.parse(JSON.stringify(cartItems.value))
  originalCartItems.value = JSON.parse(JSON.stringify(cartItems.value))
})
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.state-message {
  text-align: center;
  padding: 40px;
  font-size: 16px;
  color: #666;
}

.error-message {
  color: #f56c6c;
}

.no-items {
  padding: 60px 20px;
}

.go-to-products-button {
  margin-top: 20px;
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  background-color: #409eff;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.go-to-products-button:hover {
  background-color: #66b1ff;
}

.cart-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.cart-items-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fff;
  transition: box-shadow 0.3s;
}

.cart-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.item-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.item-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.item-price {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.item-quantity-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border: 1px solid #dcdfe6;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.quantity-btn:hover:not(:disabled) {
  background: #f5f7fa;
  border-color: #409eff;
  color: #409eff;
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input {
  width: 60px;
  height: 32px;
  text-align: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.item-subtotal-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  min-width: 120px;
}

.item-subtotal {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
  margin: 0;
  white-space: nowrap;
}

.remove-button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background-color: #f56c6c;
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.remove-button:hover {
  background-color: #f78989;
}

.cart-summary {
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 16px;
  color: #606266;
}

.total-price {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  padding-top: 12px;
  border-top: 1px solid #dcdfe6;
  margin-top: 12px;
}

.price-display {
  color: #f56c6c;
  font-weight: bold;
}

.unsaved-notice {
  background: #fff4e6;
  border: 1px solid #ffb800;
  border-radius: 4px;
  padding: 10px;
  margin: 15px 0;
}

.unsaved-notice p {
  margin: 0;
  color: #e6a23c;
  font-size: 14px;
  text-align: center;
}

.checkout-button {
  width: 100%;
  margin-top: 20px;
  padding: 12px 20px;
  border: none;
  border-radius: 6px;
  background-color: #67c23a;
  color: white;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.checkout-button:hover:not(:disabled) {
  background-color: #85ce61;
}

.checkout-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.clear-cart-button {
  width: 100%;
  margin-top: 12px;
  padding: 10px 20px;
  border: 1px solid #e6a23c;
  border-radius: 6px;
  background-color: white;
  color: #e6a23c;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-cart-button:hover:not(:disabled) {
  background-color: #e6a23c;
  color: white;
}

.clear-cart-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.continue-button {
  width: 100%;
  margin-top: 8px;
  padding: 8px 16px;
  border: 1px solid #909399;
  border-radius: 6px;
  background-color: white;
  color: #909399;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.continue-button:hover {
  background-color: #909399;
  color: white;
}

.reset-button {
  width: 100%;
  margin-top: 8px;
  padding: 8px 16px;
  border: 1px solid #409eff;
  border-radius: 6px;
  background-color: white;
  color: #409eff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.reset-button:hover {
  background-color: #409eff;
  color: white;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .cart-item {
    flex-direction: column;
  }
  
  .item-image {
    width: 100%;
    height: 200px;
  }
  
  .item-subtotal-actions {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
    margin-top: 12px;
  }
  .cart-item.stock-exceeded {
    border: 2px solid #f56c6c;
  }
  .stock-warning {
    color: #f56c6c;
    font-weight: bold;
    font-size: 14px;
    margin-top: 5px;
  }
}
</style>