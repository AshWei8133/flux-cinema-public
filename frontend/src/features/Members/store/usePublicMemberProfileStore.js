import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import publicMemberProfileService from '../services/publicMemberProfileService'

export const usePublicMemberProfileStore = defineStore('memberProfile', () => {
  const profile = ref(null)
  const isLoading = ref(false)
  const error = ref(null)
  const isUploading = ref(false)
  const isUpdating = ref(false)
  const isChangingPassword = ref(false)

  // --- 【新增】收藏相關狀態 ---
  const favoriteMovieIds = ref(new Set()) // 使用 Set 結構可以快速檢查是否存在，效率更高
  const favorites = ref([]) // 存放完整的收藏電影物件列表
  const isLoadingFavorites = ref(false)

  // --- 【新增】訂票紀錄相關狀態 ---
  const ticketOrderHistory = ref([])
  const isLoadingHistory = ref(false)
  const historyError = ref(null)

  // --- 【新增】商品訂單紀錄相關狀態 ---
  const productOrderHistory = ref([])
  const isLoadingProductHistory = ref(false)
  const productHistoryError = ref(null)

  const fetchProfile = async () => {
    isLoading.value = true
    error.value = null
    try {
      const data = await publicMemberProfileService.getProfile()
      profile.value = data // httpClient 已經返回 response.data
    } catch (err) {
      console.error('Failed to fetch profile in store:', err)
      error.value = err.message || '無法載入您的個人資料，請稍後再試。'
    } finally {
      isLoading.value = false
    }
  }

  const updateAvatar = async (avatarFile) => {
    isUploading.value = true
    error.value = null

    try {
      const memberData = JSON.parse(localStorage.getItem('member_jwt_token'))
      if (!memberData || !memberData.memberInfo) {
        throw new Error('找不到會員資訊，請先登入')
      }
      const memberId = memberData.memberInfo.memberId

      const formData = new FormData()
      formData.append('file', avatarFile)

      await publicMemberProfileService.updateAvatar(memberId, formData)

      // 上傳成功後，重新獲取最新的個人資料
      await fetchProfile()
    } catch (err) {
      console.error('Failed to update avatar in store:', err)
      error.value = err.message || '頭像上傳失敗，請稍後再試。'
      throw err // 將錯誤再次拋出，讓組件可以捕獲
    } finally {
      isUploading.value = false
    }
  }

  const updateProfile = async (profileData) => {
    isUpdating.value = true
    error.value = null
    try {
      await publicMemberProfileService.updateProfile(profileData)
      // 更新成功後，重新獲取最新的個人資料來同步狀態
      await fetchProfile()
    } catch (err) {
      console.error('Failed to update profile in store:', err)
      error.value = err.response?.data?.message || err.message || '個人資料更新失敗，請稍後再試。'
      // 讓組件可以捕獲到錯誤
      throw err
    } finally {
      isUpdating.value = false
    }
  }

  const changePassword = async (passwordData) => {
    isChangingPassword.value = true
    error.value = null
    try {
      const response = await publicMemberProfileService.changePassword(passwordData)
      return response
    } catch (err) {
      console.error('Failed to change password in store:', err)
      error.value = err.response?.data || err.message || '密碼變更失敗，請稍後再試。'
      throw err
    } finally {
      isChangingPassword.value = false
    }
  }

  // --- 【已修正】收藏相關 Actions ---

  const fetchFavoriteIds = async () => {
    try {
      // 正確地呼叫 publicMemberProfileService 中的方法
      const ids = await publicMemberProfileService.getFavoriteMovieIds()
      favoriteMovieIds.value = new Set(ids)
    } catch (error) {
      console.error('獲取收藏電影 ID 失敗:', error)
    }
  }

  const fetchFullFavorites = async () => {
    isLoadingFavorites.value = true
    try {
      // 正確地呼叫 publicMemberProfileService 中的方法
      const movies = await publicMemberProfileService.getFavoriteMovies()
      favorites.value = movies
    } catch (error) {
      console.error('獲取完整收藏列表失敗:', error)
    } finally {
      isLoadingFavorites.value = false
    }
  }

  const addFavorite = async (movieId) => {
    try {
      await publicMemberProfileService.addFavorite(movieId)
      favoriteMovieIds.value.add(movieId) // 樂觀更新
    } catch (error) {
      console.error('新增收藏失敗:', error)
    }
  }

  const removeFavorite = async (movieId) => {
    try {
      await publicMemberProfileService.removeFavorite(movieId)
      favoriteMovieIds.value.delete(movieId) // 樂觀更新
    } catch (error) {
      console.error('移除收藏失敗:', error)
    }
  }

  // --- 【新增】訂票紀錄相關方法 ---
  const fetchTicketOrderHistory = async () => {
    isLoadingHistory.value = true
    historyError.value = null
    try {
      const data = await publicMemberProfileService.getTicketOrderHistory()
      ticketOrderHistory.value = data
    } catch (err) {
      historyError.value = err.message || '獲取訂票紀錄失敗'
      console.err('獲取訂票紀錄失敗:', err)
    } finally {
      isLoadingHistory.value = false
    }
  }

  // --- 【新增】商品訂單紀錄相關方法 ---
  const fetchProductOrderHistory = async () => {
    isLoadingProductHistory.value = true
    productHistoryError.value = null
    try {
      const data = await publicMemberProfileService.getProductOrderHistory()
      productOrderHistory.value = data
    } catch (err) {
      productHistoryError.value = err.message || '獲取商品訂單紀錄失敗'
      console.error('獲取商品訂單紀錄失敗:', err)
    } finally {
      isLoadingProductHistory.value = false
    }
  }

  return {
    profile,
    isLoading,
    error,
    isUploading,
    isUpdating,
    isChangingPassword,
    fetchProfile,
    updateAvatar,
    updateProfile,
    changePassword,

    // 【新增】收藏的部分
    favoriteMovieIds,
    favorites,
    isLoadingFavorites,
    fetchFavoriteIds,
    fetchFullFavorites,
    addFavorite,
    removeFavorite,

    // 【新增】訂票紀錄的部分
    ticketOrderHistory,
    isLoadingHistory,
    historyError,
    fetchTicketOrderHistory,

    // --- 【新增】商品訂單紀錄的部分 ---
    productOrderHistory,
    isLoadingProductHistory,
    productHistoryError,
    fetchProductOrderHistory,
  }
})
