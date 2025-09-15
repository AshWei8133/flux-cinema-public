<template>
  <div class="product-list-page">
    <div class="action-bar">
      <!-- 左側：搜尋和篩選區塊 -->
      <div class="search-and-filter">
        <input
          v-model="searchParams.keyword"
          placeholder="搜尋產品名稱"
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <div class="price-filter">
          <input type="number" v-model.number="searchParams.minPrice" min="0" placeholder="最低價" />
          <span>-</span>
          <input type="number" v-model.number="searchParams.maxPrice" min="0" placeholder="最高價" />
        </div>
        <button @click="handleSearch" class="action-bar-button">搜尋</button>
      </div>

      <!-- 中間空白彈性空間，將右側按鈕推到最右邊 -->
      <div class="flex-grow"></div> 

      <!-- 右側：新增分類、新增產品和價格排序按鈕 -->
      <div class="button-group">
        <button @click="openAddCategoryDialog" class="add-category-button action-bar-button">新增分類</button>
        <button @click="openDialog('add')" class="action-bar-button">新增產品</button>
        <button @click="toggleSortByPrice" class="action-bar-button sort-by-price-button">
          價格排序
          <span v-if="searchParams.sortBy === 'price'">
            {{ searchParams.direction === 'asc' ? '↑' : '↓' }}
          </span>
        </button>
      </div>
    </div>

    <!-- 原來的 sort-bar 已經移除，其內容已合併到 action-bar -->

    <div v-if="isLoading" class="state-message">
      <p>產品列表載入中...</p>
    </div>
    <div v-else-if="error" class="state-message error-message">
      <p>載入失敗：{{ error }}</p>
    </div>
    <div v-else-if="!products.length" class="state-message">
      <p>目前沒有任何產品。</p>
    </div>

    <div v-else class="product-cards-container">
      <div v-for="product in products" :key="product.productId" class="product-card">
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
          <button @click="openDialog('edit', product)">修改</button>
          <button @click="handleDeleteProduct(product.productId)">下架</button>
        </div>
      </div>
    </div>
    
    <div class="pagination">
      <button 
        :disabled="currentPage <= 0" 
        @click="handlePageChange(currentPage - 1)">上一頁</button>
      
      <div class="page-select-container" v-if="totalPages > 0">
        <select v-model="searchParams.page" @change="handlePageChange">
          <option v-for="n in totalPages" :key="n" :value="n - 1">
            第 {{ n }} 頁
          </option>
        </select>
        <span class="page-info">/ 共 {{ totalPages }} 頁</span>
      </div>

      <button 
        :disabled="currentPage >= totalPages - 1" 
        @click="handlePageChange(currentPage + 1)">下一頁</button>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" ref="formRef" label-width="120px" :rules="formRules">
        <el-form-item label="產品名稱" prop="productName">
          <el-input v-model="form.productName" />
        </el-form-item>
        
        <el-form-item label="分類" prop="categoryId">
          <el-select 
            v-model.number="form.categoryId" 
            placeholder="請選擇一個分類" 
            required 
            :disabled="isLoading"
          >
            <el-option
              v-if="isLoading"
              value="" disabled
              label="載入分類中..."
            ></el-option>
            <el-option
              v-else-if="error"
              value="" disabled
              label="載入失敗"
            ></el-option>
            <template v-else-if="categories && categories.length > 0">
              <el-option
                v-for="category in categories"
                :key="category.categoryId"
                :label="category.categoryName"
                :value="category.categoryId"
              ></el-option>
            </template>
            <el-option
              v-else
              value="" disabled
              label="沒有可用分類"
            ></el-option>

          </el-select>
          <p v-if="error" class="error-message">
            {{ error }}
          </p>
        </el-form-item>

        <el-form-item label="價格" prop="price">
          <el-input-number v-model="form.price" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="產品描述" prop="description">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
        <el-form-item label="庫存" prop="stock">
          <el-input-number v-model="form.stock" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="產品圖片" prop="imageUrl">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageChange"
            :http-request="uploadImage"
            class="upload-demo"
            :limit="1"
          >
            <el-button type="primary">選擇圖片</el-button>
            <div class="el-upload__tip">只能上傳 JPG/PNG 圖片，且不超過 2MB。</div>
          </el-upload>
          <div v-if="form.imageUrl" class="image-preview">
            <img :src="form.imageUrl" alt="產品圖片" />
          </div>
        </el-form-item>
        
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">
            {{ isEditMode ? '更新' : '新增' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="categoryDialogVisible" title="新增產品分類" width="400px">
      <el-form :model="{ newCategoryName }" ref="categoryFormRef" label-width="100px" :rules="categoryFormRules">
        <el-form-item label="分類名稱" prop="newCategoryName">
          <el-input v-model="newCategoryName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAddCategory">新增</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, watch, nextTick, isRef } from 'vue'; 
import { useRouter } from 'vue-router';
import { useProductStore } from '../stores/useProductStore.js'; 
import { uploadImage as uploadImageService } from '../services/productService.js';
import { storeToRefs } from 'pinia'; 
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElSelect, ElOption, ElUpload, ElButton } from 'element-plus'; 
import axios from 'axios'; 

