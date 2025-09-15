import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import eventCategoryService from '@/features/Event/services/eventCategoryService'

export const useEventCategoryStore = defineStore('eventCategory', () => {
  const eventCategory = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  // ✅ 提供 alias，讓元件用 categories
  const categories = computed(() => eventCategory.value || [])

  async function fetchCategories() {
    isLoading.value = true
    error.value = null
    try {
      const response = await eventCategoryService.getAll()
      eventCategory.value = response
    } catch (err) {
      error.value = '無法載入活動類型資料，請稍後再試。'
    } finally {
      isLoading.value = false
    }
  }

  async function createEventCategory(categoryData) {
    isLoading.value = true
    error.value = null
    try {
      await eventCategoryService.create(categoryData)
      await fetchCategories()
    } catch (err) {
      error.value = err.response?.data?.message || '新增失敗，請檢查類型名稱是否重複。'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function updateEventCategory(eventCategoryId, updatedData) {
    isLoading.value = true
    error.value = null
    try {
      await eventCategoryService.update(eventCategoryId, updatedData)
      await fetchCategories()
    } catch (err) {
      error.value = err.response?.data?.message || '更新失敗，請稍後再試。'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function deleteEventCategory(eventCategoryId) {
    isLoading.value = true
    error.value = null
    try {
      await eventCategoryService.delete(eventCategoryId)
      await fetchCategories()
    } catch (err) {
      error.value = err.response?.data?.message || '刪除失敗，可能已有活動正在使用此類型。'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  return {
    // state
    eventCategory,
    categories, // ✅ 新增
    isLoading,
    error,
    // actions
    fetchCategories,
    createEventCategory,
    updateEventCategory,
    deleteEventCategory,
  }
})
