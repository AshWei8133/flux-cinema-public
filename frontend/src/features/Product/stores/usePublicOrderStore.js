import { defineStore } from "pinia";
import { ref } from "vue";
import publicOrderService from '../services/publicOrderService.js';

export const usePublicOrderStore = defineStore('publicOrder', () => {
  // 狀態
  const orders = ref([]);
  const loading = ref(false);
  const error = ref(null);

  // action: 建立訂單
  const createOrder = async (orderData) => {
    loading.value = true;
    error.value = null;
    try {
      const result = await publicOrderService.createOrder(orderData);
      orders.value.push(result); // 可選：把新訂單加到 store
      return result;
    } catch (err) {
      error.value = err.message || '訂單建立失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  return {
    orders,
    loading,
    error,
    createOrder
  };
});