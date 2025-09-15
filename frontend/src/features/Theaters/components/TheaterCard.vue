<template>
  <AdminCard
    :image-src="theater.theaterPhotoImgBase64"
    :title="theater.theaterName"
    :body-items="cardBodyItems"
  >
    <template #footer>
      <AdminButton type="primary" plain class="card-button" size="small" @click="handleEditTheater"
        >詳情</AdminButton
      >
      <AdminButton type="danger" plain class="card-button" size="small" @click="handleDeleteTheater"
        >刪除</AdminButton
      >
    </template>
  </AdminCard>
</template>

<script setup>
import AdminButton from '@/components/admin/AdminButton.vue'
import AdminCard from '@/components/admin/AdminCard.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useTheaterStore } from '../store/useTheaterStore'

const props = defineProps({
  theater: {
    type: Object,
    required: true,
  },
})
const router = useRouter()
const theaterStore = useTheaterStore()

// 計算屬性
// 將影廳資料轉換為通用卡片所需的 bodyItems 格式
const cardBodyItems = computed(() => [
  `類型：${props.theater.theaterTypeName}`,
  `總座位數：${props.theater.totalSeats}`,
])

// 方法區
// 處理點擊詳情
const handleEditTheater = () => {
  console.log(`點擊「詳情」按鈕，將跳至 ID 為 ${props.theater.theaterId}，可在此頁面執行編輯操作`)
  router.push({
    name: 'AdminTheaterUpdate',
    params: { id: props.theater.theaterId },
  })
}

// 處理刪除功能
const handleDeleteTheater = async () => {
  console.log(`點擊「刪除」按鈕，將刪除 ID 為 ${props.theater.theaterId} 影廳資料`)
  try {
    // 刪除前先作確認
    await ElMessageBox.confirm(`即將刪除${props.theater.theaterName}，確定要刪除嗎?`, '警告', {
      confirmButtonText: '確定刪除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    // 呼叫 theaterStore 執行刪除影廳操作
    const result = await theaterStore.deleteTheater(props.theater.theaterId)

    if (result.success) {
      ElMessage.success(result.message)
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    // 捕捉取消操作和API錯誤
    if (error === 'cancel' || error === 'close') {
      ElMessage.info('已取消刪除')
    } else {
      ElMessage.error(error.message || '刪除影廳時發生未知錯誤，請稍後再試。')
      console.error('刪除影廳錯誤:', error)
    }
  }
}
</script>

<style scoped>
.card-button {
  margin-left: 8px;
}
</style>