const router = useRouter();
const productStore = useProductStore();
const { products, totalPages, currentPage, isLoading, error, categories } = storeToRefs(productStore); 

console.log('ProductListPage.vue setup: categories after storeToRefs:', isRef(categories) ? categories.value : categories); 

const searchParams = ref({
  keyword: '',
  minPrice: null,
  maxPrice: null,
  sortBy: 'creationTime',
  direction: 'desc',
  page: 0
});
const selectedFile = ref(null);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const isEditMode = ref(false);
const formRef = ref(null);
const uploadRef = ref(null);
const form = reactive({
  productId: null,
  productName: '',
  categoryId: null, 
  price: null,
  description: '',
  imageUrl: '',
  stock: null,
});

const formRules = {
  productName: [{ required: true, message: '請輸入產品名稱', trigger: 'blur' }],
  categoryId: [{ required: true, message: '請選擇產品分類', trigger: 'change' }], 
  price: [{ required: true, message: '請輸入價格', trigger: 'blur' }],
  stock: [{ required: true, message: '請輸入庫存', trigger: 'blur' }],
};

const categoryDialogVisible = ref(false);
const newCategoryName = ref('');
const categoryFormRef = ref(null);
const categoryFormRules = {
  newCategoryName: [{ required: true, message: '請輸入分類名稱', trigger: 'blur' }],
};


const handleSearch = () => {
  searchParams.value.page = 0;
  productStore.fetchProducts({ ...searchParams.value, size: 20 });
};

const toggleSortByPrice = () => {
  if (searchParams.value.sortBy !== 'price') {
    searchParams.value.sortBy = 'price';
    searchParams.value.direction = 'asc';
  } else {
    searchParams.value.direction = searchParams.value.direction === 'asc' ? 'desc' : 'asc';
  }
  handleSearch();
};

const handlePageChange = (page) => {
  let newPage = page;
  if (typeof page !== 'number') { 
    newPage = parseInt(event.target.value, 10);
  }
  
  if (newPage >= 0 && newPage < totalPages.value) {
    productStore.fetchProducts({
      ...searchParams.value,
      page: newPage,
      size: 20,
    });
  }
};

