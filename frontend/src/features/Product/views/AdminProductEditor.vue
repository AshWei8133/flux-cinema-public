<template>
  <div class="product-form-container">
    <h2>{{ isEditMode ? '修改產品' : '新增產品' }}</h2>
    <form @submit.prevent="submitForm">
      <div class="form-group">
        <label for="productName">產品名稱:</label>
        <input type="text" id="productName" v-model="product.productName" required />
      </div>
      
      <div class="form-group">
        <label for="categoryId">分類:</label>
        <select id="categoryId" v-model.number="product.categoryId" required>
          <option value="" disabled>請選擇一個分類</option>
          <option 
            v-for="category in productStore.categories" 
            :key="category.id" 
            :value="category.id"
          >
            {{ category.name }}
          </option>
        </select>
       
        <p v-if="productStore.isLoading && productStore.categories.length === 0">載入分類中...</p>
        <p v-if="productStore.error && productStore.categories.length === 0" class="error-message">{{ productStore.error }}</p>
      </div>

      <div class="form-group">
        <label for="price">價格:</label>
        <input type="number" id="price" v-model.number="product.price" required />
      </div>
      <div class="form-group">
        <label for="description">產品描述:</label>
        <textarea id="description" v-model="product.description"></textarea>
      </div>
      <div class="form-group">
        <label for="stock">庫存:</label>
        <input type="number" id="stock" v-model.number="product.stock" required />
      </div>

      <div class="form-group">
        <label>圖片上傳:</label>
        <input type="file" @change="handleFileUpload" accept="image/*" />
        <p v-if="isUploading">圖片上傳中...</p>
      </div>

      <div v-if="product.imageUrl" class="image-preview">
        <p>圖片已上傳，網址:</p>
        <img :src="product.imageUrl" alt="上傳圖片預覽" />
      </div>

      <button type="submit">{{ isEditMode ? '更新產品' : '新增產品' }}</button>
      <button type="button" @click="goBack">返回</button>
    </form>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
// *** 修正後的導入路徑 ***
import { useProductStore } from '../stores/useProductStore'; // 現在路徑是 src/features/Product/views/ 回到 src/features/Product/stores/

const route = useRoute();
const router = useRouter();
const productStore = useProductStore(); // 實例化 Pinia Store

const isEditMode = ref(false);

const product = ref({
  productId: null, // 在編輯模式下會需要
  productName: '',
  categoryId: null, // 綁定選中的分類 ID
  price: null,
  description: '',
  imageUrl: '',
  stock: null,
});

const isUploading = ref(false);

// ------------------- 方法 -------------------

const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  isUploading.value = true;
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await axios.post('http://localhost:8888/api/admin/upload-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    product.value.imageUrl = response.data;
    alert('圖片上傳成功！'); // 實際應用中請替換為 ElMessage
  } catch (error) {
    console.error('圖片上傳失敗:', error);
    alert('圖片上傳失敗，請稍後再試。'); // 實際應用中請替換為 ElMessage
  } finally {
    isUploading.value = false;
  }
};

const submitForm = async () => {
  if (isUploading.value) {
    alert('請等待圖片上傳完成。'); // 實際應用中請替換為 ElMessage
    return;
  }
  
  // 檢查 product.categoryId 是否已經選擇
  if (!product.value.categoryId) {
    alert('請選擇產品分類！'); // 實際應用中請替換為 ElMessage
    return;
  }

  if (isEditMode.value) {
    try {
      await productStore.updateProduct(product.value.productId, product.value); // 使用 Store 的 updateProduct
      alert('產品更新成功！'); // 實際應用中請替換為 ElMessage
      goBack();
    } catch (error) {
      console.error('產品更新失敗:', error);
      alert('產品更新失敗，請檢查資料。'); // 實際應用中請替換為 ElMessage
    }
  } else {
    try {
      await productStore.createNewProduct(product.value); // 使用 Store 的 createNewProduct
      alert('產品新增成功！'); // 實際應用中請替換為 ElMessage
      goBack();
    } catch (error) {
      console.error('產品新增失敗:', error);
      alert('產品新增失敗，請檢查資料。'); // 實際應用中請替換為 ElMessage
    }
  }
};

const fetchProduct = async (productId) => {
  try {
    await productStore.fetchProductById(productId); // 使用 Store 的 fetchProductById
    // 將獲取到的產品數據賦值給本地的 product.value
    Object.assign(product.value, productStore.selectedProduct);
  } catch (error) {
    console.error('獲取產品資料失敗:', error);
    alert('無法載入產品資料，請檢查 ID。'); // 實際應用中請替換為 ElMessage
    goBack();
  }
};

const goBack = () => {
  router.back();
};

// ------------------- 生命周期 -------------------

onMounted(() => {
  console.log('AdminProductEditor.vue mounted, attempting to fetch categories...');
  productStore.fetchCategories(); // 在組件掛載時獲取所有分類
});

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      isEditMode.value = true;
      product.value.productId = newId; // 設置產品 ID
      fetchProduct(newId);
    } else {
      isEditMode.value = false;
      // 清空表單以備新增
      Object.assign(product.value, {
        productId: null,
        productName: '',
        categoryId: null, // 新增時確保分類 ID 為 null
        price: null,
        description: '',
        imageUrl: '',
        stock: null,
      });
    }
  },
  { immediate: true }
);

</script>

<style scoped>
.product-form-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}
.form-group {
  margin-bottom: 15px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.form-group input[type="text"],
.form-group input[type="number"],
.form-group textarea,
.form-group select { /* 新增 select 的樣式 */
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.image-preview img {
  max-width: 100%;
  height: auto;
  margin-top: 10px;
  border-radius: 4px;
}
button {
  padding: 10px 15px;
  margin-right: 10px;
  border: none;
  border-radius: 4px;
  background-color: #007bff;
  color: white;
  cursor: pointer;
  font-size: 16px;
}
button:hover {
  background-color: #0056b3;
}
.error-message {
  color: red;
  font-size: 0.9em;
  margin-top: 5px;
}
</style>
