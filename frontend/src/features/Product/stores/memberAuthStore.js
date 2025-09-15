// stores/memberAuthStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMemberAuthStore = defineStore('memberAuth', () => {
  const token = ref(localStorage.getItem('member_jwt_token') || null)

  const isAuthenticated = computed(() => !!token.value)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('member_jwt_token', newToken)
  }

  const clearToken = () => {
    token.value = null
    localStorage.removeItem('member_jwt_token')
  }

  return { token, isAuthenticated, setToken, clearToken }
})
