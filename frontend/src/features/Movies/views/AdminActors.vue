<template>
  <div class="el-container el-main">
  
    <div v-if="isMoviesLoading" class="el-loading-mask">
      </div>
    <el-alert v-else-if="moviesError" :title="moviesError.message || '無法載入電影資料。'" type="error" show-icon closable>
    </el-alert>
    <div v-else-if="movies.length">
      <!-- <h2 class="el-header">選擇電影以管理演員/卡司</h2> -->
      <el-table :data="movies" style="width: 100%" border stripe>
        <el-table-column prop="id" label="電影 ID" width="100"></el-table-column>
        <el-table-column prop="titleLocal" label="電影中文名稱"></el-table-column>
        <el-table-column prop="titleEnglish" label="電影英文名稱"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="handleManageCast(scope.row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-else>
      <el-empty description="目前沒有找到任何電影資料。">
        <p>請先匯入電影資料。</p>
      </el-empty>
    </div>

    <el-dialog v-model="dialogCastVisible" :title="`管理 ${currentMovieTitle} 的卡司`" width="700px" @close="resetNewActorForm">
      <div v-if="isCastLoading" class="el-loading-mask">
        </div>
      <el-alert v-else-if="castError" :title="castError.message || '無法載入卡司資料。'" type="error" show-icon closable>
      </el-alert>
      
      <div v-else>
        <h3 class="mb-3 text-lg font-semibold">目前卡司</h3>
        <el-table :data="currentMovieCast" style="width: 100%" border stripe>
          <el-table-column prop="actorName" label="演員姓名" width="180"></el-table-column>
          <el-table-column label="飾演角色">
            <template #default="scope">
              <el-input v-model="scope.row.characterName"></el-input>
            </template>
          </el-table-column>
          <el-table-column prop="orderNum" label="出場順序" width="100"></el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="danger" size="small" @click="handleRemoveActorFromCast(scope.row.actorId)">刪除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!currentMovieCast.length" description="該電影目前沒有演員卡司資料。"></el-empty>
      </div>

      <el-divider />

      <div>
        <h3 class="mb-3 text-lg font-semibold">新增演員至卡司</h3>
        <el-form :model="newActorData" label-width="80px" class="flex items-center space-x-4">
          <el-form-item label="選擇演員" class="flex-grow">
            <el-select
              v-model="newActorData.actorId"
              filterable
              remote
              reserve-keyword
              placeholder="請輸入演員姓名搜尋"
              :remote-method="searchActors"
              :loading="isActorsLoading"
              style="width: 100%;"
            >
              <el-option
                v-for="actor in actors"
                :key="actor.actorId"
                :label="actor.name"
                :value="actor.actorId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="飾演角色" class="flex-grow">
            <el-input v-model="newActorData.characterName" placeholder="請輸入角色名稱"></el-input>
          </el-form-item>
          <el-button type="success" @click="handleAddActorToCast">新增演員</el-button>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogCastVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCastChanges">保存角色修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useMovieStore } from '../store/useMovieStore';
import { storeToRefs } from 'pinia';
import Swal from 'sweetalert2';
import { ElMessageBox } from 'element-plus';

const movieStore = useMovieStore();
// 解構電影和【所有演員】列表相關狀態
const { movies, isMoviesLoading, moviesError, actors, isActorsLoading } = storeToRefs(movieStore);

// 卡司管理相關狀態
const dialogCastVisible = ref(false);
const currentManagingMovieId = ref(null);
const currentMovieTitle = ref('');
const currentMovieCast = ref([]);
const isCastLoading = ref(false);
const castError = ref(null);

//const castDialogRef = ref(null); // 用於獲取對話框實例
// 【新增3】新增演員表單的狀態
const newActorData = ref({
  actorId: null,
  characterName: ''
});

onMounted(() => {
  movieStore.fetchAllMovies(); // 獲取所有電影
  movieStore.fetchAllActors(); // 【新增4】獲取所有演員，用於下拉選單
});

// 【新增5】重置新增演員表單的函式
function resetNewActorForm() {
  newActorData.value.actorId = null;
  newActorData.value.characterName = '';
}

// 處理點擊「管理卡司」按鈕
async function handleManageCast(movie) {
  currentManagingMovieId.value = movie.id;
  currentMovieTitle.value = movie.titleLocal || movie.titleEnglish;
  dialogCastVisible.value = true;
  await refreshCurrentMovieCast(); // 呼叫獨立的刷新函式
}

