<template>
  <form @submit.prevent="handleSubmit" class="product-form-container">
    <div class="form-group">
      <label for="productName">產品名稱:</label>
      <input type="text" id="productName" v-model="form.productName" placeholder="產品名稱" required />
    </div>

    <div class="form-group">
      <label for="categoryId">分類ID:</label>
      <input type="number" id="categoryId" v-model.number="form.categoryId" placeholder="分類ID" />
    </div>

    <div class="form-group">
      <label for="price">價格:</label>
      <input type="number" id="price" v-model.number="form.price" min="0" placeholder="價格" required />
    </div>

    <div class="form-group">
      <label for="description">描述:</label>
      <textarea id="description" v-model="form.description" placeholder="產品描述"></textarea>
    </div>

    <div class="form-group">
      <label for="imageUrl">圖片URL:</label>
      <input type="text" id="imageUrl" v-model="form.imageUrl" placeholder="圖片URL" />
    </div>

    <div class="form-group">
      <label for="stock">庫存:</label>
      <input type="number" id="stock" v-model.number="form.stock" min="0" placeholder="庫存數量" required />
    </div>

    <button type="submit" :disabled="isSubmitting">
      {{ isSubmitting ? '處理中...' : editMode ? '更新產品' : '新增產品' }}
    </button>
    <button type="button" @click="$emit('cancel')">取消</button>
  </form>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  product: Object,
  editMode: Boolean,
  isSubmitting: Boolean,
});

const emit = defineEmits(['submit', 'cancel']);

const form = ref({});

// 監聽傳入的 product 屬性，用於初始化表單
watch(
  () => props.product,
  (newProduct) => {
    if (props.editMode && newProduct) {
      form.value = { ...newProduct };
    } else {
      // 新增模式，重設表單
      form.value = {
        productName: '',
        categoryId: null,
        price: 0,
        description: '',
        imageUrl: '',
        stock: 0,
      };
    }
  },
  { immediate: true, deep: true }
);

const handleSubmit = () => {
  emit('submit', form.value);
};
</script>

<style scoped>
.product-form-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  max-width: 500px;
  margin: auto;
}
.form-group {
  display: flex;
  flex-direction: column;
}
input, textarea {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
button {
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background-color: #007bff;
  color: white;
  margin-top: 5px;
}
button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style><template>
  <form @submit.prevent="handleSubmit" class="product-form-container">
    <div class="form-group">
      <label for="productName">產品名稱:</label>
      <input type="text" id="productName" v-model="form.productName" placeholder="產品名稱" required />
    </div>

    <div class="form-group">
      <label for="categoryId">分類ID:</label>
      <input type="number" id="categoryId" v-model.number="form.categoryId" placeholder="分類ID" />
    </div>

    <div class="form-group">
      <label for="price">價格:</label>
      <input type="number" id="price" v-model.number="form.price" min="0" placeholder="價格" required />
    </div>

    <div class="form-group">
      <label for="description">描述:</label>
      <textarea id="description" v-model="form.description" placeholder="產品描述"></textarea>
    </div>

    <div class="form-group">
      <label for="imageUrl">圖片URL:</label>
      <input type="text" id="imageUrl" v-model="form.imageUrl" placeholder="圖片URL" />
    </div>

    <div class="form-group">
      <label for="stock">庫存:</label>
      <input type="number" id="stock" v-model.number="form.stock" min="0" placeholder="庫存數量" required />
    </div>

    <button type="submit" :disabled="isSubmitting">
      {{ isSubmitting ? '處理中...' : editMode ? '更新產品' : '新增產品' }}
    </button>
    <button type="button" @click="$emit('cancel')">取消</button>
  </form>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  product: Object,
  editMode: Boolean,
  isSubmitting: Boolean,
});

const emit = defineEmits(['submit', 'cancel']);

const form = ref({});

// 監聽傳入的 product 屬性，用於初始化表單
watch(
  () => props.product,
  (newProduct) => {
    if (props.editMode && newProduct) {
      form.value = { ...newProduct };
    } else {
      // 新增模式，重設表單
      form.value = {
        productName: '',
        categoryId: null,
        price: 0,
        description: '',
        imageUrl: '',
        stock: 0,
      };
    }
  },
  { immediate: true, deep: true }
);

const handleSubmit = () => {
  emit('submit', form.value);
};
</script>

<style scoped>
.product-form-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  max-width: 500px;
  margin: auto;
}
.form-group {
  display: flex;
  flex-direction: column;
}
input, textarea {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
button {
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background-color: #007bff;
  color: white;
  margin-top: 5px;
}
button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>