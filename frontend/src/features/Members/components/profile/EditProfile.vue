<template>
  <div class="info-grid edit-mode">
    <div class="info-item">
      <span class="label">帳號</span>
      <input v-model="editableProfile.username" class="edit-input" />
    </div>
    <div class="info-item">
      <span class="label">電子郵件</span>
      <span class="value">{{ editableProfile.email }}</span>
    </div>
    <div class="info-item-inline">
      <div class="info-item">
        <span class="label">性別</span>
        <select v-model="editableProfile.gender" class="edit-select">
          <option value="M">男</option>
          <option value="F">女</option>
          <option value="O">其他</option>
        </select>
      </div>
      <div class="info-item">
        <span class="label">生日</span>
        <input v-model="editableProfile.birthDate" type="date" class="edit-input" />
      </div>
    </div>
    <div class="info-item">
      <span class="label">電話</span>
      <input v-model="editableProfile.phone" class="edit-input" />
    </div>
    <div class="edit-controls">
      <button @click="saveChanges" :disabled="isUpdating" class="save-button">
        {{ isUpdating ? '儲存中...' : '儲存變更' }}
      </button>
      <button @click="cancel" class="cancel-button">取消</button>
    </div>
  </div>
</template>

<script setup>
import { reactive, watchEffect } from 'vue'

const props = defineProps({
  profile: {
    type: Object,
    required: true,
  },
  isUpdating: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['save-changes', 'cancel-edit'])

const editableProfile = reactive({
  username: '',
  email: '',
  gender: '',
  birthDate: '',
  phone: '',
})

// Watch for prop changes to initialize or update the local state
watchEffect(() => {
  if (props.profile) {
    editableProfile.username = props.profile.username
    editableProfile.email = props.profile.email
    editableProfile.gender = props.profile.gender
    editableProfile.birthDate = props.profile.birthDate
    editableProfile.phone = props.profile.phone
  }
})

const saveChanges = () => {
  // We only need to emit the fields that are actually editable
  const updatedData = {
    username: editableProfile.username,
    gender: editableProfile.gender,
    birthDate: editableProfile.birthDate,
    phone: editableProfile.phone,
  }
  emit('save-changes', updatedData)
}

const cancel = () => {
  emit('cancel-edit')
}
</script>

<style scoped>
/* Styles are copied from MemberProfile.vue for consistency */
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.info-item {
  border: 1px solid #555;
  padding: 0.8rem 1rem;
  display: flex;
  flex-direction: column;
  border-radius: 4px;
}
.info-item-inline {
  display: flex;
  gap: 1rem;
}
.info-item-inline .info-item {
  flex: 1;
}
.label {
  font-family: 'Inter', sans-serif;
  font-weight: 700;
  font-size: 1.5rem;
  color: #aaa;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.value {
  font-size: 2rem;
  color: #fff;
  line-height: 1.2;
}
.edit-input,
.edit-select {
  background-color: #333;
  color: #fff;
  border: 1px solid #777;
  border-radius: 4px;
  padding: 8px;
  font-size: 2rem;
  width: 100%;
  box-sizing: border-box;
}
.edit-select {
  font-size: 2rem;
  padding: 10px 8px;
}
.edit-controls {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}
.edit-controls button {
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}
.save-button {
  background-color: #4a90e2;
  color: white;
}
.save-button:hover {
  background-color: #357abd;
}
.save-button:disabled {
  background-color: #555;
  cursor: not-allowed;
}
.cancel-button {
  background-color: #888;
  color: white;
}
.cancel-button:hover {
  background-color: #666;
}
</style>
