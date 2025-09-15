<template>
  <div v-if="member" class="member-flat">
    <div class="member-row"><span class="label">帳號:</span> {{ member.username }}</div>
    <div class="member-row"><span class="label">Email:</span> {{ member.email }}</div>
    <div class="member-row">
      <span class="label">性別:</span> {{ getGenderDisplay(member.gender) }}
    </div>
    <div class="member-row">
      <span class="label">電話:</span> {{ formatPhoneNumber(member.phone) }}
    </div>
    <div class="member-row"><span class="label">生日:</span> {{ member.birthDate }}</div>
    <div class="member-row"><span class="label">登入方式:</span> {{ member.loginMethod }}</div>
    <div class="member-row"><span class="label">註冊時間:</span> {{ member.registerDate }}</div>

    <div class="photo-wrapper">
      <img :src="photoUrl" alt="會員照片" />
    </div>

    <button class="close-btn" @click="$emit('close')">關閉</button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useMemberStore } from '../store/useMemberStore'
import { useAuthStore } from '@/stores/auth'
import defaultAvatar from '@/assets/images/User.jpg'

const props = defineProps({ memberId: Number })
const emit = defineEmits(['close'])
const store = useMemberStore()
const authStore = useAuthStore()

const member = ref(null)
const photoUrl = ref(defaultAvatar)

const getGenderDisplay = (gender) => {
  if (gender === 'M') return '男'
  if (gender === 'F') return '女'
  return gender
}

const formatPhoneNumber = (phone) => {
  if (!phone || typeof phone !== 'string' || phone.length !== 10) {
    return phone
  }
  return `${phone.slice(0, 4)}-${phone.slice(4, 7)}-${phone.slice(7)}`
}

const fetchMemberPhoto = async (id) => {
  const token = authStore.token

  if (!token) {
    console.error('Admin token not found!')
    // 發生錯誤，使用預設圖片
    photoUrl.value = defaultAvatar
    return
  }

  try {
    const response = await fetch(`/api/admin/members/photo/${id}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
    if (response.ok) {
      const imageBlob = await response.blob()
      if (imageBlob.size > 0) {
        if (photoUrl.value && photoUrl.value.startsWith('blob:')) {
          URL.revokeObjectURL(photoUrl.value) // 釋放舊的 Blob URL
        }
        photoUrl.value = URL.createObjectURL(imageBlob)
      } else {
        // 後端沒有照片，使用預設圖片
        photoUrl.value = defaultAvatar
      }
    } else {
      // API 請求失敗 (例如 404)，使用預設圖片
      photoUrl.value = defaultAvatar
    }
  } catch (error) {
    console.error('Error fetching member photo:', error)
    // 發生網路等錯誤，使用預設圖片
    photoUrl.value = defaultAvatar
  }
}

watch(
  () => props.memberId,
  async (id) => {
    // 每次切換會員時，先重設為預設圖片
    photoUrl.value = defaultAvatar
    member.value = null

    if (id != null) {
      await store.fetchMemberById(id)
      member.value = store.selectedMember

      if (member.value) {
        // 獲取真實照片 (如果有的話)
        fetchMemberPhoto(id)
      }
    }
  },
  { immediate: true },
)
</script>

<style scoped>
.member-flat {
  max-width: 600px;
  margin: 20px auto;
  font-family: 'Segoe UI', sans-serif;
  color: #333;
  line-height: 1.6;
}

.member-row {
  display: flex;
  margin-bottom: 8px;
}

.label {
  width: 120px;
  font-weight: 600;
  color: #4f46e5;
}

.photo-wrapper {
  margin: 15px 0;
  text-align: center;
}

.photo-wrapper img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ddd;
}

.close-btn {
  display: block;
  margin: 0 auto;
  padding: 8px 20px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.close-btn:hover {
  background-color: #4338ca;
}
</style>
