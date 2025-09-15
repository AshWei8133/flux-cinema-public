import { defineStore } from "pinia";
import { ref, computed } from "vue";
import {
    getProducts,
    getProductsByCategoryName,
    getProductsByPriceBetween,
    getProductById,
    getProductsByCategoryId 
} from '../services/publicProductService.js';

export const usePublicProductStore = defineStore('publicProduct', () => {

    // --- 狀態 (State) ---
    const products = ref([]);
    const totalProducts = ref(0);
    const currentPage = ref(0);
    const totalPages = ref(0);
    const selectedProduct = ref(null);
    const isLoading = ref(false);
    const error = ref(null);

    // --- 計算屬性 (Getters) ---
    const hasProducts = computed(() => products.value.length > 0);
    
    // --- 方法 (Actions) ---

    /**
     * 取得產品清單，可支援分頁、排序、關鍵字與價格區間篩選。
     * @param {Object} params - 包含查詢參數的物件。
     * @param {number} [params.page=0] - 頁碼。
     * @param {number} [params.size=10] - 每頁項目數。
     * @param {string} [params.sortBy] - 排序依據的欄位名稱。
     * @param {string} [params.direction] - 排序方向。
     * @param {string} [params.keyword] - 搜尋產品的關鍵字。
     * @param {number} [params.minPrice=0] - 最低價格。
     * @param {number} [params.maxPrice=1000] - 最高價格。
     */
    const fetchProducts = async (params = {}) => {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await getProducts(params);
            products.value = response.content;
            totalProducts.value = response.totalElements;
            totalPages.value = response.totalPages;
            currentPage.value = response.pageable.pageNumber;
        } catch (err) {
            error.value = err.message || '獲取產品列表失敗';
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 根據產品ID取得單一產品的詳細資訊。
     * @param {number} id - 產品的唯一ID。
     */
    const fetchProductById = async (id) => {
        isLoading.value = true;
        error.value = null;
        try {
            selectedProduct.value = await getProductById(id);
        } catch (err) {
            error.value = err.message || `獲取產品 ID=${id} 失敗`;
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };

    const fetchProductsByCategoryId = async (categoryId) => {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await getProductsByCategoryId(categoryId);
            // 根據後端回傳的格式，可能需要調整
            products.value = response.content;
            totalProducts.value = response.totalElements;
            totalPages.value = response.totalPages;
            currentPage.value = response.pageable.pageNumber;
        } catch (err) {
            error.value = err.message || `獲取分類 ID=${categoryId} 的產品失敗`;
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 根據分類名稱取得產品清單。
     * @param {string} categoryName - 要查詢的分類名稱。
     */
    const fetchProductsByCategoryName = async (categoryName) => {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await getProductsByCategoryName(categoryName);
            products.value = response;
            totalPages.value = 1; // 假設這類查詢不分頁
            currentPage.value = 0;
            totalProducts.value = response.length;
        } catch (err) {
            error.value = err.message || `獲取分類 '${categoryName}' 的產品失敗`;
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };
    
    /**
     * 根據價格區間取得產品清單。
     * @param {number} minPrice - 最低價格。
     * @param {number} maxPrice - 最高價格。
     */
    const fetchProductsByPriceBetween = async (minPrice, maxPrice) => {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await getProductsByPriceBetween(minPrice, maxPrice);
            products.value = response;
            totalPages.value = 1;
            currentPage.value = 0;
            totalProducts.value = response.length;
        } catch (err) {
            error.value = err.message || `獲取價格介於 ${minPrice} 到 ${maxPrice} 的產品失敗`;
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };

    // --- 回傳狀態和方法 ---
    return {
        products,
        totalProducts,
        currentPage,
        totalPages,
        selectedProduct,
        isLoading,
        error,
        hasProducts,
        fetchProducts,
        fetchProductById,
        fetchProductsByCategoryName,
        fetchProductsByPriceBetween,
        fetchProductsByCategoryId 
    };
});
