<template>
  <div class="product-list-page">
    <div class="action-bar">
      <div class="search-and-filter">
        <input
          v-model="searchParams.keyword"
          placeholder="搜尋產品名稱"
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <select v-model="searchParams.categoryId" @change="handleSearch" class="category-select">
          <option value="">所有分類</option>
          <option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">
            {{ category.categoryName }}
          </option>
        </select>
        
        <div class="price-filter">
          <input type="number" v-model.number="searchParams.minPrice" min="0" placeholder="最低價" />
          <span>-</span>
          <input type="number" v-model.number="searchParams.maxPrice" min="0" placeholder="最高價" />
        </div>
        <button @click="handleSearch" class="action-bar-button">搜尋</button>
      </div>

      <div class="flex-grow"></div> 

      <div class="button-group">
        <button @click="toggleSortByPrice" class="action-bar-button sort-by-price-button">
          價格排序
          <span v-if="searchParams.sortBy === 'price'">
            {{ searchParams.direction === 'asc' ? '↑' : '↓' }}
          </span>
        </button>
        
        <button @click="goToCart" class="cart-button">
          <div class="cart-icon-wrapper">
            <svg class="cart-icon" width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17M17 13v8m0-8L14.707 15.293c-.63.63-.184 1.707.707 1.707H17" 
                    stroke="currentColor" 
                    stroke-width="2" 
                    stroke-linecap="round" 
                    stroke-linejoin="round"/>
              <circle cx="9" cy="20" r="1" fill="currentColor"/>
              <circle cx="20" cy="20" r="1" fill="currentColor"/>
            </svg>
            
            <span v-if="cartStore.hasItems" class="cart-badge">
              {{ cartStore.cartItems.length > 99 ? '99+' : cartStore.cartItems.length }}
            </span>
          </div>
          <span class="cart-text">購物車</span>
        </button>
      </div>
    </div>
    
    <div v-if="productStore.isLoading" class="state-message">
      <p>產品列表載入中...</p>
    </div>
    <div v-else-if="productStore.error" class="state-message error-message">
      <p>載入失敗：{{ productStore.error }}</p>
    </div>
    <div v-else-if="!productStore.products.length" class="state-message">
      <p>目前沒有任何產品。</p>
    </div>

    <div v-else class="product-cards-container">
      <div v-for="product in productStore.products" :key="product.productId" class="product-card">
        <img 
          :src="product.imageUrl || product.description || 'https://placehold.co/300x200/cccccc/333333?text=No+Image'" 
          :alt="product.productName" 
          class="product-image" 
        />
        <div class="product-info">
          <h3>{{ product.productName }}</h3>
          <p class="product-price">NT$ {{ product.price }}</p>
        </div>
        <div class="product-actions">
          <button @click="addToCart(product.productId)" class="add-to-cart-button">加入購物車</button>
        </div>
      </div>
    </div>
    
    <div class="pagination">
      <button 
        :disabled="productStore.currentPage <= 0" 
        @click="handlePageChange(productStore.currentPage - 1)">上一頁</button>
      
      <div class="page-select-container" v-if="productStore.totalPages > 0">
        <select v-model="searchParams.page" @change="handlePageChange">
          <option v-for="n in productStore.totalPages" :key="n" :value="n - 1">
            第 {{ n }} 頁
          </option>
        </select>
        <span class="page-info">/ 共 {{ productStore.totalPages }} 頁</span>
      </div>

      <button 
        :disabled="productStore.currentPage >= productStore.totalPages - 1" 
        @click="handlePageChange(productStore.currentPage + 1)">下一頁</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'; 
import { useRouter } from 'vue-router';
import { usePublicProductStore } from '../stores/usePublicProductStore.js';
import { usePublicCartStore } from '../stores/usePublicCartStore.js';
import { useMemberAuthStore } from '../stores/memberAuthStore'
import { storeToRefs } from 'pinia'; 
import { ElMessage } from 'element-plus'; 
import axios from 'axios'; 
import authService from '@/services/authService.js';

const router = useRouter();
const productStore = usePublicProductStore();
const cartStore = usePublicCartStore();

const { products, totalPages, currentPage, isLoading, error } = storeToRefs(productStore); 

const searchParams = ref({
  keyword: '',
  minPrice: null,
  maxPrice: null,
  categoryId: '', // **從這裡將 categoryName 改為 categoryId**
  sortBy: 'creationTime',
  direction: 'desc',
  page: 0
});

const categories = ref([]);
const memberAuthStore = useMemberAuthStore()

// 獲取產品分類
const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/public/product/categories'); 
    categories.value = response.data;
  } catch (err) {
    ElMessage.error('獲取產品分類失敗：' + (err.response?.data?.message || err.message));
  }
};

// 搜尋產品
const handleSearch = () => {
  // 檢查是否選擇了分類
  if (searchParams.value.categoryId) {
    // 如果有分類ID，呼叫專門的分類搜尋方法
    productStore.fetchProductsByCategoryId(searchParams.value.categoryId);
  } else {
    // 否則，呼叫多條件篩選方法
    searchParams.value.page = 0;
    productStore.fetchProducts({ ...searchParams.value, size: 20 });
  }
};

// 價格排序
const toggleSortByPrice = () => {
  if (searchParams.value.sortBy !== 'price') {
    searchParams.value.sortBy = 'price';
    searchParams.value.direction = 'asc';
  } else {
    searchParams.value.direction = searchParams.value.direction === 'asc' ? 'desc' : 'asc';
  }
  handleSearch();
};

