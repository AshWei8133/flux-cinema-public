import httpClient from '@/services/api';
/**
 * 取得購物車內所有項目。
 * @returns {Promise<Object>} 包含購物車項目清單的回應物件。
 */
async function getCartItems() {
    try {
        const response = await httpClient.get('/public/cart');
        return response;
    } catch (error) {
        console.error('取得購物車項目失敗:', error);
        throw error;
    }
}
/**
 * 將產品加入購物車。
 * @param {number} productId - 產品的唯一ID。
 * @param {number} quantity - 要加入的數量。
 * @returns {Promise<Object>} 包含更新後購物車項目的回應物件。
 */
async function addToCart(productId, quantity) {
    try {
        const response = await httpClient.post('/public/cart/add', null, {
            params: { productId, quantity }
        });
        return response;
    } catch (error) {
        console.error('加入購物車失敗:', error);
        throw error;
    }
}
/**
 * 更新購物車項目的數量（依據購物車項目ID）。
 * @param {number} cartItemId - 購物車項目的唯一ID。
 * @param {number} newQuantity - 新的數量。
 * @returns {Promise<Object>} 包含更新後購物車項目的回應物件。
 */
async function updateCartItem(cartItemId, newQuantity) {
    try {
        const response = await httpClient.put('/public/cart/update', null, {
            params: { cartItemId, newQuantity }
        });
        return response;
    } catch (error) {
        console.error('更新購物車項目失敗:', error);
        throw error;
    }
}
/**
 * 從購物車中移除指定項目。
 * @param {number} cartItemId - 要移除的購物車項目的唯一ID。
 * @returns {Promise<Object>} 空的回應物件。
 */
async function removeCartItem(cartItemId) {
    try {
        const response = await httpClient.delete('/public/cart/remove', {
            params: { cartItemId }
        });
        return response;
    } catch (error) {
        console.error('移除購物車項目失敗:', error);
        throw error;
    }
}
/**
 * 清空整個購物車。
 * @returns {Promise<Object>} 空的回應物件。
 */
async function clearCart() {
    try {
        const response = await httpClient.delete('/public/cart/clear');
        return response;
    } catch (error) {
        console.error('清空購物車失敗:', error);
        throw error;
    }
}
/**
 * 從購物車中減少產品數量。
 * @param {number} productId - 產品的唯一ID。
 * @param {number} quantity - 要減少的數量。
 * @returns {Promise<Object>} 包含更新後購物車項目的回應物件。
 */
async function minusFromCart(productId, quantity) {
    try {
        const response = await httpClient.put('/public/cart/minus', null, {
            params: { productId, quantity }
        });
        return response;
    } catch (error) {
        console.error('從購物車減少數量失敗:', error);
        throw error;
    }
}
export {
    getCartItems,
    addToCart,
    updateCartItem,
    removeCartItem,
    clearCart,
    minusFromCart
};