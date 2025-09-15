import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUiStore = defineStore('ui', () => {
    // 狀態：用來存放滾動目標的 CSS 選擇器
    const scrollTarget = ref(null);

    /**
     * 動作：設定滾動目標
     * @param {string | null} selector - 目標元素的 CSS 選擇器
     */
    function setScrollTarget(selector) {
        scrollTarget.value = selector;
    }

    return {
        scrollTarget,
        setScrollTarget
    };
});