// 【新增6】將刷新卡司列表的邏輯獨立出來，方便重複使用
async function refreshCurrentMovieCast() {
  isCastLoading.value = true;
  castError.value = null;
  try {
    const cast = await movieStore.getMovieCast(currentManagingMovieId.value);
    currentMovieCast.value = cast;
  } catch (error) {
    castError.value = error;
    Swal.fire('載入卡司失敗！', '無法載入電影卡司：' + (error.response?.data?.message || error.message), 'error');
  } finally {
    isCastLoading.value = false;
  }
}

// 【新增7】處理新增演員到卡司
async function handleAddActorToCast() {
  const { actorId, characterName } = newActorData.value;
  if (!actorId) {
    Swal.fire('請選擇演員', '你必須先選擇一位演員才能新增。', 'warning');
    return;
  }
  if (!characterName.trim()) {
    Swal.fire('請填寫角色', '請為這位演員填寫飾演的角色名稱。', 'warning');
    return;
  }

  // 防止重複加入同一個演員
  if (currentMovieCast.value.some(castMember => castMember.actorId === actorId)) {
    Swal.fire('演員已存在', '這位演員已經在卡司列表裡了。', 'info');
    return;
  }

  try {
    // 假設 store 有一個 addActorToMovie 的 action
    await movieStore.addActorToMovie(currentManagingMovieId.value, actorId, characterName);
    // Swal.fire('新增成功', '演員已成功加入卡司列表。', 'success');
    resetNewActorForm(); // 清空表單
    await refreshCurrentMovieCast(); // 重新載入卡司列表
  } catch (error) {
    Swal.fire('新增失敗！', '無法新增演員：' + (error.response?.data?.message || error.message), 'error');
  }
}

// 【新增8】處理從卡司中刪除演員
async function handleRemoveActorFromCast(actorId) {
  try {
    await ElMessageBox.confirm(
      '將從這部電影的卡司中移除此演員。', // message
      '確定要刪除嗎？', // title
      {
        confirmButtonText: '是的，刪除！',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    // 如果使用者點擊了「確認」，程式碼會繼續往下執行
    // 如果點擊「取消」，ElMessageBox 會拋出一個錯誤，直接跳到下面的 catch 區塊
    
    // 只有在確認後才會執行的刪除邏輯
    try {
      await movieStore.removeActorFromMovie(currentManagingMovieId.value, actorId);
      // 刪除成功後，我們仍然可以使用 Swal 來顯示一個簡單的成功提示
      // Swal.fire('刪除成功', '演員已從卡司列表中移除。', 'success');
      await refreshCurrentMovieCast();
    } catch (error) {
      Swal.fire('刪除失敗！', '無法刪除演員：' + (error.response?.data?.message || error.message), 'error');
    }

  } catch (action) {
    // 這個 catch 區塊會捕捉到使用者點擊「取消」或關閉對話框的行為
    console.log('使用者取消了刪除操作:', action);
  }
}

// 提交卡司修改 (只修改角色名稱)
async function submitCastChanges() {
  Swal.fire({
    title: "保存角色中...",
    html: "正在更新演員角色，請稍候...",
    allowOutsideClick: false,
    didOpen: () => Swal.showLoading()
  });

  try {
    // 使用 Promise.all 來並行處理所有更新請求，效率更高
    const updatePromises = currentMovieCast.value.map(actorRole => 
      movieStore.updateMovieActorRole(
        currentManagingMovieId.value,
        actorRole.actorId,
        actorRole.characterName
      )
    );
    await Promise.all(updatePromises);
    
    dialogCastVisible.value = false;
    Swal.fire('保存成功！', '電影卡司資訊已成功更新。', 'success');
  } catch (error) {
    Swal.fire('保存失敗！', '無法保存卡司：' + (error.response?.data?.message || error.message), 'error');
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
  background-color: #eaedf1;
}
.el-form-item {
  margin-bottom: 18px;
}
.el-dialog__body .el-form-item {
  margin-bottom: 18px;
}
:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important; /* 設定一個深灰色背景 */
  color: #e9eaeb !important; /* 設定淺灰色文字，形成對比 */
  font-weight: 600; /* 字體加粗 */
}
</style>