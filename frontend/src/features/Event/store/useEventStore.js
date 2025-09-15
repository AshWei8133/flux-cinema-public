import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import eventService from '@/features/Event/services/eventService'

export const useEventStore = defineStore('event', () => {
  // --- State (狀態) ---
  const events = ref({ count: 0, list: [] })
  const currentEvent = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // --- Getters (計算屬性) ---
  const eventCount = computed(() => events.value.count)

  // --- Actions (動作) ---
  /**
   * 獲取活動列表
   * @param {object} params - 包含查詢條件的物件
   */
  async function fetchEvents(params = {}) {
    loading.value = true
    error.value = null
    try {
      const response = await eventService.getAllEvents(params)
      events.value = response
    } catch (err) {
      error.value = '獲取活動列表失敗！'
      events.value = { count: 0, list: [] }
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 根據 ID 獲取單一活動
   */
  async function fetchEventById(eventId) {
    loading.value = true
    error.value = null
    try {
      const response = await eventService.getEventById(eventId)
      currentEvent.value = response
    } catch (err) {
      error.value = `獲取活動 (ID: ${eventId}) 失敗！`
      currentEvent.value = null
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 新增活動
   */
  async function addEvent(eventData) {
    loading.value = true
    error.value = null
    try {
      await eventService.createEvent(eventData)
      // 在您的原始邏輯中，新增和更新後會重新 fetchEvents()
      // 這裡我們暫時遵循這種模式，呼叫一個不帶參數的 fetchEvents
      await fetchEvents()
    } catch (err) {
      error.value = '新增活動失敗！'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新現有活動
   */
  async function updateExistingEvent(eventId, updatedData) {
    loading.value = true
    error.value = null
    try {
      await eventService.updateEvent(eventId, updatedData)
      await fetchEvents()
    } catch (err) {
      error.value = '更新活動失敗！'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * 刪除活動
   */
  async function deleteEventById(eventId) {
    await eventService.deleteEvent(eventId)
  }

  function clearCurrentEvent() {
    currentEvent.value = null
  }

  return {
    events,
    currentEvent,
    loading,
    error,
    eventCount,
    fetchEvents,
    fetchEventById,
    addEvent,
    updateExistingEvent,
    deleteEventById,
    clearCurrentEvent,
  }
})