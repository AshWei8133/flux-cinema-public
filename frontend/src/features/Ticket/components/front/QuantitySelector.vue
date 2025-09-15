<template>
  <div class="quantity-selector">
    <button class="quantity-btn" @click="decrement" :disabled="modelValue === 0">
      <Icon icon="mdi:minus" />
    </button>
    <span class="quantity-display">{{ modelValue }}</span>
    <button class="quantity-btn" @click="increment" :disabled="modelValue >= max">
      <Icon icon="mdi:plus" />
    </button>
  </div>
</template>

<script setup>
/**
 * @fileoverview
 * 可重用的數量選擇器組件 (QuantitySelector.vue)
 *
 * - 職責：
 * 1. 顯示一個包含「減」、「加」按鈕和目前數量的 UI。
 * 2. 透過 `v-model` 與父組件雙向綁定數量值。
 * 3. 根據傳入的 `max` 屬性，限制最大可選數量。
 * 4. 在數量為 0 或達到最大值時，自動禁用對應的按鈕。
 */
import { Icon } from '@iconify/vue'

// ----------------------------------------------------------------
// 組件屬性與事件定義 (Props & Emits)
// ----------------------------------------------------------------
const props = defineProps({
  /**
   * 當前數量，用於 v-model。
   */
  modelValue: {
    type: Number,
    required: true,
  },
  /**
   * 允許選擇的最大數量。
   */
  max: {
    type: Number,
    default: 10,
  },
})

const emit = defineEmits(['update:modelValue'])

// ----------------------------------------------------------------
// 方法 (Methods)
// ----------------------------------------------------------------

/**
 * 增加數量，並發出事件通知父組件更新。
 */
function increment() {
  if (props.modelValue < props.max) {
    emit('update:modelValue', props.modelValue + 1)
  }
}

/**
 * 減少數量，並發出事件通知父組件更新。
 */
function decrement() {
  if (props.modelValue > 0) {
    emit('update:modelValue', props.modelValue - 1)
  }
}
</script>

<style scoped>
.quantity-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #333;
  border-radius: 20px;
  padding: 4px;
  border: 1px solid #555;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #e50914;
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  transition:
    background-color 0.2s,
    transform 0.2s;
}

.quantity-btn:hover:not(:disabled) {
  background-color: #f40612;
  transform: scale(1.1);
}

.quantity-btn:disabled {
  background-color: #555;
  cursor: not-allowed;
  opacity: 0.6;
}

.quantity-display {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  min-width: 25px;
  text-align: center;
}
</style>