const openDialog = async (mode, productData = null) => {
    console.log('--- openDialog called ---');
    
    // 重置檔案選擇
    selectedFile.value = null;
    
    if (formRef.value) {
        formRef.value.resetFields();
    }
    
    Object.keys(form).forEach(key => form[key] = null);
    
    if (mode === 'add') {
        isEditMode.value = false;
        dialogTitle.value = '新增產品';
        form.categoryId = null;
        form.imageUrl = ''; // 清空圖片 URL
    } else if (mode === 'edit' && productData) {
        isEditMode.value = true;
        dialogTitle.value = '修改產品';
        
        form.productId = productData.productId;
        form.productName = productData.productName || '';
        form.price = productData.price || null;
        form.description = productData.description || '';
        form.imageUrl = productData.imageUrl || '';
        form.stock = productData.stock || null;

        if (productData.categoryId !== undefined && productData.categoryId !== null) {
            form.categoryId = productData.categoryId;
        } else if (productData.category && productData.category.categoryId !== undefined) {
            form.categoryId = productData.category.categoryId;
        } else {
            form.categoryId = null;
        }
    }
    
    dialogVisible.value = true;
};


const handleImageChange = (file) => {
  const isJPGPNG = file.raw.type === 'image/jpeg' || file.raw.type === 'image/png';
  const isLt2M = file.raw.size / 1024 / 1024 < 2;

  if (!isJPGPNG) {
    ElMessage.error('圖片只能是 JPG/PNG 格式!');
    uploadRef.value.clearFiles();
    form.imageUrl = '';
    selectedFile.value = null;
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('圖片大小不能超過 2MB!');
    uploadRef.value.clearFiles();
    form.imageUrl = '';
    selectedFile.value = null;
    return false;
  }
  
  // 儲存檔案
  selectedFile.value = file.raw;
  // 產生本地預覽
  form.imageUrl = URL.createObjectURL(file.raw);
  return true;
};

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let finalImageUrl = form.imageUrl;
        
        // 檢查是否有新選擇的圖片需要上傳
        if (selectedFile.value) {
          console.log('開始上傳圖片...');
          try {
            // 使用 productService 上傳圖片
            const uploadedUrl = await uploadImageService(selectedFile.value);
            console.log('圖片上傳成功，URL:', uploadedUrl);
            finalImageUrl = uploadedUrl;
          } catch (uploadError) {
            console.error('圖片上傳失敗:', uploadError);
            ElMessage.error('圖片上傳失敗，請稍後再試');
            return; // 上傳失敗就不要繼續提交表單
          }
        }
        
        // 準備要提交的資料
        const payload = {
          productName: form.productName,
          categoryId: form.categoryId,
          price: form.price,
          description: form.description,
          imageUrl: finalImageUrl, // 使用上傳後的 URL
          stock: form.stock,
        };

        console.log('提交的產品資料:', payload);

        if (isEditMode.value) {
          await productStore.updateProduct(form.productId, payload);
          ElMessage.success('產品更新成功！');
        } else {
          await productStore.createNewProduct(payload);
          ElMessage.success('產品新增成功！');
        }
        
        // 重置檔案選擇
        selectedFile.value = null;
        dialogVisible.value = false;
        
        // 重新載入產品列表
        productStore.fetchProducts({
          ...searchParams.value,
          page: currentPage.value,
          size: 20
        });
        
      } catch (err) {
        console.error('提交失敗:', err);
        ElMessage.error('操作失敗：' + (err.response?.data?.message || err.message));
      }
    } else {
      ElMessage.error('表單驗證失敗，請檢查輸入內容！');
    }
  });
};

const handleDeleteProduct = async (productId) => {
    if (confirm('確定要下架此產品嗎？')) {
        try {
            await productStore.updateProduct(productId, { isAvailable: false });
            ElMessage.success('產品已成功下架！');
            
            productStore.fetchProducts({
                ...searchParams.value,
                page: currentPage.value,
                size: 20
            });
        } catch (err) {
            ElMessage.error('下架失敗：' + (err.response?.data?.message || err.message));
        }
    }
};

const openAddCategoryDialog = () => {
  console.log('AdminProductListPage.vue: openAddCategoryDialog 函數已觸發！'); 
  categoryDialogVisible.value = true;
  newCategoryName.value = ''; // 清空輸入欄位
  nextTick(() => {
    if (categoryFormRef.value) {
      categoryFormRef.value.resetFields(); // 重設表單驗證狀態
    }
  });
};

