<template>
  <section id="booking-section" class="booking-announcement-section">
    <div class="container">
      <QuickBookingForm />
      <AnnouncementList />
    </div>
  </section>
</template>

<script setup>
import AnnouncementList from '@/features/Event/components/AnnouncementList.vue'
import QuickBookingForm from '@/features/MovieSessions/components/front/QuickBookingForm.vue'

// 引入新建立的子組件

// 原本的資料和方法都已經移到子組件中了，這裡變得非常乾淨！
</script>

<style scoped>
/* 父組件只保留負責整體佈局的樣式 */
.booking-announcement-section {
  padding: 60px 0;
  background-color: #141414;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1.5fr; /* 左右比例維持不變 */
  gap: 40px;
  padding: 0 20px;
}
</style>
