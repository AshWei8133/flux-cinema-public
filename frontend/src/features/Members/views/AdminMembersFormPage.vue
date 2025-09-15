<template>
  <div class="page-container">
    <h1 class="page-title">{{ editMode ? '編輯會員' : '新增會員' }}</h1>

    <div v-if="isLoading" class="loading">載入資料中...</div>
    <div v-else-if="loadError" class="error">{{ loadError }}</div>

    <MemberForm
      v-else
      :member="memberData"
      :editMode="editMode"
      :isSubmitting="isSubmitting"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMemberStore } from '../store/useMemberStore'
import MemberForm from '../components/MemberForm.vue'

const route = useRoute()
const router = useRouter()
const store = useMemberStore()

const memberData = ref(null)
const isSubmitting = ref(false)
const isLoading = ref(false)
const loadError = ref('')

// 透過路由參數 `id` 是否存在來判斷是編輯還是新增模式
const editMode = computed(() => !!route.params.id)
const memberId = computed(() => route.params.id)

// 組件掛載時，如果是編輯模式，則獲取會員資料
onMounted(async () => {
  if (editMode.value) {
    isLoading.value = true
    try {
      await store.fetchMemberById(memberId.value)
      memberData.value = store.selectedMember
      if (!memberData.value) {
        loadError.value = '查無此會員資料。'
      }
    } catch (error) {
      loadError.value = '資料載入失敗。'
    } finally {
      isLoading.value = false
    }
  }
})

// 處理表單提交
const handleSubmit = async (formData, file) => {
  isSubmitting.value = true
  try {
    if (editMode.value) {
      await store.updateMember(memberId.value, formData, file)
      alert('更新成功！')
    } else {
      await store.createMember(formData, file)
      alert('新增成功！')
    }
    // 操作成功後，返回會員列表頁
    router.push('/admin/members')
  } catch (e) {
    alert(e.message || '操作失敗')
  } finally {
    isSubmitting.value = false
  }
}

// 處理取消操作，返回上一頁
const handleCancel = () => {
  router.back()
}
</script>

<style scoped>
.page-container {
  max-width: 600px;
  margin: auto;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
  color: #1f2937; /* 深灰色 */
}

.loading {
  text-align: center;
  font-size: 16px;
  padding: 12px;
  background-color: #f3f4f6;
  border-radius: 8px;
  color: #374151;
}

.error {
  text-align: center;
  font-size: 16px;
  padding: 12px;
  border-radius: 8px;
  background-color: #fee2e2;
  color: #b91c1c;
  font-weight: 500;
}
</style>
