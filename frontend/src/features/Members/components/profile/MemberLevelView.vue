<template>
  <div class="member-level-view">
    <!-- Loading or Error State -->
    <div v-if="isLoading || levelsLoading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="error || levelsError" class="error-state">
      <el-alert :title="error || levelsError" type="error" show-icon :closable="false" />
    </div>

    <div v-else>
      <!-- Dynamic Progress Section -->
      <div v-if="status" class="progress-section">
        <el-card shadow="always">
          <div class="current-level-info">
            <div class="level-identity">
              <el-image
                v-if="status.currentLevelIcon"
                :src="`data:image/png;base64,${status.currentLevelIcon}`"
                fit="contain"
                class="current-level-icon"
              />
              <div class="level-text">
                <span>目前等級</span>
                <h3 class="current-level-name">{{ status.currentLevelName }}</h3>
              </div>
            </div>
            <div class="spending-info">
              <span>目前累積消費</span>
              <p class="spending-amount">NT$ {{ status.totalSpent }}</p>
            </div>
          </div>

          <el-progress
            :percentage="status.progressPercentage"
            :stroke-width="22"
            striped
            striped-flow
            :color="['#6fcf97', '#f2c94c', '#f2994a', '#eb5757']"
          >
            <span>{{ status.totalSpent }} / {{ status.nextLevelThreshold }}</span>
          </el-progress>

          <div class="progress-label">
            <span>
              NT$ {{ currentLevelThreshold }}
              <small>{{ status.currentLevelName }}</small>
            </span>
            <span v-if="status.nextLevelName">
              NT$ {{ status.nextLevelThreshold }}
              <small>{{ status.nextLevelName }}</small>
            </span>
            <span v-else>
              <small>最高等級</small>
            </span>
          </div>

          <div class="upgrade-message">
            <p>{{ upgradeMessage }}</p>
          </div>
        </el-card>
      </div>

      <!-- All Levels Section -->
      <div class="all-levels-section">
        <h2 class="section-title">所有會員等級</h2>
        <div class="levels-grid">
          <el-card
            v-for="level in sortedLevels"
            :key="level.memberLevelId"
            class="level-card"
            :class="{ 'is-current-level': status && level.levelName === status.currentLevelName }"
            shadow="hover"
          >
            <div class="card-content">
              <div class="icon-container">
                <el-image
                  v-if="level.levelIcon"
                  :src="`data:image/png;base64,${level.levelIcon}`"
                  fit="contain"
                  class="level-icon"
                />
                <div v-else class="icon-placeholder">無圖示</div>
              </div>
              <div class="details-container">
                <h3 class="level-name">{{ level.levelName }}</h3>
                <p class="level-description">{{ level.upgradeConditionDescription }}</p>
                <p class="level-threshold">
                  <strong>升級門檻:</strong> 累積消費滿 NT$ {{ level.thresholdLowerBound }}
                </p>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useMemberLevelStatusStore } from '@/features/MemberLevel/store/useMemberLevelStatusStore.js'
import { useMemberLevelStore } from '@/features/MemberLevel/store/memberLevelStore.js'
import { ElCard, ElImage, ElSkeleton, ElAlert, ElProgress } from 'element-plus'

// 使用兩個 Store
const levelStatusStore = useMemberLevelStatusStore()
const memberLevelStore = useMemberLevelStore()

// 從 Store 中解構出需要的狀態和方法
const { status, isLoading, error } = storeToRefs(levelStatusStore)
const { updateStatus, fetchStatus } = levelStatusStore

const { levels, isLoading: levelsLoading, error: levelsError } = storeToRefs(memberLevelStore)
const { fetchLevels } = memberLevelStore

// 確保會員等級列表是按升級門檻排序的
const sortedLevels = computed(() => {
  if (!levels.value) return []
  return [...levels.value].sort((a, b) => a.thresholdLowerBound - b.thresholdLowerBound)
})

const currentLevelThreshold = computed(() => {
  if (!status.value || !levels.value) return 0
  const current = levels.value.find((l) => l.levelName === status.value.currentLevelName)
  return current ? current.thresholdLowerBound : 0
})

const upgradeMessage = computed(() => {
  if (!status.value) return ''
  if (!status.value.nextLevelName) return '恭喜您！您已是我們的頂級會員！'
  const remaining = Math.max(0, status.value.nextLevelThreshold - status.value.totalSpent)
  return `再消費 NT$ ${remaining} 即可升級至 ${status.value.nextLevelName}。`
})

onMounted(async () => {
  // 依序執行：更新狀態 -> 獲取最新狀態 -> 獲取所有等級列表
  await updateStatus()
  await fetchStatus()
  await fetchLevels()
})
</script>

<style scoped>
.member-level-view {
  padding: 20px;
}

.progress-section {
  margin-bottom: 40px; /* 增加與下方列表的間距 */
}

.progress-section .el-card {
  background-color: #333;
  border-color: #555;
  color: #e0e0e0;
  border-radius: 12px;
  padding: 20px;
}

.current-level-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}

.level-identity {
  display: flex;
  align-items: center;
  gap: 15px;
}

.current-level-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #444;
  border: 2px solid #4a90e2;
}

.level-text span {
  font-size: 0.9rem;
  color: #aaa;
}

.current-level-name {
  font-size: 1.8rem;
  font-weight: bold;
  color: #4a90e2;
  margin: 0;
}

.spending-info {
  text-align: right;
}

.spending-info span {
  font-size: 0.9rem;
  color: #aaa;
}

.spending-amount {
  font-size: 1.5rem;
  font-weight: bold;
  color: #6fcf97;
  margin: 0;
}

.el-progress {
  margin-bottom: 5px;
}

:deep(.el-progress-bar__innerText) {
  color: #000;
  font-weight: bold;
  font-size: 14px;
  text-shadow: 1px 1px 2px rgba(255, 255, 255, 0.5);
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
}

.progress-label small {
  display: block;
  color: #ccc;
  font-size: 11px;
}

.upgrade-message {
  text-align: center;
  background-color: rgba(0, 0, 0, 0.2);
  padding: 10px;
  border-radius: 6px;
  margin-top: 15px;
}

.upgrade-message p {
  margin: 0;
  color: #f2c94c;
  font-size: 1rem;
}

.loading-state,
.error-state {
  max-width: 800px;
  margin: 20px auto;
}

/* All Levels Section Styles */
.all-levels-section {
  margin-top: 30px;
}

.section-title {
  font-size: 1.5rem;
  color: #e0e0e0;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #444;
}

.levels-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.level-card {
  border: 1px solid #444;
  background-color: #2c2c2c;
  color: #e0e0e0;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  border-radius: 8px;
}

.level-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 123, 255, 0.2);
}

.level-card.is-current-level {
  border-color: #4a90e2;
  box-shadow: 0 0 15px rgba(74, 144, 226, 0.5);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.icon-container {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #3a3a3a;
  border-radius: 50%;
}

.level-icon {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.icon-placeholder {
  font-size: 12px;
  color: #888;
}

.details-container {
  flex-grow: 1;
}

.level-name {
  font-size: 1.5rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 8px 0;
}

.level-description {
  font-size: 1rem;
  color: #b0b0b0;
  margin: 0 0 12px 0;
  min-height: 40px; /* 給描述一個最小高度，避免卡片高度不一 */
}

.level-threshold {
  font-size: 0.9rem;
  color: #909090;
  margin: 0;
}

.level-threshold strong {
  color: #c0c0c0;
}
</style>
