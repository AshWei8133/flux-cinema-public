import { defineStore } from 'pinia';
import { ref } from 'vue';
import MemberLevelService from '../services/memberLevelService';

export const useMemberLevelStatusStore = defineStore('memberLevelStatus', () => {
  // --- State ---
  const status = ref(null); // 將用來儲存後端回傳的 MemberLevelStatusDTO
  const isLoading = ref(false);
  const error = ref(null);

  // --- Actions ---
  async function fetchStatus() {
    isLoading.value = true;
    error.value = null;
    try {
      const response = await MemberLevelService.fetchMemberLevelStatus();
      status.value = response;
    } catch (err) {
      error.value = '無法獲取會員等級狀態';
      console.error(err);
      // 可以考慮在這裡拋出錯誤或用 ElMessage 提示
    } finally {
      isLoading.value = false;
    }
  }

  async function updateStatus() {
    isLoading.value = true;
    error.value = null;
    try {
      await MemberLevelService.updateMemberLevelStatus();
    } catch (err) {
      error.value = '無法更新會員等級狀態';
      console.error(err);
    } finally {
      isLoading.value = false;
    }
  }

  return {
    status,
    isLoading,
    error,
    fetchStatus,
    updateStatus,
  };
})