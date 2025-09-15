import httpClient from '@/services/api';

/**
 * 取得產品清單，可支援分頁、排序、關鍵字與價格區間篩選。
 * * @param {Object} params - 包含查詢參數的物件。
 * @param {number} [params.page=0] - 頁碼，從0開始。
 * @param {number} [params.size=10] - 每頁項目數。
 * @param {string} [params.sortBy] - 排序依據的欄位名稱，例如 'price'。
 * @param {string} [params.direction] - 排序方向，'asc' 或 'desc'。
 * @param {string} [params.keyword] - 搜尋產品的關鍵字。
 * @param {number} [params.minPrice=0] - 最低價格。
 * @param {number} [params.maxPrice=1000] - 最高價格。
 * @returns {Promise<Object>} 包含分頁產品資料的回應物件。
 */
async function getProducts(params = {}) {
    try {
        const response = await httpClient.get('/public/product', { params });
        return response;
    } catch (error) {
        console.error('取得產品清單失敗:', error);
        throw error;
    }
}

/**
 * 根據分類名稱取得產品清單。
 * * @param {string} categoryName - 要查詢的分類名稱。
 * @returns {Promise<Object>} 包含產品清單的回應物件。
 */
async function getProductsByCategoryName(categoryName) {
    if (!categoryName) {
        throw new Error('分類名稱不能為空。');
    }
    try {
        const response = await httpClient.get('/public/product/category', { params: { categoryName } });
        return response;
    } catch (error) {
        console.error('根據分類名稱取得產品清單失敗:', error);
        throw error;
    }
}

async function getProductsByCategoryId(categoryId) {
    if (!categoryId) {
        throw new Error('分類ID不能為空。');
    }
    try {
        
        const response = await httpClient.get('/public/product', { params: { categoryId } });
        return response;
    } catch (error) {
        console.error('根據分類ID取得產品清單失敗:', error);
        throw error;
    }
}

/**
 * 根據價格區間取得產品清單。
 * * @param {number} minPrice - 最低價格。
 * @param {number} maxPrice - 最高價格。
 * @returns {Promise<Object>} 包含產品清單的回應物件。
 */
async function getProductsByPriceBetween(minPrice, maxPrice) {
    try {
        const response = await httpClient.get('/public/product/price', { params: { minPrice, maxPrice } });
        return response;
    } catch (error) {
        console.error('根據價格區間取得產品清單失敗:', error);
        throw error;
    }
}

/**
 * 根據產品ID取得單一產品的詳細資訊。
 * * @param {number} id - 產品的唯一ID。
 * @returns {Promise<Object>} 包含產品詳細資料的回應物件。
 */
async function getProductById(id) {
    if (!id) {
        throw new Error('產品ID不能為空。');
    }
    try {
        const response = await httpClient.get(`/public/product/${id}`);
        return response;
    } catch (error) {
        console.error(`取得產品ID ${id} 的詳細資訊失敗:`, error);
        throw error;
    }
}

export {
    getProducts,
    getProductsByCategoryName,
    getProductsByPriceBetween,
    getProductById,
    getProductsByCategoryId 
};
