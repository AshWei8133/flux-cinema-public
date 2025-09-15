<template>
  <div class="el-container el-main">


    <!-- 新增導演按鈕 -->
    <div class="mb-4">
      <el-button type="primary" @click="handleAddNewDirector">新增導演</el-button>
    </div>

    <!-- 載入狀態顯示 -->
    <div v-if="isDirectorsLoading" class="el-loading-mask">
      <div class="el-loading-spinner">
        <svg viewBox="25 25 50 50" class="circular">
          <circle cx="50" cy="50" r="20" fill="none" class="path"></circle>
        </svg>
        <p class="el-loading-text">載入導演資料中...</p>
      </div>
    </div>

    <!-- 錯誤訊息顯示 -->
    <el-alert v-else-if="directorsError" :title="directorsError.message || '無法載入導演資料。'" type="error" show-icon closable>
    </el-alert>

    <!-- 導演列表顯示 -->
    <div v-else-if="directors.length">
      <!-- <h2 class="el-header">所有導演列表</h2> -->
      <el-table :data="directors" style="width: 100%" border stripe>
        <el-table-column prop="directorId" label="ID" width="80"></el-table-column>
        <el-table-column prop="tmdbDirectorId" label="TMDB ID" width="100"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <!-- <el-table-column prop="directorSummary" label="簡介"></el-table-column> -->
        <el-table-column label="關聯電影 (中文名稱)" min-width="200"> <!-- 修正點：新增關聯電影欄位 -->
          <template #default="scope">
            <span v-if="scope.row.associatedMovies && scope.row.associatedMovies.length">
              {{ scope.row.associatedMovies.map(movie => movie.titleLocal).join('、') }}
            </span>
            <span v-else>無</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleEditDirector(scope.row)">修改</el-button>
            <el-button size="small" type="danger" @click="handleDeleteDirector(scope.row.directorId)">刪除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 沒有導演資料時的顯示 -->
    <div v-else>
      <el-empty description="目前沒有找到任何導演資料。">
        <p>請先新增導演。</p>
      </el-empty>
    </div>

    <!-- 修改導演資訊的對話框 -->
    <el-dialog v-model="dialogVisible" title="修改導演資訊" width="500px">
      <el-form :model="currentDirector" label-width="100px" label-position="top">
        <el-form-item label="導演姓名">
          <el-input v-model="currentDirector.name"></el-input>
        </el-form-item>
        <el-form-item label="導演簡介">
          <el-input v-model="currentDirector.directorSummary" type="textarea"></el-input>
        </el-form-item>
        <!-- tmdbDirectorId 通常不允許修改 -->
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">保存修改</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增導演的對話框 -->
    <el-dialog v-model="dialogAddVisible" title="新增導演" width="500px">
      <el-form :model="newDirector" label-width="100px" label-position="top">
        <el-form-item label="導演姓名" required>
          <el-input v-model="newDirector.name"></el-input>
        </el-form-item>
        <el-form-item label="導演簡介">
          <el-input v-model="newDirector.directorSummary" type="textarea"></el-input>
        </el-form-item>
        <!-- tmdbDirectorId 通常由匯入功能設定，手動新增時可選填或不填 -->
        <el-form-item label="TMDB ID (選填)">
          <el-input-number v-model="newDirector.tmdbDirectorId" :min="0" style="width: 100%;"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogAddVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdd">新增</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useMovieStore } from '../store/useMovieStore'; // 請確認您的 store 路徑
import { storeToRefs } from 'pinia';
import Swal from 'sweetalert2'; // 導入 SweetAlert2

const movieStore = useMovieStore();
const { directors, isDirectorsLoading, directorsError } = storeToRefs(movieStore); // 解構導演相關狀態

const dialogVisible = ref(false); // 控制修改對話框的顯示
const currentDirector = ref({}); // 當前正在編輯的導演物件

const dialogAddVisible = ref(false); // 控制新增對話框的顯示
const newDirector = ref({}); // 新增導演的物件

onMounted(() => {
  movieStore.fetchAllDirectors(); // 組件掛載時獲取所有導演
});

// 處理點擊「新增導演」按鈕
function handleAddNewDirector() {
  newDirector.value = {
    name: '',
    directorSummary: '',
    tmdbDirectorId: null,
  };
  dialogAddVisible.value = true;
}

// 提交新增導演表單
async function submitAdd() {
  if (!newDirector.value.name) {
    Swal.fire('錯誤', '導演姓名不能為空！', 'error');
    return;
  }

  Swal.fire({
    title: "新增中...",
    allowOutsideClick: false,
    didOpen: () => {
      Swal.showLoading();
    },
  });

  try {
    await movieStore.createDirector(newDirector.value);
    dialogAddVisible.value = false;
    Swal.fire('已新增！', '導演已成功新增。', 'success');
  }
  // 修正點：新增處理批量匯入的錯誤訊息
  catch (error) {
    Swal.fire(
      '新增失敗！',
      '無法新增導演：' + (error.response?.data || error.message),
      'error'
    );
  }
}

// 處理點擊修改按鈕
function handleEditDirector(director) {
  currentDirector.value = JSON.parse(JSON.stringify(director)); // 深拷貝
  dialogVisible.value = true;
}

// 提交修改導演表單
async function submitEdit() {
  if (!currentDirector.value.name) {
    Swal.fire('錯誤', '導演姓名不能為空！', 'error');
    return;
  }

  Swal.fire({
    title: "更新中...",
    allowOutsideClick: false,
    didOpen: () => {
      Swal.showLoading();
    },
  });

  try {
    await movieStore.updateDirector(currentDirector.value.directorId, currentDirector.value);
    dialogVisible.value = false;
    Swal.fire('已更新！', '導演資訊已成功更新。', 'success');
  } catch (error) {
    Swal.fire(
      '更新失敗！',
      '無法更新導演：' + (error.response?.data || error.message),
      'error'
    );
  }
}

// 處理刪除導演
async function handleDeleteDirector(directorId) {
  const result = await Swal.fire({
    title: '確定要刪除這位導演嗎？',
    text: "此操作無法復原！",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#d33',
    confirmButtonText: '是的，刪除它！',
    cancelButtonText: '取消'
  });

  if (result.isConfirmed) {
    Swal.fire({
      title: "刪除中...",
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });
    try {
      await movieStore.deleteDirector(directorId);
      Swal.fire(
        '已刪除！',
        '導演已成功刪除。',
        'success'
      );
    } catch (error) {
      Swal.fire(
        '刪除失敗！',
        '無法刪除導演：' + (error.response?.data || error.message),
        'error'
      );
    }
  }
}
</script>

<style scoped>
/* 這裡放置全局或組件共用的 Element Plus 樣式覆蓋 */
.el-container {
  padding: 20px;
  background-color: #eaedf1;
}
.el-header {
  margin-bottom: 20px;
}
.el-table {
  margin-top: 20px;
}
/* Element Plus 表單行內樣式調整 */
.el-form-item {
  margin-bottom: 18px; /* 預設間距 */
}
.el-dialog__body .el-form-item {
  margin-bottom: 18px; /* 對話框內表單項間距 */
}
:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important; /* 設定一個深灰色背景 */
  color: #e9eaeb !important; /* 設定淺灰色文字，形成對比 */
  font-weight: 600; /* 字體加粗 */
}
</style>