const submitAddCategory = () => {
  categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await productStore.createNewCategory(newCategoryName.value);
        ElMessage.success(`分類 "${newCategoryName.value}" 新增成功！`);
        categoryDialogVisible.value = false;
      } catch (err) {
        console.error('新增分類失敗:', err);
        ElMessage.error('新增分類失敗：' + (err.response?.data?.message || err.message));
      }
    } else {
      ElMessage.error('請輸入有效的分類名稱！');
    }
  });
};

onMounted(() => {
  console.log('ProductListPage.vue mounted.'); 
  productStore.fetchProducts({ page: 0, size: 20, sortBy: 'creationTime', direction: 'desc' });
  console.log('ProductListPage.vue mounted, attempting to fetch categories...'); 
  productStore.fetchCategories(); 
});

watch(currentPage, (newPage) => {
  searchParams.value.page = newPage;
}, { immediate: true });

watch(() => isRef(categories) ? categories.value : undefined, (newValue) => {
  console.log('Watcher: categories (from storeToRefs) changed to:', newValue); 
}, { deep: true });
</script>

<style scoped>
.product-list-page {
  padding: 20px;
}
.action-bar {
  display: flex;
  justify-content: space-between; /* 將左右兩側內容推開 */
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap; /* 允許在小螢幕上換行 */
  gap: 15px; /* 主容器內元素之間的最小間距 */
}
.search-and-filter {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap; /* 允許搜尋和篩選元件內部換行 */
}
.search-input {
  width: 250px;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ccc;
}
.price-filter {
  display: flex;
  align-items: center;
  gap: 5px;
}
.price-filter input {
  width: 80px;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

/* 統一按鈕的基礎樣式，並用一個 class 應用 */
.action-bar-button {
  padding: 8px 16px;
  border-radius: 4px;
  border: 1px solid #007bff;
  background-color: #007bff;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s, border-color 0.3s;
  white-space: nowrap; /* 防止按鈕文字換行 */
}
.action-bar-button:hover {
  background-color: #0056b3;
  border-color: #0056b3;
}

/* 針對新增分類按鈕的特定樣式 */
.add-category-button {
  background-color: #28a745; /* 綠色 */
  border-color: #28a745;
}
.add-category-button:hover {
  background-color: #218838;
  border-color: #218838;
}

/* 右側按鈕群組 */
.button-group {
  display: flex;
  align-items: center;
  gap: 10px; /* 按鈕之間的間距 */
  flex-wrap: wrap; /* 允許按鈕群組內部換行 */
}

/* flex-grow 用於推開內容 */
.flex-grow {
  flex-grow: 1;
}

/* 原有的 .sort-bar 及其相關樣式已移除 */
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
  height: 360px; 
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
.product-actions button {
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #ccc;
  background-color: #f0f0f0;
  color: #333;
  cursor: pointer;
  transition: background-color 0.3s;
}
.product-actions button:hover {
  background-color: #e0e0e0;
}
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}
.page-select-container {
    display: flex;
    align-items: center;
    gap: 5px;
}
.pagination button {
  padding: 8px 16px;
  border-radius: 4px;
  border: 1px solid #007bff;
  background-color: #007bff;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s, border-color 0.3s;
}
.pagination button:disabled {
  background-color: #cccccc;
  border-color: #cccccc;
  cursor: not-allowed;
}
.image-preview img {
  max-width: 100%;
  max-height: 200px; 
  object-fit: contain;
  margin-top: 10px;
  border-radius: 4px;
  border: 1px solid #eee;
}
.el-upload__tip {
  font-size: 0.8em;
  color: #909399;
  line-height: 1.5;
}
.error-message {
  color: red;
  font-size: 0.9em;
  margin-top: 5px;
}
</style>
