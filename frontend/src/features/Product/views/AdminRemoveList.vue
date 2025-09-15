<template>
  <div class="inactive-product-list-page">
    
    <div class="search-bar">
      <input 
        v-model="searchKeyword" 
        placeholder="搜尋產品名稱" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <!-- <button @click="handleSearch">搜尋</button>
      <button @click="resetSearch">重設</button> -->
    </div>

    <div v-if="isLoading" class="state-message">
      <p>產品列表載入中...</p>
    </div>
    <div v-else-if="error" class="state-message error-message">
      <p>載入失敗：{{ error }}</p>
    </div>
    <div v-else-if="filteredProducts.length === 0" class="state-message">
      <p>沒有找到符合條件的產品，或目前沒有任何已下架的產品。</p>
    </div>

    <div v-else class="product-cards-container">
      <div v-for="product in filteredProducts" :key="product.productId" class="product-card">
        <img :src="product.imageUrl" :alt="product.productName" class="product-image" />
        <div class="product-info">
          <h3>{{ product.productName }}</h3>
          <p class="product-price">NT$ {{ product.price }}</p>
        </div>
        <div class="product-actions">
          <button @click="handleReactivateProduct(product.productId)">重新上架</button>
          <button @click="handleDeleteProduct(product.productId)" class="delete-button">永久刪除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { useProductStore } from '../stores/useProductStore.js';
import { storeToRefs } from 'pinia';
import { ElMessage } from 'element-plus';

const productStore = useProductStore();
const { products, isLoading, error } = storeToRefs(productStore);

const searchKeyword = ref('');

// 新增：計算屬性，用於篩選產品列表
const filteredProducts = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return products.value;
  }
  return products.value.filter(product => 
    product.productName.toLowerCase().includes(keyword)
  );
});

// 新增：處理搜尋按鈕點擊
const handleSearch = () => {
    // 這裡不需要重新發送 API 請求，因為篩選是在前端進行的
    // filteredProducts 計算屬性會自動更新
};

// 新增：處理重設搜尋
const resetSearch = () => {
    searchKeyword.value = '';
};

// 處理重新上架產品
const handleReactivateProduct = async (productId) => {
  if (confirm('確定要重新上架此產品嗎？')) {
    try {
      await productStore.updateProduct(productId, { isAvailable: true });
      ElMessage.success('產品已成功重新上架！');
      // 重新載入列表以更新 UI
      productStore.fetchInactiveProducts();
    } catch (err) {
      ElMessage.error('上架失敗：' + err.message);
    }
  }
};

// 新增：處理永久刪除產品
const handleDeleteProduct = async (productId) => {
  if (confirm('警告：確定要永久刪除此產品嗎？此操作不可逆！')) {
    try {
      // 呼叫 Pinia Store 中的刪除方法
      await productStore.deleteProduct(productId);
      ElMessage.success('產品已永久刪除！');
      // 刪除後重新載入列表
      productStore.fetchInactiveProducts();
    } catch (err) {
      ElMessage.error('刪除失敗：' + err.message);
    }
  }
};

// ------------------- 生命週期 -------------------
onMounted(() => {
  productStore.fetchInactiveProducts();
});
</script>

<style scoped>
/* 搜尋欄位樣式 */
.search-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
}
.search-input {
    width: 250px;
    padding: 8px 12px;
    border-radius: 4px;
    border: 1px solid #ccc;
}
.search-bar button {
  padding: 8px 16px;
  border-radius: 4px;
  border: 1px solid #ccc;
  cursor: pointer;
  background-color: #f0f0f0;
}
.inactive-product-list-page {
  padding: 20px;
}
.state-message {
  text-align: center;
  padding: 50px;
  color: #666;
}
.error-message {
  color: #d9534f;
}
.product-cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.product-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: center;
  height: 350px; 
  display: flex;
  flex-direction: column;
}
.product-image {
  width: 100%;
  height: 70%; 
  object-fit: cover;
}
.product-info {
  flex-grow: 1;
  padding: 5px 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.product-info h3 {
  font-size: 1.2em;
  margin: 0;
}
.product-price {
  color: #007bff;
  font-weight: bold;
  margin: 5px 0;
}
.product-actions {
  display: flex;
  justify-content: space-around;
  padding: 8px;
  border-top: 1px solid #eee;
}
/* 刪除按鈕樣式 */
.delete-button {
  background-color: #f44336;
  color: white;
  border: none;
}
</style>