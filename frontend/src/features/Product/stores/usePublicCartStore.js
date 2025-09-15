import { defineStore } from "pinia";
import { ref, computed } from "vue";
import {
    getCartItems,
    addToCart,
    minusFromCart,
    removeCartItem,
    clearCart,
    updateCartItem
} from '../services/publicCartService.js';

export const usePublicCartStore = defineStore('publicCart', () => {

    // --- 狀態 (State) ---
    const cartItems = ref([]);
    const isLoading = ref(false);
    const error = ref(null);

    // --- 計算屬性 (Getters) ---
    const hasItems = computed(() => Array.isArray(cartItems.value) && cartItems.value.length > 0);

    const cartItemSubtotal = (item) => {
        const price = Number(item.productPrice) || 0;
        const qty = Number(item.quantity) || 0;
        return price * qty;
    };

    const cartTotalPrice = computed(() => {
        return cartItems.value.reduce((total, item) => total + cartItemSubtotal(item), 0);
    });

    // --- 方法 (Actions) ---
    const fetchCartItems = async () => {
        isLoading.value = true;
        error.value = null;
        try {
            // 從服務獲取資料
            const data = await getCartItems();
            
            // 偵錯輔助：印出從 API 接收到的原始資料
            console.log('API Received Data:', data);
            
            // 使用可選鏈接 (Optional Chaining) 確保資料存在
            const items = data?.items || [];
            
            cartItems.value = Array.isArray(items)
                ? items
                    .filter(Boolean)
                    .map(item => ({
                        ...item,
                        quantity: item?.quantity ?? 1,
                        imageUrl: item?.imageUrl ?? 'http://res.cloudinary.com/dg98ebu8y/image/upload/v1755571478/smgkomzsedilrlhjvjgq.jpg'
                    }))
                : [];
        } catch (err) {
            error.value = err.message || '獲取購物車項目失敗';
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };

    const addProductToCart = async (productId, quantity = 1) => {
        isLoading.value = true;
        error.value = null;
        try {
            await addToCart(productId, quantity);
            await fetchCartItems();
        } catch (err) {
            error.value = err.message || '加入購物車失敗';
            console.error('API Error: ', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    const minusProductFromCart = async (productId, quantity = 1) => {
        isLoading.value = true;
        error.value = null;
        try {
            await minusFromCart(productId, quantity);
            await fetchCartItems();
        } catch (err) {
            error.value = err.message || '減少商品數量失敗';
            console.error('API Error: ', err);
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    


    const removeCartItemById = async (cartItemId) => {
        isLoading.value = true;
        error.value = null;
        try {
            await removeCartItem(cartItemId);
            await fetchCartItems();

        } catch (err) {
            if (err.response && err.response.status === 400) {
                error.value = '這個商品已經不在你的購物車中了。';
            } else {
                error.value = err.message || '移除購物車項目失敗';
            }
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };

    const clearTheCart = async () => {
        isLoading.value = true;
        error.value = null;
        try {
            await clearCart();
            cartItems.value = [];
        } catch (err) {
            error.value = err.message || '清空購物車失敗';
            console.error('API Error: ', err);
            throw err;
        } finally {
            isLoading.value = false;
        }

    };

    const updateProductQuantityAction = async (cartItemId, newQuantity) => {
        if (newQuantity < 1) newQuantity = 1;
        isLoading.value = true;
        error.value = null;
        try {
            await updateCartItem(cartItemId, newQuantity);
            // 成功後，重新從後端獲取最新資料
            await fetchCartItems();
            
        } catch (err) {
            error.value = err?.response?.data?.message || err.message || '更新數量失敗';
            console.error('API Error: ', err);
        } finally {
            isLoading.value = false;
        }
    };


    return {
        cartItems,
        isLoading,
        error,
        hasItems,
        cartItemSubtotal,
        cartTotalPrice,
        fetchCartItems,
        addProductToCart,
        minusProductFromCart,
        removeCartItemById,
        clearTheCart,
        updateProductQuantityAction
    };
});