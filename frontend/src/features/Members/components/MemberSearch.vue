<template>
  <div class="search-bar">
    <input v-model="username" placeholder="帳號" />
    <input v-model="email" placeholder="Email" />
    <select v-model="gender">
      <option value="">性別 (不限)</option>
      <option value="M">男</option>
      <option value="F">女</option>
    </select>
    <button @click="handleSearch">🔍 查詢</button>
    <button @click="resetSearch">重置</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useMemberStore } from '../store/useMemberStore'

const store = useMemberStore()
const username = ref('')
const email = ref('')
const gender = ref('')

const handleSearch = () => {
  const filters = {
    username: username.value,
    email: email.value,
    gender: gender.value,
  }
  store.filterMembers(filters) // 前端過濾
}

const resetSearch = () => {
  username.value = ''
  email.value = ''
  gender.value = ''
  store.fetchAllMembers() // 重新抓全部會員
}
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}
.search-bar input {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
}
.search-bar button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>
