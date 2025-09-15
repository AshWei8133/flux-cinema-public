<template>
  <el-button
    :type="mappedType"
    :size="mappedSize"
    :disabled="disabled"
    :round="round"
    :circle="circle"
    :plain="plain"
    :loading="loading"
    @click="handleClick"
  >
    <slot>{{ label }}</slot>
  </el-button>
</template>

<script setup>
import { computed } from 'vue'

// 1. 定義 props
const props = defineProps({
  label: {
    type: String,
    default: '',
  },
  /**
   * 按鈕類型，決定樣式。
   * 映射到 Element Plus 的 'type'
   * 可選值：'primary', 'success', 'warning', 'danger', 'info', 'text', 'default' (您可自定義映射)
   */
  type: {
    type: String,
    default: 'default', // 預設為 Element Plus 的 default 樣式
    validator: (value) =>
      ['primary', 'success', 'warning', 'danger', 'info', 'text', 'default'].includes(value),
  },
  /**
   * 按鈕大小。
   * 映射到 Element Plus 的 'size'
   * 可選值：'small', 'medium', 'large' (對應 Element Plus 的 'small', 'default', 'large')
   */
  size: {
    type: String,
    default: 'medium', // 預設為中等大小
    validator: (value) => ['small', 'medium', 'large'].includes(value),
  },
  /**
   * 是否禁用按鈕。
   */
  disabled: {
    type: Boolean,
    default: false,
  },
  /**
   * 是否為圓角按鈕 (Element Plus 特有屬性)
   */
  round: {
    type: Boolean,
    default: false,
  },
  /**
   * 是否為圓形按鈕 (Element Plus 特有屬性)
   */
  circle: {
    type: Boolean,
    default: false,
  },
  /**
   * 是否為樸素按鈕 (Element Plus 特有屬性)
   */
  plain: {
    type: Boolean,
    default: false,
  },
  /**
   * 是否顯示加載狀態 (Element Plus 特有屬性)
   */
  loading: {
    type: Boolean,
    default: false,
  },
})

// 2. 定義 emits
const emit = defineEmits(['click'])

// 3. 映射您的 prop 到 ElButton 的 prop
// 注意：這裡的映射確保您的 Button.vue 的 props 和 Element Plus 的 ElButton props 一致性
const mappedType = computed(() => {
  // Element Plus 的 type 屬性包含 'primary', 'success', 'warning', 'danger', 'info', 'text'
  return props.type // 直接使用，因為我們的 type prop 已經和 Element Plus 的 type 屬性保持一致
})

const mappedSize = computed(() => {
  // Element Plus 的 size 屬性為 'large', 'default', 'small'
  switch (props.size) {
    case 'small':
      return 'small'
    case 'medium':
      return 'default' // Element Plus 的 'default' 是中等大小
    case 'large':
      return 'large'
    default:
      return 'default'
  }
})

// 4. 處理點擊事件
const handleClick = (event) => {
  // ElButton 自己會處理 disabled 和 loading 狀態下的點擊。
  // 但保留這一層可以在 future 加一些額外邏輯。
  if (!props.disabled && !props.loading) {
    // 確保禁用和加載狀態下不觸發點擊
    emit('click', event)
  }
}
</script>

<style scoped></style>
