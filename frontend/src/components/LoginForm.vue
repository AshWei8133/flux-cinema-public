<template>
  <el-form
    :model="loginForm"
    :rules="rules"
    ref="formRef"
    label-width="80px"
    @submit.prevent="handleSubmit"
  >
    <el-form-item label="帳號" prop="adminName">
      <InputField v-model="loginForm.adminName" placeholder="請輸入帳號" />
    </el-form-item>

    <el-form-item label="密碼" prop="password">
      <InputField
        v-model="loginForm.password"
        type="password"
        show-password
        placeholder="請輸入密碼"
      />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" :loading="isLoading" @click="handleSubmit">登入</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive } from 'vue'
import InputField from './InputField.vue'

const props = defineProps(['isLoading'])
const emit = defineEmits(['submit'])

const formRef = ref(null)

const loginForm = reactive({
  adminName: '',
  password: '',
})

const rules = reactive({
  adminName: [
    { required: true, message: '請輸入帳號', trigger: 'blur' },
    { min: 3, max: 20, message: '帳號長度需在 3 到 20 個字元', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, message: '密碼長度至少為 6 個字元', trigger: 'blur' },
  ],
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    emit('submit', { ...loginForm })
  } catch (error) {
    console.error('表單驗證失敗:', error)
  }
}
</script>
