<template>
  <div class="checkout-page">
    <h1 class="page-title">結帳確認</h1>

    <div v-if="isLoading" class="state-message">
      <p>載入中...</p>
    </div>
    <div v-else-if="!hasItems" class="state-message no-items">
      <p>購物車是空的</p>
      <button @click="goToCart" class="go-back-button">返回購物車</button>
    </div>

    <div v-else class="checkout-content">
      <!-- 左側：訂單商品明細 -->
      <div class="order-details">
        <h2>訂單明細</h2>
        <div class="cart-items-list">
          <div 
            v-for="item in cartItems" 
            :key="item.productId"
            class="cart-item"
          >
            <img :src="item.imageUrl" :alt="item.productName" class="item-image" />
            <div class="item-info">
              <h4>{{ item.productName }}</h4>
              <p>單價: NT$ {{ item.productPrice }}</p>
              <p>數量: {{ item.quantity }}</p>
            </div>
            <div class="item-subtotal">
              NT$ {{ item.productPrice * item.quantity }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右側：結帳資訊 -->
      <div class="checkout-form">
        <h2>結帳資訊</h2>
        
        <!-- Email 輸入 -->
        <div class="form-group">
          <label>聯絡 Email:</label>
          <input 
            type="email" 
            v-model="orderEmail" 
            :placeholder="memberEmail || '請輸入 Email'"
            class="form-input"
          />
        </div>

        <!-- 優惠券選擇 -->
        <div class="form-group">
          <label>優惠券:</label>
          <div class="coupon-section">
            <select 
              v-model.number="selectedCouponId" 
              @change="onCouponChange"
              class="form-select"
              :disabled="loadingCoupons"
            >
              <option :value="null">不使用優惠券</option>
              <option 
                v-for="memberCoupon in availableCoupons" 
                :key="memberCoupon.memberCouponId"
                :value="Number(memberCoupon.memberCouponId)"
                :disabled="!isCouponApplicable(memberCoupon)"
              >
                {{ memberCoupon.couponName }} - 
                {{ formatCouponDiscount(memberCoupon) }}
                {{ !isCouponApplicable(memberCoupon) ? `(需滿 NT$ ${memberCoupon.minimumSpend})` : '' }}
                {{ memberCoupon.expiryDate ? ` (到期: ${formatDate(memberCoupon.expiryDate)})` : '' }}
              </option>
            </select>
            
            <!-- 優惠券詳情顯示 -->
            <div v-if="selectedCoupon" class="coupon-details">
              <div class="coupon-info">
                <span class="coupon-name">{{ selectedCoupon.couponName }}</span>
                <span class="coupon-description">{{ selectedCoupon.description }}</span>
              </div>
              <div v-if="couponValidationMessage" :class="['validation-message', couponValidationStatus]">
                {{ couponValidationMessage }}
              </div>
            </div>
          </div>
        </div>

        <!-- 付款方式 -->
        <div class="form-group">
          <label>付款方式:</label>
          <select v-model="paymentMethod" class="form-select">
            <option value="CREDIT_CARD">信用卡</option>
            <option value="CASH">現金</option>
          </select>
        </div>

        <!-- 金額摘要 -->
        <div class="price-summary">
          <div class="summary-row">
            <span>商品小計:</span>
            <span>NT$ {{ subtotal }}</span>
          </div>
          <div class="summary-row" v-if="discountAmount > 0">
            <span>優惠折扣 ({{ selectedCoupon?.couponName }}):</span>
            <span class="discount">-NT$ {{ discountAmount }}</span>
          </div>
          <div class="summary-row total">
            <span>應付總額:</span>
            <span>NT$ {{ totalAmount }}</span>
          </div>
        </div>

        <!-- 按鈕區 -->
        <div class="button-group">
          <button @click="goToCart" class="back-button">返回購物車</button>
          <button 
            @click="submitOrder" 
            class="submit-button"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? '處理中...' : '確認訂單' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { usePublicCartStore } from '../stores/usePublicCartStore.js'
import { useMemberAuthStore } from '@/stores/memberAuth.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import httpClient from '@/services/api'
import publicCouponService from '../services/publicCouponService'

const router = useRouter()
const cartStore = usePublicCartStore()
const memberAuthStore = useMemberAuthStore()
const { cartItems, hasItems, isLoading } = storeToRefs(cartStore)

// 表單資料
const orderEmail = ref('')
const selectedCouponId = ref(null)
const selectedCoupon = ref(null)
const selectedMemberCoupon = ref(null)
const paymentMethod = ref('CREDIT_CARD')
const discountAmount = ref(0)
const isSubmitting = ref(false)

// 優惠券相關
const availableCoupons = ref([])
const loadingCoupons = ref(false)
const couponValidationMessage = ref('')
const couponValidationStatus = ref('')

// 取得會員 email
const memberEmail = computed(() => memberAuthStore.memberInfo?.email || '')

// 計算金額
const subtotal = computed(() => {
  return cartItems.value.reduce((sum, item) => 
    sum + (item.productPrice * item.quantity), 0
  )
})

const totalAmount = computed(() => {
  return Math.max(0, subtotal.value - discountAmount.value)
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getDate().toString().padStart(2, '0')}`
}

// 格式化優惠券折扣顯示
const formatCouponDiscount = (memberCoupon) => {
  if (memberCoupon.discountType === 'FIXED') {
    return `折扣 NT$ ${memberCoupon.discountAmount}`
  } else if (memberCoupon.discountType === 'PERCENTAGE') {
    return `${memberCoupon.discountAmount}% OFF`
  }
  return ''
}

// 判斷優惠券是否可用
const isCouponApplicable = (memberCoupon) => {
  return subtotal.value >= memberCoupon.minimumSpend
}

// 載入優惠券列表
const loadCoupons = async () => {
  loadingCoupons.value = true
  try {
    const coupons = await publicCouponService.getUnusedCoupons()
    availableCoupons.value = coupons || []
  } catch (error) {
    console.error('載入優惠券失敗:', error)
    availableCoupons.value = []
  } finally {
    loadingCoupons.value = false
  }
}

// 當選擇優惠券時
const onCouponChange = () => {
  if (!selectedCouponId.value) {
    selectedCoupon.value = null
    selectedMemberCoupon.value = null
    discountAmount.value = 0
    couponValidationMessage.value = ''
    couponValidationStatus.value = ''
    return
  }

  selectedMemberCoupon.value = availableCoupons.value.find(mc => Number(mc.memberCouponId) === selectedCouponId.value)
  if (!selectedMemberCoupon.value) return

  selectedCoupon.value = selectedMemberCoupon.value

  if (subtotal.value < selectedCoupon.value.minimumSpend) {
    couponValidationMessage.value = `此優惠券需要最低消費 NT$ ${selectedCoupon.value.minimumSpend}`
    couponValidationStatus.value = 'error'
    discountAmount.value = 0
    return
  }

  calculateCouponDiscount()
}

// 計算優惠券折扣
const calculateCouponDiscount = () => {
  if (!selectedCoupon.value) {
    discountAmount.value = 0
    return
  }

  if (selectedCoupon.value.discountType === 'FIXED') {
    discountAmount.value = selectedCoupon.value.discountAmount
  } else if (selectedCoupon.value.discountType === 'PERCENTAGE') {
    const percent = selectedCoupon.value.discountAmount / 100
    discountAmount.value = Math.round(subtotal.value * (1-percent))
  }

  couponValidationMessage.value = `已套用優惠券，節省 NT$ ${discountAmount.value}`
  couponValidationStatus.value = 'success'
}

// 監聽購物車小計變化，重新計算優惠券
watch(subtotal, () => {
  if (selectedCoupon.value && subtotal.value >= selectedCoupon.value.minimumSpend) {
    calculateCouponDiscount()
  } else if (selectedCoupon.value && subtotal.value < selectedCoupon.value.minimumSpend) {
    discountAmount.value = 0
    couponValidationMessage.value = ''
    couponValidationStatus.value = ''
    ElMessage.warning('訂單金額變更，優惠券已移除')
    selectedCoupon.value = null
    selectedMemberCoupon.value = null
    selectedCouponId.value = null
  }
})

// 提交訂單
const submitOrder = async () => {
  if (isSubmitting.value) return

  if (!orderEmail.value && !memberEmail.value) {
    ElMessage.error('請輸入聯絡 Email')
    return
  }

  try {
    await ElMessageBox.confirm(
      `確認要送出訂單嗎？總金額: NT$ ${totalAmount.value}`,
      '確認訂單',
      {
        confirmButtonText: '確認送出',
        cancelButtonText: '再想想',
        type: 'info'
      }
    )
  } catch {
    return
  }

  isSubmitting.value = true

  const createOrderDTO = {
    memberId: memberAuthStore.memberInfo?.memberId,
    email: orderEmail.value || memberEmail.value,
    orderDetails: cartItems.value.map(item => ({
      productId: parseInt(item.productId, 10),
      quantity: parseInt(item.quantity, 10),
      extraPrice: parseInt(item.extraPrice || 0, 10)
    })),
    couponId: selectedCoupon.value?.couponId || null,
    paymentMethod: paymentMethod.value
  }

  try {
    const response = await httpClient.post('/public/order/create', createOrderDTO)
    
    if (response?.orderId) {
      ElMessage.success(response.message || '訂單建立成功！')
      await cartStore.clearTheCart()
      router.push({ 
        name: 'OrderSuccess',
        params: { orderId: response.orderNumber }
      })
    } else {
      ElMessage.error('訂單建立失敗')
    }
  } catch (err) {
    console.error('訂單建立失敗:', err)
  } finally {
    isSubmitting.value = false
  }
  console.log('送出的訂單資料', createOrderDTO)
}


// 返回購物車
const goToCart = () => {
  router.push({ name: 'Cart' })
}

// 初始化
onMounted(() => {
  if (!memberAuthStore.isAuthenticated) {
    ElMessage.warning('請先登入')
    router.push({ name: 'Login' })
    return
  }

  cartStore.fetchCartItems()
  loadCoupons()

  if (memberEmail.value) {
    orderEmail.value = memberEmail.value
  }
})
</script>

<style scoped>
/* ========== 結帳頁面樣式 ========== */

/* 整體頁面 */
.checkout-page {
  max-width: 1600px;
  margin: 20px auto;
  padding: 20px 30px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 40px);
}

/* 頁面標題 */
.page-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 2.2em;
  color: #2d3748;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 狀態訊息 */
.state-message {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  color: #4a5568;
  font-size: 1.2em;
}

.state-message.no-items {
  background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
}

/* 返回按鈕 */
.go-back-button {
  margin-top: 20px;
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.2);
}

.go-back-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(102, 126, 234, 0.3);
}

/* 主內容區 */
.checkout-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
  width: 100%;
}

/* ---------------- 左側：訂單明細 ---------------- */
.order-details {
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.order-details h2 {
  color: #2d3748;
  margin-bottom: 20px;
  font-size: 1.6em;
  font-weight: 600;
  border-bottom: 3px solid;
  border-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%) 1;
  padding-bottom: 8px;
}

/* 購物車列表 */
.cart-items-list {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}

.cart-items-list::-webkit-scrollbar {
  width: 8px;
}

.cart-items-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.cart-items-list::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
}

/* 單一購物車項目 */
.cart-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  transition: all 0.3s;
}

.cart-item:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

/* 商品圖片 */
.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  margin-right: 15px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

/* 商品資訊 */
.item-info {
  flex-grow: 1;
}

.item-info h4 {
  color: #2d3748;
  margin: 0 0 6px 0;
  font-size: 1.1em;
  font-weight: 600;
}

.item-info p {
  color: #4a5568;
  margin: 3px 0;
  font-size: 0.9em;
}

/* 小計金額 */
.item-subtotal {
  font-size: 1.2em;
  font-weight: bold;
  color: #f5576c;
  min-width: 100px;
  text-align: right;
}

/* ---------------- 右側：結帳資訊 ---------------- */
.checkout-form {
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  height: fit-content;
  position: sticky;
  top: 20px;
  min-width: 0;
  overflow: hidden;
}

.checkout-form h2 {
  color: #2d3748;
  margin-bottom: 20px;
  font-size: 1.6em;
  font-weight: 600;
  border-bottom: 3px solid;
  border-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%) 1;
  padding-bottom: 8px;
}

/* 表單群組 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2d3748;
  font-size: 1em;
}

/* 輸入框 / 下拉選單 */
.form-input,
.form-select {
  width: 100%;
  padding: 10px 15px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  background: white;
  color: #2d3748;
  box-sizing: border-box;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-select:disabled {
  background: #f7fafc;
  cursor: not-allowed;
}

/* 優惠券區塊 */
.coupon-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.coupon-details {
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.coupon-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coupon-name {
  font-weight: 600;
  color: #2d3748;
  font-size: 0.95em;
}

.coupon-description {
  color: #718096;
  font-size: 0.85em;
}

.validation-message {
  margin-top: 8px;
  padding: 8px;
  border-radius: 6px;
  font-size: 0.85em;
  font-weight: 500;
}

.validation-message.success {
  background: #c6f6d5;
  color: #22543d;
  border: 1px solid #9ae6b4;
}

.validation-message.error {
  background: #fed7d7;
  color: #742a2a;
  border: 1px solid #fc8181;
}

/* 金額摘要 */
.price-summary {
  padding: 18px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  margin: 20px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #4a5568;
  font-size: 1em;
}

.summary-row.total {
  font-size: 1.3em;
  font-weight: bold;
  color: #2d3748;
  border-top: 2px solid #cbd5e0;
  padding-top: 10px;
  margin-top: 12px;
}

.discount {
  color: #48bb78;
  font-weight: 600;
}

/* 按鈕區 */
.button-group {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.back-button {
  flex: 1;
  padding: 12px;
  border: 2px solid #e2e8f0;
  background: white;
  color: #4a5568;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.back-button:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
  transform: translateY(-2px);
}

.submit-button {
  flex: 2;
  padding: 12px;
  border: none;
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  border-radius: 10px;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 6px rgba(72, 187, 120, 0.25);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(72, 187, 120, 0.35);
  background: linear-gradient(135deg, #38a169 0%, #48bb78 100%);
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* ---------------- 響應式設計 ---------------- */
@media (max-width: 1200px) {
  .checkout-page {
    max-width: 100%;
    padding: 15px;
  }

  .checkout-content {
    grid-template-columns: 1fr;
  }

  .checkout-form {
    position: static;
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 1.8em;
  }

  .cart-item {
    flex-direction: column;
    text-align: center;
  }

  .item-image {
    margin: 0 0 10px 0;
  }

  .item-subtotal {
    margin-top: 8px;
    text-align: center;
  }

  .button-group {
    flex-direction: column;
  }

  .back-button,
  .submit-button {
    width: 100%;
  }
}
</style>