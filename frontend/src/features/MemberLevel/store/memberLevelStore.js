import { defineStore } from 'pinia'
import { ref } from 'vue'
import MemberLevelService from '../services/memberLevelService'
import { ElMessage } from 'element-plus'

export const useMemberLevelStore = defineStore('memberLevel', () => {
  // 狀態
  const levels = ref([]) // 會員等級列表
  const isLoading = ref(false)
  const error = ref(null)

  // 方法
  // 取得全部會員等級
  // 取得所有等級
  async function fetchLevels() {
    isLoading.value = true;
    error.value = null;
    try {
      const data = await MemberLevelService.getMemberLevels();
      levels.value = data;
    } catch (err) {
      error.value = '無法獲取會員等級列表';
      console.error(err);
      ElMessage.error(error.value);
    } finally {
      isLoading.value = false;
    }
  }

  // 新增等級
  async function createLevel(levelData, levelIconFile) {
    isLoading.value = true;
    error.value = null;
    try {
      await MemberLevelService.createMemberLevel(levelData, levelIconFile);
      ElMessage.success('會員等級新增成功！');
      await fetchLevels();// 成功後刷新列表
    } catch (err) {
      error.value = '新增會員等級失敗';
      console.error(err);
      ElMessage.error(error.value);
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  // 更新等級
  async function updateLevel(id, levelData, levelIconFile) {
    isLoading.value = true;
    error.value = null;
    try {
      await MemberLevelService.updateMemberLevel(id, levelData, levelIconFile);
      ElMessage.success('會員等級更新成功！');
      await fetchLevels();
    } catch (err) {
      error.value = '更新會員等級失敗';
      console.error(err);
      ElMessage.error(error.value);
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  // 刪除等級
  async function deleteLevel(id) {
    isLoading.value = true;
    error.value = null;
    try {
      await MemberLevelService.deleteMemberLevel(id);
      ElMessage.success('會員等級刪除成功！');
      await fetchLevels();
    } catch (err) {
      error.value = '刪除會員等級失敗';
      console.error(err);
      ElMessage.error(error.value);
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  return {
    levels,
    isLoading,
    error,
    fetchLevels,
    createLevel,
    updateLevel,
    deleteLevel,
  }
})
