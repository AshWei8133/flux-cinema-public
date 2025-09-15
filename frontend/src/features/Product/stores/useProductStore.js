import { defineStore } from "pinia";
import { ref, computed } from "vue";
import {
    getProducts,
    getProductById,
    createProduct,
    updateProduct as updateProductService, // 重新命名，避免與 store 方法衝突
    deleteProduct,
    getUnavailableProducts,
    getProductsNeedingRestock,
    uploadImage,
    createCategory,
    getCategories
} from '../services/productService.js';

export const useProductStore = defineStore('product', () => {

    // --- 狀態 (State) ---
    const products = ref([]);
    const totalProducts = ref(0);
    const currentPage = ref(0);
    const totalPages = ref(0);
    const selectedProduct = ref(null);
    const restockProducts = ref([]);
    const isLoading = ref(false);
    const error = ref(null);
    const categories = ref([]);

    // --- 計算屬性 (Getters) ---
    const hasProducts = computed(() => products.value.length > 0);
    
    // --- 方法 (Actions) ---
    const fetchProducts = async (params = {}) => {
        isLoading.value = true;
        error.value = null;
        try {
            const { page = 0, size = 20, keyword = '', minPrice, maxPrice, sortBy = 'creationTime', direction = 'desc' } = params;
            const response = await getProducts({ page, size, keyword, minPrice, maxPrice, sortBy, direction });
            products.value = response.content;
            totalProducts.value = response.totalElements;
            totalPages.value = response.totalPages;
            if (response && response.pageable) {
                currentPage.value = response.pageable.pageNumber;
            } else {
                currentPage.value = params.page || 0;
            }
        } catch (err) {
            error.value = err.message || '獲取產品列表失敗';
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };

    const fetchInactiveProducts = async () => {
        isLoading.value = true;
        error.value = null;
        try {
            const inactiveProducts = await getUnavailableProducts(); 
            products.value = inactiveProducts; 
            totalPages.value = 1;
            currentPage.value = 0;
        } catch (err) {
            error.value = err.message || '獲取已下架產品列表失敗';
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };
    
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

    const createNewProduct = async (productData) => {
        isLoading.value = true;
        error.value = null;
        try {
            const newProduct = await createProduct(productData);
            await fetchProducts({ page: 0 });
            return newProduct;
        } catch (err) {
            error.value = err.message || '新增產品失敗';
            console.error('API Error: ', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    const deleteProductFromStore = async (id) => {
        isLoading.value = true;
        error.value = null;
        try {
            await deleteProduct(id);
            const index = products.value.findIndex(p => p.productId === id);
            if (index !== -1) {
                products.value.splice(index, 1);
            }
        } catch (err) {
            error.value = err.message || '刪除產品失敗';
            console.error('API Error: ', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    };
    
    const fetchRestockProducts = async () => {
        isLoading.value = true;
        error.value = null;
        try {
            const fetchedProducts = await getProductsNeedingRestock();
            restockProducts.value = fetchedProducts;
        } catch (err) {
            error.value = err.message || '獲取需補貨產品列表失敗';
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };

    const updateProduct = async (id, productData) => {
        isLoading.value = true;
        error.value = null;
        try {
            const updatedProduct = await updateProductService(id, productData);
            return updatedProduct;
        } catch (err) {
            error.value = err.message || '更新產品失敗';
            console.error('API Error: ', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    const fetchCategories = async () => {
        isLoading.value = true; 
        error.value = null;     
        try {
            const fetchedCategories = await getCategories();
            // --- 關鍵點：確保這裡直接賦值，不需額外轉換 ---
            categories.value = fetchedCategories; 
            console.log('Pinia Store: Categories fetched successfully:', categories.value); 
        } catch (err) {
            console.error('Pinia Store: Failed to fetch categories:', err); 
            error.value = '無法載入產品分類。'; 
            categories.value = []; 
        } finally {
            isLoading.value = false; 
        }
    };

    // *** 修正：將 createNewCategory 移出 fetchCategories 函數 ***
    const createNewCategory = async (categoryName) => { 
        isLoading.value = true;
        error.value = null;
        try {
            await createCategory(categoryName); // 呼叫 service 函數
            // 成功創建後，重新獲取所有分類以更新列表
            await fetchCategories(); 
        } catch (err) {
            console.error('Pinia Store: Failed to create new category:', err);
            error.value = '新增產品分類失敗。';
            throw err; 
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
        restockProducts,
        isLoading,
        error,
        categories,
        hasProducts,
        fetchProducts,
        fetchInactiveProducts,
        fetchProductById,
        createNewCategory, // 確保這裡正確導出
        createNewProduct,
        deleteProduct: deleteProductFromStore,
        fetchRestockProducts,
        updateProduct,
        uploadImage,
        // createCategory, // 這個通常不需要導出，因為它由 createNewCategory 內部使用
        fetchCategories,
    };
});
