<template>
  <div class="coupon-wizard">
    <el-card shadow="always" class="wizard-header">
      <div class="header-row">
        <el-tag type="info" round>Step {{ currentStep }} / 4</el-tag>
      </div>

      <el-steps :active="currentStep - 1" simple class="steps">
        <el-step title="選擇活動" />
        <el-step title="選擇優惠條件" />
        <el-step title="選擇優惠方式" />
        <el-step title="總覽" />
      </el-steps>
    </el-card>

    <transition name="fade-slide" mode="out-in">
      <div :key="currentStep" class="wizard-body">
        <keep-alive>
          <component
            ref="stepRef"
            :is="currentStepComponent"
            :formData="couponFormData"
            :showFooter="false"
            @data-change="updateFormData"
          />
        </keep-alive>
      </div>
    </transition>

    <div class="wizard-actions">
      <div class="left">
        <el-button :disabled="currentStep === 1 || loading" @click="prevStep" plain>
          上一步
        </el-button>
      </div>
      <div class="right">
        <el-button v-if="currentStep < 4" type="primary" :loading="loading" @click="nextStep">
          下一步
        </el-button>
        <el-button v-else type="success" :loading="loading" @click="issueCoupon"> 發放 </el-button>
      </div>
    </div>

    <el-alert
      v-if="errorMsg"
      class="wizard-error"
      type="error"
      :title="errorMsg"
      show-icon
      closable
      @close="errorMsg = ''"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router' // ★ 新增
// 用你專案的 httpClient 比較一致（可留著 axios 但我建議改）
import httpClient from '@/services/api' // ★ 可選：若你要在父層打 API

import Step1 from '@/features/Event/views/Coupon/AdminEventSelect.vue'
import Step2 from '@/features/Event/views/Coupon/AdminCouponCondition.vue'
import Step3 from '@/features/Event/views/Coupon/AdminChooseDiscount.vue'
import Step4 from '@/features/Event/views/Coupon/AdminOverview.vue'

const router = useRouter() // ★ 新增

const currentStep = ref(1)
const loading = ref(false)
const errorMsg = ref('')

const couponFormData = ref({
  event: {},
  conditions: {},
  discount: {},
})

const stepComponents = { 1: Step1, 2: Step2, 3: Step3, 4: Step4 }
const currentStepComponent = computed(() => stepComponents[currentStep.value])
const stepRef = ref()

/** 呼叫子元件（每一步）暴露的 validate */
const callChildValidate = async () => {
  const inst = stepRef.value
  if (inst && typeof inst.validate === 'function') {
    return !!(await inst.validate())
  }
  return true
}

const nextStep = async () => {
  errorMsg.value = ''
  const pass = await callChildValidate()
  if (!pass) {
    errorMsg.value = '請修正此步驟的必填資料後再繼續。'
    return
  }
  if (currentStep.value < 4) currentStep.value++
}

const prevStep = () => {
  errorMsg.value = ''
  if (currentStep.value > 1 && !loading.value) currentStep.value--
}

/** 子元件會用 @data-change 回拋；我們合併到同一份表單物件 */
const updateFormData = (data) => {
  couponFormData.value = { ...couponFormData.value, ...data }
}

/** 發放：改成呼叫 Step4 暴露的 issue() */
const issueCoupon = async () => {
  errorMsg.value = ''
  const inst = stepRef.value

  // 最後一步也跑一下 validate（若 Step4 有檢核）
  const pass = await callChildValidate()
  if (!pass) {
    errorMsg.value = '總覽中有必填資料未完成，請檢查後再發放。'
    return
  }

  if (!inst || typeof inst.issue !== 'function') {
    errorMsg.value = '發放功能尚未載入，請稍後再試。'
    return
  }

  try {
    loading.value = true
    const created = await inst.issue() // ★ 呼叫子元件發放
    // 成功：重置或導回列表
    couponFormData.value = { event: {}, conditions: {}, discount: {} }
    currentStep.value = 1
    // 導回列表頁（請換成你的實際路由）
    router.push({ name: 'AdminCouponList' }) // ★ 例如：/admin/coupons/list
  } catch (err) {
    errorMsg.value = err?.message || '發放失敗，請稍後再試。'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.coupon-wizard {
  display: grid;
  gap: 16px;
  padding: 0 12px 24px;
  background-color: #f0f2f5;
}

/* header 卡片 */
.wizard-header {
  margin-left: 10px;
  margin-right: 10px;
  margin-top: 10px;
  border-radius: 16px;
  padding: 16px 20px;
  background: var(--el-bg-color-overlay);
}

/* header row */
.header-row {
  display: flex;
  align-items: center;
  justify-content: flex-start; /* ← 改成靠左 */
  gap: 12px;
  margin-bottom: 8px;
}

.header-row .el-tag {
  font-size: 13px;
  padding: 2px 10px;
  font-weight: 600;
  background-color: var(--el-color-info-light-9);
  color: var(--el-color-info);
  border: none;
  border-radius: 999px; /* 更圓的 pill 樣式 */
}

.title {
  margin: 0;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 0.5px;
}

.steps {
  margin-top: 4px;
  --el-step-icon-size: 20px;
}
.steps :deep(.el-step__title) {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
}

/* body 區域 */
.wizard-body {
  margin-left: 10px;
  margin-right: 10px;
  min-height: 320px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* sticky action bar */
.wizard-actions {
  position: sticky;
  margin-left: 10px;
  margin-right: 10px;
  bottom: 0;
  margin-top: 8px;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 12px;
  z-index: 1;
  box-shadow: 0 -2px 6px rgba(0, 0, 0, 0.04);
  background: linear-gradient(to top, var(--el-bg-color), transparent);
  backdrop-filter: blur(6px);
}

.wizard-actions .el-button {
  min-width: 90px;
  font-weight: 500;
}

.wizard-error {
  position: sticky;
  bottom: 8px;
  z-index: 2;
  border-radius: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

/* 轉場動畫 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition:
    opacity 0.16s ease,
    transform 0.16s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(6px);
}
</style>
