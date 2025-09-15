<template>
    <div class="el-container el-main">

  
      <!-- 載入狀態顯示 -->
      <div v-if="isMoviesLoading || isGenresLoading" class="el-loading-mask"> <!-- 修正點：檢查電影和類型的載入狀態 -->
        <div class="el-loading-spinner">
          <svg viewBox="25 25 50 50" class="circular">
            <circle cx="50" cy="50" r="20" fill="none" class="path"></circle>
          </svg>
          <p class="el-loading-text">載入電影及類型資料中...</p>
        </div>
      </div>
  
      <!-- 錯誤訊息顯示 -->
      <el-alert v-else-if="moviesError || genresError" :title="(moviesError?.message || genresError?.message) || '無法載入資料。'" type="error" show-icon closable>
      </el-alert>
  
      <!-- 電影列表顯示 (用於選擇要管理類型的電影) -->
      <div v-else-if="movies.length"> <!-- 修正點：綁定 movies 列表 -->
        <!-- <h2 class="el-header">所有電影列表</h2> -->
        <el-table :data="movies" style="width: 100%" border stripe> <!-- 修正點：:data="movies" -->
          <el-table-column prop="id" label="電影 ID" width="100"></el-table-column>
          <el-table-column prop="titleLocal" label="電影中文名稱"></el-table-column>
          <el-table-column prop="titleEnglish" label="電影英文名稱"></el-table-column>
          <el-table-column label="當前類型" min-width="200">
            <template #default="scope">
              <span v-if="scope.row.genres && scope.row.genres.length">
                {{ scope.row.genres.map(genre => genre.name).join('、') }}
              </span>
              <span v-else>無</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" @click="handleEditMovieGenres(scope.row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
  
      <!-- 沒有電影資料時的顯示 -->
      <div v-else>
        <el-empty description="目前沒有找到任何電影資料。">
          <p>請先匯入電影資料。</p>
        </el-empty>
      </div>
  
      <!-- 修改電影類型對話框 -->
      <el-dialog v-model="dialogGenreVisible" :title="`修改 ${currentMovieTitle} 的類型`" width="500px">
        <el-form :model="currentMovieGenres" label-width="120px" label-position="top">
          <el-form-item label="選擇類型">
            <el-select
              v-model="currentMovieGenres.selectedGenreIds"
              multiple
              placeholder="請選擇電影類型"
              style="width: 100%;"
              :loading="isGenresLoading"
            >
              <el-option
                v-for="genre in genres"
                :key="genre.genreId"
                :label="genre.name"
                :value="genre.genreId"
              ></el-option>
            </el-select>
            <el-alert v-if="genresError" :title="genresError.message || '無法載入類型資料。'" type="warning" show-icon :closable="false"></el-alert>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogGenreVisible = false">取消</el-button>
            <el-button type="primary" @click="submitGenreEdit">保存修改</el-button>
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
  // 修正點：解構 movies 相關狀態 (用於顯示電影列表)，以及 genres 相關狀態 (用於類型選擇器)
  onMounted(() => {
    movieStore.fetchAllMovies(); // 修正點：組件掛載時獲取所有電影
    movieStore.fetchAllGenres(); // 組件掛載時獲取所有類型 (用於選擇器)
  });
  const { movies, isMoviesLoading, moviesError, genres, isGenresLoading, genresError } = storeToRefs(movieStore); 
  
  console.log('genres:', genres.value); // 確認 genres 是否正確獲取

  // 修改類型相關的狀態
  const dialogGenreVisible = ref(false); // 控制修改類型對話框的顯示
  const currentMovieGenres = ref({ // 當前正在編輯類型的電影物件
    movieId: null,
    selectedGenreIds: []
  }); 
  const currentMovieTitle = ref(''); // 當前修改類型的電影標題
  
  
  // 處理點擊「修改類型」按鈕
  async function handleEditMovieGenres(movie) {
    currentMovieTitle.value = movie.titleLocal || movie.titleEnglish; // 顯示電影標題
  
    Swal.fire({
      title: "載入類型中...",
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });
  
    try {
      // 獲取當前電影的類型 ID 列表
      // movie.genres 應該已經被後端回傳在 MovieResponseDto 中
      const currentGenreIds = movie.genres ? movie.genres.map(g => g.genreId) : [];
  
      currentMovieGenres.value.movieId = movie.id;
      currentMovieGenres.value.selectedGenreIds = currentGenreIds;
      
      dialogGenreVisible.value = true; // 顯示類型修改對話框
      Swal.close();
    } catch (error) {
      Swal.fire(
        '載入類型失敗！',
        '無法載入電影類型：' + (error.response?.data || error.message),
        'error'
      );
    }
  }
  
  // 提交修改電影類型表單
  async function submitGenreEdit() {
    Swal.fire({
      title: "更新中...",
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });
  
    try {
      await movieStore.updateMovieGenres(currentMovieGenres.value.movieId, currentMovieGenres.value.selectedGenreIds);
      
      dialogGenreVisible.value = false; // 關閉對話框
      Swal.fire(
        '已更新！',
        '電影類型已成功更新。',
        'success'
      );
    } catch (error) {
      Swal.fire(
        '更新失敗！',
        '無法更新電影類型：' + (error.response?.data || error.message),
        'error'
      );
    }
  }
  </script>
  
  <style scoped>
  .el-container {
    padding: 20px;
    background-color: #eaedf1;
  }
  .el-header {
    margin-bottom: 20px;
  }
  .el-table {
    /* margin-top: 20px; */
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
  