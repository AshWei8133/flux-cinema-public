<template>
  <div class="restock-product-list-page">
    
    <div v-if="isLoading" class="state-message">
      <p>產品列表載入中...</p>
    </div>
    <div v-else-if="error" class="state-message error-message">
      <p>載入失敗：{{ error }}</p>
    </div>
    <div v-else-if="restockProducts.length === 0" class="state-message">
      <p>目前沒有任何產品需要補貨。</p>
    </div>

    <div v-else class="product-cards-container">
      <div v-for="product in restockProducts" :key="product.productId" class="product-card">
        <img :src="product.imageUrl" :alt="product.productName" class="product-image" />
        <div class="product-info">
          <h3>{{ product.productName }}</h3>
          <p class="product-stock">目前庫存: {{ product.stock || product.stockQuantity }}</p>
        </div>
        <div class="product-actions">
          <button @click="handleRestockProduct(product)">修改庫存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useProductStore } from '../stores/useProductStore.js';
import { storeToRefs } from 'pinia';
import { ElMessage, ElMessageBox } from 'element-plus';
import { updateProduct } from '../services/productService.js';

const productStore = useProductStore();
const { restockProducts, isLoading, error } = storeToRefs(productStore);

// 處理修改庫存
const handleRestockProduct = async (product) => {
  try {
    const { value: newStock } = await ElMessageBox.prompt(
      `請輸入 ${product.productName} 的新庫存量`,
      '修改庫存',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        inputPattern: /^[0-9]+$/,
        inputErrorMessage: '請輸入有效的數字'
      }
    );

    if (newStock !== null) {
      await updateProduct(product.productId, {
        stockQuantity: parseInt(newStock)
      });
      ElMessage.success('庫存已成功更新！');

      productStore.fetchRestockProducts();
    }
  } catch (err) {
    if (err !== 'cancel') {
        ElMessage.error('庫存更新失敗：' + (err.message || '未知錯誤'));
    }
  }
};

// ------------------- 生命週期 -------------------
onMounted(() => {
  productStore.fetchRestockProducts();
});
</script>

<style scoped>
.restock-product-list-page {
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
  min-height: 350px; 
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
.product-stock {
  font-weight: bold;
  margin: 5px 0;
  color: #ff9800;
}
.product-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8px;
  border-top: 1px solid #eee;
  margin-top: auto; 
}
.product-actions button {
  width: 100%;
  padding: 8px 16px;
  border: none;
  background-color: #4CAF50;
  color: white;
  cursor: pointer;
  font-weight: bold;
  box-sizing: border-box;
}
</style>