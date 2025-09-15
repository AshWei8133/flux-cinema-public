<template>
  <div class="stock-editor">
    <div class="stock-display">當前庫存: {{ currentStock }}</div>
    <div class="stock-form">
      <input type="number" v-model.number="newStock" min="0" />
      <button @click="handleUpdate">更新庫存</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  productId: Number,
  currentStock: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['update']);

const newStock = ref(props.currentStock);

// 監聽父組件的 currentStock 變化，更新本地狀態
watch(
  () => props.currentStock,
  (newVal) => {
    newStock.value = newVal;
  }
);

const handleUpdate = () => {
  if (newStock.value >= 0 && props.productId != null) {
    emit('update', props.productId, newStock.value);
  } else {
    alert('庫存值無效！');
  }
};
</script>

<style scoped>
.stock-editor {
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 8px;
  display: inline-flex;
  gap: 10px;
  align-items: center;
}
.stock-form {
  display: flex;
  gap: 5px;
}
</style>