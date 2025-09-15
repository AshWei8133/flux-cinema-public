<template>
  <div class="card">
    <div v-if="store.isMembersLoading" class="loading">載入中...</div>
    <div v-if="store.membersError" class="error">{{ store.membersError }}</div>

    <table v-if="store.members.length">
      <thead>
        <tr>
          <th>帳號</th>
          <th>Email</th>
          <th>性別</th>
          <th>電話</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="member in store.members" :key="member.memberId">
          <td>{{ member.username }}</td>
          <td>{{ member.email }}</td>
          <td>{{ getGenderDisplay(member.gender) }}</td>
          <td>{{ formatPhoneNumber(member.phone) }}</td>
          <td class="actions">
            <button class="btn btn-view" @click="$emit('view', member.memberId)" title="查看">
              🔍
            </button>
            <button class="btn btn-edit" @click="$emit('edit', member.memberId)" title="編輯">
              ✏️
            </button>
            <button class="btn btn-delete" @click="$emit('delete', member.memberId)" title="刪除">
              🗑
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-else class="empty">目前沒有會員資料</div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useMemberStore } from '../store/useMemberStore'

const store = useMemberStore()

const getGenderDisplay = (gender) => {
  if (gender === 'M') return '男'
  if (gender === 'F') return '女'
}

const formatPhoneNumber = (phone) => {
  if (!phone || typeof phone !== 'string' || phone.length !== 10) {
    return phone
  }
  return `${phone.slice(0, 4)}-${phone.slice(4, 7)}-${phone.slice(7)}`
}

onMounted(() => store.fetchAllMembers())
</script>

<style scoped>
.card {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h2 {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 15px;
}

.loading {
  color: gray;
}

.error {
  color: red;
  font-weight: bold;
}

.empty {
  margin-top: 10px;
  color: gray;
}

/* 表格樣式 */
table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ddd;
}

th,
td {
  padding: 10px;
  border: 1px solid #ddd;
  text-align: left;
}

thead {
  background-color: #f3f4f6;
}

tbody tr:hover {
  background-color: #f9fafb;
}

/* 按鈕樣式 */
.actions {
  text-align: center;
}

.btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  margin: 0 3px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}

.btn-view {
  background-color: #22c55e; /* 綠色 */
  color: white;
}

.btn-view:hover {
  background-color: #16a34a;
}

.btn-edit {
  background-color: #f59e0b; /* 黃色 */
  color: white;
}

.btn-edit:hover {
  background-color: #d97706;
}

.btn-delete {
  background-color: #ef4444; /* 紅色 */
  color: white;
}

.btn-delete:hover {
  background-color: #dc2626;
}
</style>
