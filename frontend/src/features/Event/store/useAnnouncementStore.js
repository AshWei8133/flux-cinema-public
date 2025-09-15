import { defineStore } from 'pinia'
import { ref } from 'vue'
import announcementService from '@/features/Event/services/announcementService' // 確保路徑正確

/**
 * 管理公告的 Pinia Store
 */
export const useAnnouncementStore = defineStore('announcement', () => {
  // --- State (狀態) ---
  // 存放公告列表和總數，預期後端返回 { count: number, list: [] } 這樣的結構
  const announcements = ref({ count: 0, list: [] })
  // 存放當前選中的單一公告詳情
  const currentAnnouncement = ref(null) // <-- 重新加入此狀態
  // 載入狀態，用於 UI 顯示載入指示器
  const loading = ref(false)
  // 錯誤訊息，用於顯示給使用者
  const error = ref(null)

  // 排序狀態，由 AdminAnnouncement.vue 中的 default-sort 設定，預設為按發布日期降序
  const sortBy = ref('publishDate')
  const sortOrder = ref('desc') // 'asc' (升序) 或 'desc' (降序)

  // --- Actions (動作) ---

  /**
   * 獲取公告列表，支援查詢、排序和分頁。
   * @param {object} params - 包含查詢條件的物件。
   * 例如：{ title: '標題關鍵字', sortBy: 'publishDate', sortOrder: 'desc', page: 1, size: 10 }
   */
  async function fetchAnnouncements(params = {}) {
    loading.value = true
    error.value = null
    try {
      console.log('傳遞給後端的公告篩選參數:', params)
      // 呼叫服務層的方法，傳遞所有查詢參數
      const response = await announcementService.getAllAnnouncements(params)
      announcements.value = response // 假設 response 包含 { count, list }
    } catch (err) {
      console.error('Store 獲取公告列表錯誤:', err)
      error.value = err.message || '獲取公告列表失敗！'
      throw err // 將錯誤重新拋出，讓元件可以捕獲
    } finally {
      loading.value = false
    }
  }

  /**
   * 根據 ID 獲取單一公告詳情。
   * @param {number} announcementId - 公告 ID
   */
  async function fetchAnnouncementById(announcementId) {
    // <-- 重新加入此動作
    loading.value = true
    error.value = null
    try {
      const response = await announcementService.getAnnouncementById(announcementId)
      currentAnnouncement.value = response
    } catch (err) {
      console.error('Store 獲取單一公告錯誤:', err)
      error.value = err.message || `獲取公告 (ID: ${announcementId}) 失敗！`
      currentAnnouncement.value = null
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 新增公告
   * @param {object} announcementData - 新公告的資料
   */
  async function addAnnouncement(announcementData) {
    // <-- 重新加入此動作
    loading.value = true
    error.value = null
    try {
      await announcementService.createAnnouncement(announcementData)
      await fetchAnnouncements() // 新增成功後刷新列表
    } catch (err) {
      console.error('Store 新增公告錯誤:', err)
      error.value = err.message || '新增公告失敗！'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新現有公告
   * @param {number} announcementId - 公告 ID
   * @param {object} updatedData - 更新的資料
   */
  async function updateExistingAnnouncement(announcementId, updatedData) {
    // <-- 重新加入此動作
    loading.value = true
    error.value = null
    try {
      await announcementService.updateAnnouncement(announcementId, updatedData)
      await fetchAnnouncements() // 更新成功後刷新列表
    } catch (err) {
      console.error('Store 更新公告錯誤:', err)
      error.value = err.message || '更新公告失敗！'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 根據 ID 刪除一筆公告。
   * @param {number} announcementId - 公告 ID。
   */
  async function deleteAnnouncement(announcementId) {
    loading.value = true
    error.value = null
    try {
      await announcementService.deleteAnnouncement(announcementId)
      await fetchAnnouncements({
        title: '',
        sortBy: sortBy.value,
        sortOrder: sortOrder.value,
        page:
          announcements.value.list.length === 1 && announcements.value.count > 1
            ? Math.max(1, announcements.value.page - 1)
            : announcements.value.page,
        size: announcements.value.size,
      })
    } catch (err) {
      console.error('Store 刪除公告錯誤:', err)
      error.value = err.message || '刪除公告失敗！'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 清除當前選中的公告資料
   */
  function clearCurrentAnnouncement() {
    // <-- 重新加入此動作
    currentAnnouncement.value = null
  }

  // --- Return ---
  // 將所有需要給外部元件使用的 state 和 actions 在此導出
  return {
    announcements,
    currentAnnouncement, // <-- 重新導出此狀態
    loading,
    error,
    sortBy,
    sortOrder,
    fetchAnnouncements,
    fetchAnnouncementById, // <-- 重新導出此動作
    addAnnouncement, // <-- 重新導出此動作
    updateExistingAnnouncement, // <-- 重新導出此動作
    deleteAnnouncement,
    clearCurrentAnnouncement, // <-- 重新導出此動作
  }
})
