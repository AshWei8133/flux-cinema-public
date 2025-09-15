<template>
  <div class="el-container el-main">
  
    <div class="mb-4 p-4 border rounded-lg bg-gray-50">
      <h2 class="text-xl font-semibold mb-3">從 TMDB API 匯入電影</h2>
      <el-form :inline="true" :model="tmdbImportParams" class="demo-form-inline">
        <el-form-item label="起始日期">
          <el-date-picker
            v-model="tmdbImportParams.startDate"
            type="date"
            placeholder="選擇起始日期"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="結束日期">
          <el-date-picker
            v-model="tmdbImportParams.endDate"
            type="date"
            placeholder="選擇結束日期"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="地區代碼">
          <el-input v-model="tmdbImportParams.region" placeholder="例如: TW"></el-input>
        </el-form-item>

        <el-form-item label="匯入頁碼">
          <el-input-number v-model="tmdbImportParams.page" :min="1" :max="tmdbImportParams.totalPages || 1" controls-position="right" style="width: 120px;"/>
        </el-form-item>
        <el-form-item>
           <span v-if="tmdbImportParams.totalPages > 0" class="ml-2 text-gray-500">
            / 共 {{ tmdbImportParams.totalPages }} 頁
          </span>
        </el-form-item>

        <el-form-item>
          <el-button type="success" @click="handleTriggerTmdbImport" :loading="isImporting">
            執行 API 匯入列表
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="mb-4 flex space-x-2">
      <el-button type="primary" @click="handleImportSelectedMovies" :loading="isImporting" :disabled="selectedTmdbMovieIds.length === 0">
        選定電影匯入至本地 ({{ selectedTmdbMovieIds.length }})
      </el-button>
      <el-button type="info" @click="movieStore.fetchAllTmdbMovies()" :loading="isTmdbMoviesLoading">
        重新整理
      </el-button>
    </div>

    <div v-if="isTmdbMoviesLoading" class="el-loading-mask">
      <div class="el-loading-spinner">
        <svg viewBox="25 25 50 50" class="circular">
          <circle cx="50" cy="50" r="20" fill="none" class="path"></circle>
        </svg>
        <p class="el-loading-text">載入 TMDB 電影資料中...</p>
      </div>
    </div>

    <el-alert v-else-if="tmdbMoviesError" :title="tmdbMoviesError.message || '無法載入 TMDB 電影資料。'" type="error" show-icon closable>
    </el-alert>
    <el-alert v-else-if="importError" :title="importError.message || '匯入操作失敗。'" type="error" show-icon closable>
    </el-alert>

    <div v-else-if="tmdbMovies && tmdbMovies.length">
      <h2 class="el-header">TMDB 電影列表</h2>
      <el-table 
        :data="tmdbMovies" 
        style="width: 100%" 
        border 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="tmdbMovieId" label="TMDB ID" width="100"></el-table-column>
        <el-table-column label="海報" width="100">
          <template #default="scope">
            <img 
              :src="getTmdbPosterUrl(scope.row.posterImage)" 
              alt="電影海報" 
              style="width: 70px; height: 100px; object-fit: cover; border-radius: 4px;"
              onerror="this.onerror=null;this.src='https://placehold.co/60x90/cccccc/333333?text=無海報';"
            />
          </template>
        </el-table-column>
        <el-table-column prop="titleLocal" label="本地片名"></el-table-column>
        <el-table-column prop="titleEnglish" label="英文片名"></el-table-column>
        <el-table-column prop="releaseDate" label="上映日期" width="120"></el-table-column>
        <el-table-column prop="durationMinutes" label="片長 (分鐘)" width="120"></el-table-column>
        <el-table-column prop="popularity" label="熱門度" width="100"></el-table-column>
        <el-table-column prop="voteAverage" label="評分" width="80"></el-table-column>
        <el-table-column prop="voteCount" label="票數" width="80"></el-table-column>
        <el-table-column prop="originalLanguage" label="原始語言" width="100"></el-table-column>
      </el-table>
    </div>

    <div v-else>
      <el-empty description="目前沒有找到任何 TMDB 電影資料。">
        <p>請先從 TMDB API 匯入原始電影資料。</p>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useMovieStore } from '../store/useMovieStore';
import { storeToRefs } from 'pinia';
import Swal from 'sweetalert2';

const movieStore = useMovieStore();
const { tmdbMovies, isTmdbMoviesLoading, tmdbMoviesError, isImporting, importError } = storeToRefs(movieStore);

