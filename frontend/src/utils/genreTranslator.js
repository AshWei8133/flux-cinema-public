/**
 * 一個簡單的前端工具，用來將簡體中文的電影類型名稱，翻譯成繁體中文。
 */

// 1. 建立我們的「簡繁對照翻譯字典」
// Map 是一種在 JavaScript 中效能很好的字典格式
const translationMap = new Map([
    ["动作", "動作"],
    ["冒险", "冒險"],
    ["动画", "動畫"],
    ["喜剧", "喜劇"],
    ["犯罪", "犯罪"],
    ["纪录", "紀錄"],
    ["剧情", "劇情"],
    ["家庭", "家庭"],
    ["奇幻", "奇幻"],
    ["历史", "歷史"],
    ["恐怖", "恐怖"],
    ["音乐", "音樂"],
    ["悬疑", "懸疑"],
    ["爱情", "愛情"],
    ["科幻", "科幻"],
    ["惊悚", "驚悚"],
    ["战争", "戰爭"],
]);

/**
 * 執行翻譯的函式
 * @param {string} simplifiedName 簡體中文的類型名稱
 * @returns {string} 對應的繁體中文名稱，如果字典裡找不到，就回傳原本的簡體名稱
 */
export function translateGenre(simplifiedName) {
    // 2. 使用 translationMap.get() 來查詢字典
    //    如果找到了，就回傳翻譯結果；如果找不到，就回傳原本的文字
    return translationMap.get(simplifiedName) || simplifiedName;
}
