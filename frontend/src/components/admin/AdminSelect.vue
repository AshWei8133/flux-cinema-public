<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    clearable
    :loading="isLoading"
    :disabled="isDisabled"
    @update:modelValue="$emit('update:modelValue', $event)"
  >
    <el-option
      v-for="option in options"
      :key="option[valueKey]"
      :label="option[labelKey]"
      :value="option[valueKey]"
    ></el-option>
  </el-select>
</template>

<script setup>
// 定義 Props
const props = defineProps({
  modelValue: {
    type: [String, Number, Object, Array],
    default: null,
  },
  placeholder: {
    type: String,
    default: '請選擇',
  },
  options: {
    type: Array,
    required: true,
  },
  valueKey: {
    type: String,
    default: 'value',
  },
  labelKey: {
    type: String,
    default: 'label',
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  isDisabled: {
    type: Boolean,
    default: false,
  },
})

// 定義 Emits
const emit = defineEmits(['update:modelValue'])
</script>

<style scoped>
.el-select {
  width: 100%;
}
</style>
