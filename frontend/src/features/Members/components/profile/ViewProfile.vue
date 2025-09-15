<template>
  <div class="info-grid view-mode">
    <div class="info-item">
      <span class="label">帳號</span>
      <span class="value">{{ profile.username }}</span>
    </div>
    <div class="info-item">
      <span class="label">電子郵件</span>
      <span class="value">{{ profile.email }}</span>
    </div>
    <div class="info-item-inline">
      <div class="info-item">
        <span class="label">性別</span>
        <span class="value">{{ genderText }}</span>
      </div>
      <div class="info-item">
        <span class="label">生日</span>
        <span class="value">{{ profile.birthDate || '未設定' }}</span>
      </div>
    </div>
    <div class="info-item-inline">
      <div class="info-item">
        <span class="label">電話</span>
        <span class="value">{{ formattedPhone }}</span>
      </div>
      <div class="info-item">
        <span class="label">註冊日期</span>
        <span class="value">{{ formattedRegisterDate }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  profile: {
    type: Object,
    required: true,
  },
})

const genderText = computed(() => {
  if (!props.profile) return ''
  switch (props.profile.gender) {
    case 'M':
      return '男'
    case 'F':
      return '女'
    case 'O':
      return '其他'
    default:
      return '未設定'
  }
})

const formattedRegisterDate = computed(() => {
  if (!props.profile || !props.profile.registerDate) return ''
  return new Date(props.profile.registerDate).toLocaleDateString()
})

const formattedPhone = computed(() => {
  const phone = props.profile?.phone
  if (!phone) return '未設定'
  // Simple format, can be improved with a regex for more robustness
  if (phone.length === 10) {
    return phone.replace(/(\d{4})(\d{3})(\d{3})/, '$1-$2-$3')
  }
  return phone
})
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
  color: #aaaaaaf6;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.value {
  font-size: 2rem;
  color: #fff;
  line-height: 1.2;
}
</style>
