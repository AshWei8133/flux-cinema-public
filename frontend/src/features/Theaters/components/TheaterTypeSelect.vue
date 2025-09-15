<template>
  <AdminSelect
    v-model="selectedTheaterTypeId"
    placeholder="請選擇影廳類別"
    :options="theaterTypes"
    value-key="theaterTypeId"
    label-key="theaterTypeName"
    :is-loading="isTheaterTypesLoading"
    :is-disabled="isTheaterTypesLoading"
  />
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import AdminSelect from '@/components/admin/AdminSelect.vue'
import { useTheaterStore } from '../store/useTheaterStore'

// 定義 Props 與 Emits
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: null,
  },
})

const emit = defineEmits(['update:modelValue'])

// 從 Pinia Store 獲取影廳類別資料
const theaterStore = useTheaterStore()
const { theaterTypes, isTheaterTypesLoading } = storeToRefs(theaterStore)

// 處理 v-model 雙向綁定
const selectedTheaterTypeId = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

// 載入影廳類別資料
onMounted(() => {
  if (theaterTypes.value.length === 0) {
    theaterStore.fetchAllTheaterTypes()
  }
})
</script>
