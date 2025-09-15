// 這個檔案應該放在: src/features/Product/services/publicCouponService.js

import httpClient from '@/services/api';

/**
 * 取得會員未使用的優惠券列表
 * 對應後端 API: GET /api/public/order/coupon
 * @returns {Promise<Array>} 優惠券列表
 */
async function getUnusedCoupons() {
    try {
        const response = await httpClient.get('/public/order/coupon');
        return response;
    } catch (error) {
        console.error('取得優惠券列表失敗:', error);
        throw error;
    }
}

// 前端計算折扣金額的輔助函數
function calculateDiscountAmount(coupon, orderAmount) {
    if (!coupon || orderAmount < coupon.minimumSpend) {
        return 0;
    }
    
    if (coupon.discountType === 'FIXED') {
        // 固定金額折扣
        return Math.min(coupon.discountAmount, orderAmount);
    } else if (coupon.discountType === 'PERCENTAGE') {
        // 百分比折扣
        const percent = coupon.discountAmount / 100;
        return Math.round(orderAmount * percent);
    }
    
    return 0;
}

// 前端驗證優惠券是否可用
function validateCoupon(coupon, orderAmount, expiryDate = null) {
    const validation = {
        isValid: true,
        message: ''
    };
    
    // 檢查最低消費
    if (orderAmount < coupon.minimumSpend) {
        validation.isValid = false;
        validation.message = `此優惠券需要最低消費 NT$ ${coupon.minimumSpend}`;
        return validation;
    }
    
    // 檢查有效期限
    if (expiryDate) {
        const expiry = new Date(expiryDate);
        const today = new Date();
        if (expiry < today) {
            validation.isValid = false;
            validation.message = '此優惠券已過期';
            return validation;
        }
    }
    
    validation.message = '優惠券可以使用';
    return validation;
}

export default {
    getUnusedCoupons,
    calculateDiscountAmount,
    validateCoupon
};