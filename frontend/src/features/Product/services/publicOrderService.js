import httpClient from '@/services/api';

const publicOrderService = {
  /**
   * 建立新訂單
   * @param {Object} createOrderDTO - 訂單資料
   * @returns {Promise<Object>} 回傳建立訂單的結果
   */
  async createOrder(createOrderDTO) {
  try {
    const response = await httpClient.post('/public/order/create', createOrderDTO);
    return response.data;
  } catch (error) {
    const message = error.response?.data?.message || error.message || '訂單建立失敗';
    throw new Error(message);
  }
}
};

export default publicOrderService;