const selectedTmdbMovieIds = ref([]);

// 【修改4】TMDB API 匯入參數新增 page 和 totalPages
const tmdbImportParams = ref({
  startDate: '2025-01-01',
  endDate: '2025-12-31',
  region: 'TW',
  page: 1, // 預設匯入第 1 頁
  totalPages: 0 // 初始總頁數為 0
});

onMounted(() => {
  movieStore.fetchAllTmdbMovies();
});

const handleSelectionChange = (selection) => {
  selectedTmdbMovieIds.value = selection.map(movie => movie.tmdbMovieId);
};

const getTmdbPosterUrl = (base64ImageData) => {
  if (base64ImageData) {
    return `data:image/jpeg;base64,${base64ImageData}`;
  }
  return 'https://placehold.co/60x90/cccccc/333333?text=無海報';
};

async function handleImportSelectedMovies() {
    if (selectedTmdbMovieIds.value.length === 0) {
    Swal.fire('請選擇電影', '請至少選擇一部電影才能匯入。', 'warning');
    return;
  }

  const result = await Swal.fire({
    title: '確定要匯入選定電影嗎？',
    text: `您將匯入 ${selectedTmdbMovieIds.value.length} 部電影到本地 Movie 表。`,
    icon: 'info',
    showCancelButton: true,
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#d33',
    confirmButtonText: '是的，開始匯入！',
    cancelButtonText: '取消'
  });

  if (result.isConfirmed) {
    Swal.fire({
      title: "匯入中...",
      html: "這可能需要一些時間，請耐心等待...",
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });
    try {
      await movieStore.importSelectedTmdbMovies(selectedTmdbMovieIds.value);
      Swal.fire(
        '匯入完成！',
        '選定電影已成功匯入到本地 Movie 表。',
        'success'
      );
      selectedTmdbMovieIds.value = [];
    } catch (error) {
      Swal.fire(
        '匯入失敗！',
        '無法匯入電影：' + (error.response?.data || error.message),
        'error'
      );
    }
  }
}

// 【修改5】更新觸發匯入的函式
async function handleTriggerTmdbImport() {
  console.log('準備執行匯入，目前的參數:', tmdbImportParams.value);
  const { startDate, endDate, region, page } = tmdbImportParams.value;

  if (!startDate || !endDate || !region) {
    Swal.fire('缺少參數', '請填寫起始日期、結束日期和地區代碼。', 'warning');
    return;
  }

  const result = await Swal.fire({
    title: `確定要從 TMDB API 匯入第 ${page} 頁嗎？`,
    html: `這將會匯入 ${startDate} 至 ${endDate} 在 ${region} 上映的電影到 TMDBMovie 表。<br>這可能需要一些時間。`,
    icon: 'info',
    showCancelButton: true,
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#d33',
    confirmButtonText: '是的，開始匯入！',
    cancelButtonText: '取消'
  });

  if (result.isConfirmed) {
    Swal.fire({
      title: "匯入中...",
      html: "正在從 TMDB API 獲取資料並匯入，請耐心等待...",
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });
    try {
      const response = await movieStore.fetchAndImportTmdbMovies(startDate, endDate, region, page);
      
      console.log('從 Store Action 收到的原始 Response:', response);

      // 使用從 JSON 拿到的正確 key: total_pages
      if (response && response.total_pages) {
        tmdbImportParams.value.totalPages = response.total_pages;
        console.log("成功更新總頁數為: " + response.total_pages);
      } else {
        // 更新警告訊息，讓它和 if 判斷一致
        console.warn('Response 物件不存在，或缺少 total_pages 屬性。');
      }

      Swal.fire(
        '匯入成功！',
        `第 ${page} 頁的 TMDB 電影資料已成功匯入並刷新列表。`,
        'success'
      );
    } catch (error) {
      Swal.fire(
        '匯入失敗！',
        '無法從 TMDB API 匯入電影：' + (error.response?.data || error.message),
        'error'
      );
    }
  }
}
</script>

<style scoped>
.el-container {
  padding: 20px;
}
.el-header {
  margin-bottom: 20px;
}
.el-table {
  margin-top: 20px;
}
.el-upload__tip {
  margin-top: 5px;
  font-size: 12px;
  color: #606266;
}
.demo-form-inline .el-form-item {
  margin-right: 15px;
  margin-bottom: 0;
}
</style>