// 分頁切換
const handlePageChange = (page) => {
  let newPage = page;
  if (typeof page !== 'number') { 
    newPage = parseInt(event.target.value, 10);
  }
  if (newPage >= 0 && newPage < productStore.totalPages) {
    productStore.fetchProducts({ ...searchParams.value, page: newPage, size: 20 });
  }
};

// 前往購物車
const goToCart = () => {
  if (memberAuthStore.isAuthenticated) {
    router.push({ name: 'Cart' });
  } else {
    ElMessage.info('請先登入會員');
    router.push({ name: 'FluxAppLogin' });
  }
};

// 加入購物車
const addToCart = async (productId) => {
  // 判斷是否登入
  if (!memberAuthStore.isAuthenticated) {
    ElMessage.info('請先登入會員');
    router.push({ name: 'FluxAppLogin' });
    return;
  }

  try {
    await cartStore.addProductToCart(productId, 1);
    ElMessage.success('產品已成功加入購物車！');
  } catch (err) {
    ElMessage.error('加入購物車失敗：' + (err.response?.data?.message || err.message));
  }
};

onMounted(() => {
  productStore.fetchProducts({ page: 0, size: 20, sortBy: 'creationTime', direction: 'desc' });
  if (memberAuthStore.isAuthenticated) {
    cartStore.fetchCartItems();  // 只有登入才去拿購物車
  }
  fetchCategories();
});

watch(currentPage, (newPage) => {
  searchParams.value.page = newPage;
}, { immediate: true });
</script>

<style scoped>
/* ========== 產品列表頁面樣式 - 黑色背景版本 ========== */
.product-list-page {
  padding: 20px;
  background: #1a1a1a; /* 保持黑色背景 */
  min-height: 100vh;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 15px;
  background: #2d2d2d; /* 深灰色背景 */
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
}

.search-and-filter {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input, .category-select {
  width: 250px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 2px solid #444;
  background: #1a1a1a;
  color: #fff;
  transition: all 0.3s;
}

.search-input::placeholder {
  color: #888;
}

.search-input:focus, .category-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.category-select {
  width: auto;
  min-width: 140px;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-filter input {
  width: 90px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 2px solid #444;
  background: #1a1a1a;
  color: #fff;
  transition: all 0.3s;
}

.price-filter input::placeholder {
  color: #888;
}

.price-filter span {
  color: #fff;
}

.price-filter input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.action-bar-button {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
}

.action-bar-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(102, 126, 234, 0.4);
}

/* 美化的購物車按鈕樣式 */
.cart-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 8px rgba(245, 87, 108, 0.3);
  position: relative;
}

.cart-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(245, 87, 108, 0.4);
}

.cart-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.cart-icon {
  width: 22px;
  height: 22px;
}

.cart-text {
  font-size: 15px;
  letter-spacing: 0.5px;
}

.cart-badge {
  position: absolute;
  top: -12px;
  right: -12px;
  background: #ff3b30;
  color: white;
  font-size: 11px;
  font-weight: bold;
  padding: 3px 6px;
  border-radius: 12px;
  min-width: 18px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(255, 59, 48, 0.5);
  border: 2px solid #2d2d2d;
}

.button-group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.flex-grow {
  flex-grow: 1;
}

/* 狀態訊息 */
.state-message {
  text-align: center;
  padding: 60px;
  color: #fff;
  font-size: 18px;
  background: #2d2d2d;
  border-radius: 12px;
  margin: 20px 0;
}

.error-message {
  color: #ff6b6b;
  background: #3d2222;
}

/* 產品卡片容器 */
.product-cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
  padding: 10px;
}

/* 產品卡片 - 適合黑色背景 */
.product-card {
  background: #2d2d2d;
  border: 1px solid #444;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
  height: 420px;
  display: flex;
  flex-direction: column;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.product-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.product-card:hover::before {
  opacity: 1;
}

.product-image {
  width: 100%;
  height: 260px;
  object-fit: cover;
  transition: transform 0.4s;
  border-bottom: 1px solid #444;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-info {
  flex-grow: 1;
  padding: 15px 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #333;
}

.product-info h3 {
  font-size: 1.3em;
  margin: 0 0 8px 0;
  color: #fff; /* 白色文字在黑色背景上 */
  font-weight: 600;
  text-align: center;
  line-height: 1.4;
}

.product-price {
  font-weight: bold;
  font-size: 1.2em;
  margin: 5px 0;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.product-actions {
  padding: 0;
  border-top: none;
}

.add-to-cart-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 0;
  padding: 14px 20px;
  cursor: pointer;
  transition: all 0.3s;
  width: 100%;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.add-to-cart-button:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  box-shadow: inset 0 4px 8px rgba(0,0,0,0.2);
}

/* 分頁樣式 - 適合黑色背景 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
  padding: 20px;
  background: #2d2d2d;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
}

.page-select-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-select-container select {
  padding: 8px 12px;
  border: 2px solid #444;
  border-radius: 8px;
  background: #1a1a1a;
  color: #fff;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.page-select-container select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.page-info {
  color: #aaa;
  font-weight: 500;
}

.pagination button {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
}

.pagination button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(102, 126, 234, 0.4);
}

.pagination button:disabled {
  background: #444;
  box-shadow: none;
  cursor: not-allowed;
  opacity: 0.5;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .product-cards-container {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 15px;
  }
  
  .product-card {
    height: 380px;
  }
  
  .product-image {
    height: 220px;
  }
}
</style>