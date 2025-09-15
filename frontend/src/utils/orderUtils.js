/**
 * 根據訂單狀態返回對應的 Element Plus Tag 顏色類型
 * @param {string} status - 訂單狀態字串
 * @returns {string} Element Plus 的 Tag 類型 ('success', 'warning', 'info', '')
 */
export const getStatusTagType = (status) => {
    if (status === '已付款') return 'success';
    if (status === '待付款') return 'warning';
    if (status === '已取消') return 'info';
    return '';
};