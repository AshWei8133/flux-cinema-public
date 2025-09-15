<template>
  <el-card shadow="hover" class="generic-card">
    <img :src="imageSrc" alt="圖片" class="card-image" v-if="imageSrc" />
    <el-image v-else class="card-image-placeholder" fit="cover" />

    <div style="padding: 14px">
      <h4>{{ title }}</h4>
      <div class="card-text" v-for="(item, index) in bodyItems" :key="index">
        <span>{{ item }}</span>
      </div>
    </div>

    <div class="card-footer">
      <slot name="footer"></slot>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  imageSrc: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    required: true,
  },
  bodyItems: {
    type: Array,
    default: () => [],
  },
})
</script>

<style scoped>
.generic-card {
  width: 100%;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  display: flex;
  flex-direction: column;
}

.generic-card:hover {
  transform: translateY(-5px);
}

/* 確保卡片內容填滿空間，並將 footer 推到底部 */
:deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.card-footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
}

.card-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.card-image-placeholder {
  width: 100%;
  height: 200px;
  background-color: #e9e9eb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

h4 {
  font-size: 18px;
  margin: 0 0 8px 0;
}

.card-text {
  font-size: 14px;
  color: #606266;
  line-height: 20px;
  margin-bottom: 4px;
}
</style>
