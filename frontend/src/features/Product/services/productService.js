import httpClient from '@/services/api';
import axios from 'axios';

// const api = axios.create({
//     baseURL: '/api/admin', // 你的所有 API 都在這個路徑下
//     headers: {
//         'Content-Type': 'application/json'
//     }
// });


// 1. 取得所有產品 (分頁、篩選、排序)
async function getProducts(params = {}) {
    // 參數範例: { page: 0, size: 10, sortBy: 'price', direction: 'asc', keyword: '電影' }
    const response = await httpClient.get('/admin/products', { params });
    return response;
}

// 2. 建立新產品
async function createProduct(productData) {
    // productData 必須是一個包含產品資訊的物件
    // 例如: { name: '新電影', description: '描述', price: 100 }
    const response = await httpClient.post('/admin/products', productData);
    return response;
}

// 3. 根據 ID 更新產品
async function updateProduct(productId, updateData) {
    // updateData 是包含要修改的欄位物件
    const response = await httpClient.patch(`/admin/products/${productId}`, updateData);
    return response;
}

// 4. 根據 ID 刪除產品
async function deleteProduct(productId) {
    const response = await httpClient.delete(`/admin/products/${productId}`);
    return response;
}

// 5. 取得單一產品
async function getProductById(productId) {
    const response = await httpClient.get(`/admin/products/${productId}`);
    return response;
}

// 6. 取得需要補貨的產品清單
async function getProductsNeedingRestock() {
    const response = await httpClient.get('/admin/products/restock');
    return response;
}

// 7. 取得指定分類下的產品
async function getProductsByCategoryName(categoryName) {
    const response = await httpClient.get('/admin/products/category', { params: { categoryName } });
    return response;
}

// 8. 取得特定價格區間內的產品
async function getProductsByPriceBetween(minPrice, maxPrice) {
    const response = await httpClient.get('/admin/products/price', { params: { minPrice, maxPrice } });
    return response;
}

// 9. 取得下架的產品清單
async function getUnavailableProducts() {
    const response = await httpClient.get('/admin/products/unavailable');
    return response;
}

// 10. 上傳圖片
async function uploadImage(file) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await httpClient.post('/admin/upload-image', formData, {
        headers: {
            'Content-Type': 'multipart/form-data' // 這是上傳檔案的必要設定
        }
    });
    return response; // 返回圖片的 URL
}

// 11. 建立新分類
async function createCategory(categoryName) {
  if (!categoryName || categoryName.trim() === '') {
    throw new Error('分類名稱不能為空');
  }

  const categoryData = { categoryName: categoryName.trim() };

  try {
    const response = await httpClient.post('/admin/categories/add', categoryData);
    return response.data; // 回傳新增成功的資料
  } catch (err) {
    // 如果後端有返回錯誤訊息，直接取 message
    const msg = err.response?.data?.message || err.message;
    throw new Error(msg);
  }
}

export const getCategories = async () => {
    try {
        console.log('productService.getCategories: Sending GET request to /admin/categories'); 
        const response = await httpClient.get('/admin/products/categories'); // 假設你的後端 API 端點是這個
        return response;
    } catch (error) {
        console.error('Failed to fetch categories:', error);
        throw error;
    }
};






export {
    getProducts,
    createProduct,
    updateProduct,
    deleteProduct,
    getProductById,
    getProductsNeedingRestock,
    getProductsByCategoryName,
    getProductsByPriceBetween,
    getUnavailableProducts,
    uploadImage,
    createCategory
};