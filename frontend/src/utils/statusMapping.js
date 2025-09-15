/**
 * @fileoverview
 * 訂單狀態管理中心 (Order Status Mapping)
 * * 這個檔案是前端處理訂單狀態的「單一事實來源」。
 * 後端使用 Enum (例如 'PENDING', 'PAID') 來確保資料一致性，
 * 前端則透過這個檔案，將這些 Enum 值「翻譯」成使用者看得懂的中文文字，
 * 以及對應的 UI 樣式 (例如 Element Plus 的 tag type)。
 * * 好處：
 * 1. 集中管理：未來需要修改狀態文字或顏色，只需修改此檔案一處。
 * 2. 避免魔法字串：程式碼中不再出現 '已付款', 'success' 等硬코딩字串。
 * 3. 易於擴充：新增狀態時，只需在此檔案中增加一個條目。
 */

// 1. 定義訂單狀態的完整映射表
// Key: 後端 OrderStatus Enum 的名稱 (大寫)
// Value: 一個包含顯示文字(text)和對應 Element Plus 標籤類型(tagType)的物件
export const ORDER_STATUS_MAP = {
    PENDING: { text: '待付款', tagType: 'warning' },
    PAID: { text: '已付款', tagType: 'success' },
    CANCELLED: { text: '已取消', tagType: 'info' },
    REFUNDED: { text: '已退款', tagType: 'danger' },
    // COMPLETED: { text: '已完成', tagType: '' }, // 預設樣式
    // 如果未來有更多狀態，直接在這裡新增即可
};

// 2. 提供一個給 <el-select> 使用的選項列表
// 這樣篩選器中的選項就可以動態生成，而不需要 hardcode
export const ORDER_STATUS_OPTIONS = Object.keys(ORDER_STATUS_MAP).map(key => ({
    value: key,
    label: ORDER_STATUS_MAP[key].text,
}));


// 3. 導出一個通用的輔助函式，方便在各個元件中呼叫
/**
 * 根據後端傳來的狀態字串，獲取對應的顯示資訊
 * @param {string} status - 後端傳來的 Enum 名稱 (e.g., 'PENDING')
 * @returns {{text: string, tagType: string}} - 包含文字和樣式的物件
 */
export function getOrderStatusInfo(status) {
    // 如果傳入的 status 存在於我們的映射表中，就返回對應的資訊
    if (status && ORDER_STATUS_MAP[status]) {
        return ORDER_STATUS_MAP[status];
    }
    // 如果傳入一個未知的 status，或者 status 為 null/undefined，
    // 則返回一個預設的未知狀態，避免頁面因讀取 undefined 的屬性而崩潰。
    return { text: '未知狀態', tagType: 'info' };
}