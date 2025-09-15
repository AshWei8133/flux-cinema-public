<template>
  <div class="el-container el-main">
    <!-- <h1 class="el-header">電影管理</h1> -->
    <div class="el-main">
      <!-- 操作按鈕區塊 -->
      <div class="mb-4 flex space-x-2">
        <el-button type="primary" @click="handleAddNewMovie">新增電影</el-button>
      </div>

      <!-- 載入狀態顯示 -->
      <div v-if="isMoviesLoading" class="el-loading-mask">
        <!-- ... loading spinner ... -->
      </div>

      <!-- 錯誤訊息顯示 -->
      <el-alert
        v-else-if="moviesError"
        :title="moviesError.message || '無法載入電影資料。'"
        type="error"
        show-icon
        closable
      >
      </el-alert>

      <!-- 電影列表顯示 -->
      <div v-else-if="movies.length" class="el-table-container">
        <!-- <h2 class="el-header">所有電影列表</h2> -->
        <el-table :data="movies" style="width: 100%" border stripe class="el-table-tr">
          <!-- ... table columns ... -->
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column label="海報" width="100">
            <template #default="scope">
              <el-image
                style="width: 70px; height: 100px; border-radius: 4px"
                :src="posterUrls[scope.row.id]"
                fit="cover"
                lazy
              >
                <template #placeholder>
                  <div class="image-slot">載入中...</div>
                </template>
                <template #error>
                  <div class="image-slot">
                    <span>海報載入中...</span>
                  </div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="titleLocal" label="本地片名"></el-table-column>
          <el-table-column prop="titleEnglish" label="英文片名"></el-table-column>
          <el-table-column prop="releaseDate" label="上映日期" width="120"></el-table-column>
          <el-table-column prop="durationMinutes" label="片長 (分鐘)" width="120"></el-table-column>
          <el-table-column prop="certification" label="分級" width="80"></el-table-column>
          <!-- <el-table-column prop="voteAverage" label="評分" width="80"></el-table-column> -->
          <el-table-column label="狀態" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status ? 'success' : 'info'">
                {{ scope.row.status ? '上架中' : '已下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" @click="handleEditMovie(scope.row)">修改</el-button>
              <el-button size="small" type="danger" @click="handleDeleteMovie(scope.row.id)"
                >刪除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 沒有電影資料時的顯示 -->
      <div v-else>
        <el-empty description="目前沒有找到任何電影資料。">
          <p>請確認後端資料庫中已有電影。</p>
        </el-empty>
      </div>

      <!-- 修改電影資訊的對話框 -->
      <el-dialog v-model="dialogVisible" title="修改電影資訊" width="700px">
        <el-form :model="currentMovie" label-width="120px" label-position="top">
          <!-- ... 其他表單項目 ... -->
          <el-form-item label="本地片名">
            <el-input v-model="currentMovie.titleLocal"></el-input>
          </el-form-item>
          <el-form-item label="英文片名">
            <el-input v-model="currentMovie.titleEnglish"></el-input>
          </el-form-item>
          <el-form-item label="上映日期">
            <el-date-picker
              v-model="currentMovie.releaseDate"
              type="date"
              placeholder="選擇日期"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="分級">
            <el-select
              v-model="currentMovie.certification"
              placeholder="請選擇分級"
              style="width: 100%"
            >
              <el-option
                v-for="item in certificationOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="預告片連結">
            <el-input v-model="currentMovie.trailerUrl"></el-input>
          </el-form-item>
          <el-form-item label="劇情簡介">
            <el-input v-model="currentMovie.overview" type="textarea"></el-input>
          </el-form-item>
          <el-form-item label="是否上架">
            <el-switch v-model="currentMovie.status"></el-switch>
          </el-form-item>
          <!-- 【已修改】更換海報區塊 -->
          <el-form-item label="更換海報">
            <!-- 新增的圖片預覽區 -->
            <el-image
              style="
                width: 150px;
                height: 225px;
                margin-bottom: 10px;
                border-radius: 6px;
                background-color: #f5f7fa;
              "
              :src="posterPreviewSrc"
              fit="contain"
            >
              <template #error>
                <div class="image-slot"><span>目前無海報</span></div>
              </template>
            </el-image>
            <!-- 上傳工具 -->
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handlePosterImageChange"
              :limit="1"
              accept="image/*"
            >
              <el-button type="primary" plain size="small">選擇檔案</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  <span v-if="selectedFileName">{{ selectedFileName }}</span>
                  <span v-else>選擇新的圖片檔案</span>
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <!-- 【新增】預覽圖片上傳區塊 -->
          <el-divider content-position="left">預覽圖片</el-divider>
          <el-row :gutter="20">
            <el-col :span="12" v-for="index in 4" :key="index">
              <el-form-item :label="`預覽圖 ${index}`">
                <!-- 圖片預覽 -->
                <el-image
                  style="
                    width: 100%;
                    height: 150px;
                    margin-bottom: 10px;
                    border-radius: 6px;
                    background-color: #f5f7fa;
                  "
                  :src="previewImageSrcs[index - 1]"
                  fit="contain"
                >
                  <template #error>
                    <div class="image-slot"><span>無圖片</span></div>
                  </template>
                </el-image>
                <!-- 上傳工具 -->
                <div class="flex items-center">
                  <el-upload
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="(file) => handlePreviewImageChange(file, index)"
                    :limit="1"
                    accept="image/*"
                    class="flex-grow"
                  >
                    <el-button type="primary" plain size="small">選擇檔案</el-button>
                  </el-upload>
                  <el-button
                    type="danger"
                    plain
                    size="small"
                    @click="clearPreviewImage(index)"
                    class="ml-2"
                    title="清除此圖片"
                    >清除
                  </el-button>
                </div>
                <div class="el-upload__tip text-xs truncate">
                  {{ selectedPreviewFileNames[index - 1] || ' ' }}
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEdit">保存修改</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 【已完成】新增電影的對話框 -->
      <el-dialog v-model="dialogAddVisible" title="新增電影" width="700px">
        <el-form :model="newMovie" label-width="120px" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="本地片名">
                <el-input v-model="newMovie.titleLocal"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="英文片名">
                <el-input v-model="newMovie.titleEnglish"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="劇情簡介">
            <el-input v-model="newMovie.overview" type="textarea" :rows="4"></el-input>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="上映日期">
                <el-date-picker
                  v-model="newMovie.releaseDate"
                  type="date"
                  placeholder="選擇日期"
                  style="width: 100%"
                ></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="下檔日期">
                <el-date-picker
                  v-model="newMovie.offShelfDate"
                  type="date"
                  placeholder="選擇日期"
                  style="width: 100%"
                ></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="片長 (分鐘)">
                <el-input-number
                  v-model="newMovie.durationMinutes"
                  :min="0"
                  style="width: 100%"
                ></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="分級">
                <el-select
                  v-model="newMovie.certification"
                  placeholder="請選擇分級"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in certificationOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="是否上架">
                <el-switch v-model="newMovie.status"></el-switch>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 【已新增】關聯導演、演員和類型的選單 -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="選擇導演">
                <el-select
                  v-model="newMovie.directorIds"
                  multiple
                  filterable
                  placeholder="可搜尋並選擇多位導演"
                  style="width: 100%"
                >
                  <el-option
                    v-for="director in directors"
                    :key="director.directorId"
                    :label="director.name"
                    :value="director.directorId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="選擇演員">
                <el-select
                  v-model="newMovie.actorIds"
                  multiple
                  filterable
                  placeholder="可搜尋並選擇多位演員"
                  style="width: 100%"
                >
                  <el-option
                    v-for="actor in actors"
                    :key="actor.actorId"
                    :label="actor.name"
                    :value="actor.actorId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="選擇類型">
            <el-select
              v-model="newMovie.genreIds"
              multiple
              filterable
              placeholder="可搜尋並選擇多種類型"
              style="width: 100%"
            >
              <el-option
                v-for="genre in genres"
                :key="genre.genreId"
                :label="genre.name"
                :value="genre.genreId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="預告片連結">
            <el-input v-model="newMovie.trailerUrl"></el-input>
          </el-form-item>

          <el-form-item label="選擇海報">
            <el-image
              style="
                width: 150px;
                height: 225px;
                margin-bottom: 10px;
                border-radius: 6px;
                background-color: #f5f7fa;
              "
              :src="newMoviePosterPreviewSrc"
              fit="contain"
            >
              <template #error>
                <div class="image-slot"><span>請選擇海報</span></div>
              </template>
            </el-image>
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleNewMoviePosterChange"
              :limit="1"
              accept="image/*"
            >
              <el-button type="primary">選擇檔案</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  <span v-if="newMovieFileName">{{ newMovieFileName }}</span>
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <el-divider content-position="left">預覽圖片</el-divider>
          <el-row :gutter="20">
            <el-col :span="12" v-for="index in 4" :key="index">
              <el-form-item :label="`預覽圖 ${index}`">
                <el-image
                  style="
                    width: 100%;
                    height: 150px;
                    margin-bottom: 10px;
                    border-radius: 6px;
                    background-color: #f5f7fa;
                  "
                  :src="newMoviePreviewSrcs[index - 1]"
                  fit="contain"
                >
                  <template #error>
                    <div class="image-slot"><span>無圖片</span></div>
                  </template>
                </el-image>
                <el-upload
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="(file) => handleNewMoviePreviewChange(file, index)"
                  :limit="1"
                  accept="image/*"
                >
                  <el-button type="primary" plain size="small">選擇檔案</el-button>
                </el-upload>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogAddVisible = false">取消</el-button>
            <el-button type="primary" @click="submitAdd">新增</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useMovieStore } from '../store/useMovieStore'
import { storeToRefs } from 'pinia'
import Swal from 'sweetalert2'

const certificationOptions = ref([
  { value: '普遍級', label: '普遍級 (G)' },
  { value: '保護級', label: '保護級 (P)' },
  { value: '輔導級', label: '輔導級 (PG-12)' },
  { value: '限制級', label: '限制級 (R)' },
  { value: '待確定', label: '待確定' },
])

const movieStore = useMovieStore()
// 【已修改】從 store 中多解構出 directors, actors, 和 genres
const { movies, isMoviesLoading, moviesError, directors, actors, genres } = storeToRefs(movieStore)

const dialogVisible = ref(false)
const currentMovie = ref({})
const selectedFileBase64 = ref(null)
const selectedFileName = ref('')
const selectedPreviews = ref([null, null, null, null])
const selectedPreviewFileNames = ref(['', '', '', ''])

const dialogAddVisible = ref(false)
const newMovie = ref({})
const newMovieFileBase64 = ref(null)
const newMovieFileName = ref('')
const posterUrls = reactive({})
const newMoviePreviews = ref([null, null, null, null])
const newMoviePreviewFileNames = ref(['', '', '', ''])

onMounted(async () => {
  // 【已修改】頁面載入時，不僅獲取電影，也獲取所有導演、演員和類型的列表
  await movieStore.fetchAllMovies()

  if (movies.value && movies.value.length > 0) {
    const posterPromises = movies.value.map(async (movie) => {
      const blobUrl = await movieStore.getMoviePosterBlobUrl(movie.id)
      posterUrls[movie.id] = blobUrl
    })
    await Promise.all(posterPromises)
  }

  await movieStore.fetchAllDirectors()
  await movieStore.fetchAllActors()
  await movieStore.fetchAllGenres()
})

const posterPreviewSrc = computed(() => {
  if (selectedFileBase64.value) {
    return `data:image/jpeg;base64,${selectedFileBase64.value}`
  }
  if (currentMovie.value && currentMovie.value.posterImage) {
    return `data:image/jpeg;base64,${currentMovie.value.posterImage}`
  }
  return ''
})
const previewImageSrcs = computed(() => {
  if (!currentMovie.value) return ['', '', '', '']
  return [1, 2, 3, 4].map((index) => {
    const previewData = selectedPreviews.value[index - 1]
    if (previewData && previewData !== '__CLEAR__') {
      return `data:image/jpeg;base64,${previewData}`
    }
    if (currentMovie.value[`previewImage${index}`]) {
      return `data:image/jpeg;base64,${currentMovie.value[`previewImage${index}`]}`
    }
    return ''
  })
})
const newMoviePosterPreviewSrc = computed(() => {
  if (newMovieFileBase64.value) {
    return `data:image/jpeg;base64,${newMovieFileBase64.value}`
  }
  return ''
})
const newMoviePreviewSrcs = computed(() => {
  return newMoviePreviews.value.map((base64) => {
    return base64 ? `data:image/jpeg;base64,${base64}` : ''
  })
})

async function handleDeleteMovie(movieId) {
  const result = await Swal.fire({
    title: '確定要刪除這部電影嗎？',
    text: '此操作無法復原！',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#d33',
    confirmButtonText: '是的，刪除它！',
    cancelButtonText: '取消',
  })

  if (result.isConfirmed) {
    Swal.fire({
      title: '刪除中...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading()
      },
    })
    try {
      await movieStore.deleteMovie(movieId)
      location.reload()
    } catch (error) {
      Swal.fire('刪除失敗！', '無法刪除電影：' + (error.response?.data || error.message), 'error')
    }
  }
}

function handleEditMovie(movie) {
  currentMovie.value = JSON.parse(JSON.stringify(movie))
  if (currentMovie.value.releaseDate) {
    currentMovie.value.releaseDate = new Date(currentMovie.value.releaseDate)
  }
  if (currentMovie.value.offShelfDate) {
    currentMovie.value.offShelfDate = new Date(currentMovie.value.offShelfDate)
  }
  selectedFileBase64.value = null
  selectedFileName.value = ''
  selectedPreviews.value = [null, null, null, null]
  selectedPreviewFileNames.value = ['', '', '', '']
  dialogVisible.value = true
}

async function handlePosterImageChange(file) {
  if (file.raw) {
    try {
      selectedFileBase64.value = await readFileAsBase64(file.raw)
      selectedFileName.value = file.raw.name
    } catch (error) {
      console.error('讀取圖片檔案失敗：', error)
      Swal.fire('圖片讀取失敗', '無法讀取您選擇的圖片檔案。', 'error')
    }
  }
}

async function handlePreviewImageChange(file, index) {
  if (file.raw) {
    try {
      const base64 = await readFileAsBase64(file.raw)
      selectedPreviews.value[index - 1] = base64
      selectedPreviewFileNames.value[index - 1] = file.raw.name
    } catch (error) {
      console.error('讀取預覽圖片失敗：', error)
      Swal.fire('圖片讀取失敗', '無法讀取您選擇的圖片檔案。', 'error')
    }
  }
}

function clearPreviewImage(index) {
  selectedPreviews.value[index - 1] = '__CLEAR__'
  selectedPreviewFileNames.value[index - 1] = '已標記為清除'
  if (currentMovie.value) {
    currentMovie.value[`previewImage${index}`] = null
  }
}

function readFileAsBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const base64String = reader.result.split(',')[1]
      resolve(base64String)
    }
    reader.onerror = (error) => reject(error)
    reader.readAsDataURL(file)
  })
}

async function submitEdit() {
  Swal.fire({
    title: '更新中...',
    allowOutsideClick: false,
    didOpen: () => Swal.showLoading(),
  })
  try {
    const updatedData = { ...currentMovie.value }
    if (updatedData.releaseDate) {
      updatedData.releaseDate = updatedData.releaseDate.toISOString().split('T')[0]
    }
    if (updatedData.offShelfDate) {
      updatedData.offShelfDate = updatedData.offShelfDate.toISOString().split('T')[0]
    }
    if (selectedFileBase64.value) {
      updatedData.posterImage = selectedFileBase64.value
    }
    selectedPreviews.value.forEach((base64, index) => {
      const key = `previewImage${index + 1}`
      if (base64 === '__CLEAR__') {
        updatedData[key] = null
      } else if (base64) {
        updatedData[key] = base64
      }
    })
    await movieStore.updateMovie(updatedData.id, updatedData)
    location.reload()
  } catch (error) {
    Swal.fire('更新失敗！', '無法更新電影：' + (error.response?.data || error.message), 'error')
  }
}

function handleAddNewMovie() {
  newMovie.value = {
    titleLocal: '',
    titleEnglish: '',
    releaseDate: null,
    offShelfDate: null,
    certification: '待確定',
    overview: '',
    trailerUrl: '',
    durationMinutes: 90,
    originalLanguage: 'en',
    popularity: 0.0,
    status: false,
    // 【已修改】初始化 directorIds, actorIds, 和 genreIds 為空陣列
    directorIds: [],
    actorIds: [],
    genreIds: [],
  }
  newMovieFileBase64.value = null
  newMovieFileName.value = ''
  newMoviePreviews.value = [null, null, null, null]
  newMoviePreviewFileNames.value = ['', '', '', '']
  dialogAddVisible.value = true
}

async function handleNewMoviePosterChange(file) {
  if (file.raw) {
    try {
      newMovieFileBase64.value = await readFileAsBase64(file.raw)
      newMovieFileName.value = file.raw.name
    } catch (error) {
      console.error('讀取圖片檔案失敗：', error)
      Swal.fire('圖片讀取失敗', '無法讀取您選擇的圖片檔案。', 'error')
    }
  }
}

async function handleNewMoviePreviewChange(file, index) {
  if (file.raw) {
    try {
      const base64 = await readFileAsBase64(file.raw)
      newMoviePreviews.value[index - 1] = base64
    } catch (error) {
      console.error('讀取預覽圖片失敗：', error)
      Swal.fire('圖片讀取失敗', '無法讀取您選擇的圖片檔案。', 'error')
    }
  }
}
async function submitAdd() {
  Swal.fire({ title: '新增中...', allowOutsideClick: false, didOpen: () => Swal.showLoading() })
  try {
    const dataToCreate = { ...newMovie.value }
    if (dataToCreate.releaseDate) {
      dataToCreate.releaseDate = dataToCreate.releaseDate.toISOString().split('T')[0]
    }
    if (dataToCreate.offShelfDate) {
      dataToCreate.offShelfDate = dataToCreate.offShelfDate.toISOString().split('T')[0]
    }
    dataToCreate.posterImage = newMovieFileBase64.value

    newMoviePreviews.value.forEach((base64, index) => {
      dataToCreate[`previewImage${index + 1}`] = base64
    })

    await movieStore.createMovie(dataToCreate)
    location.reload()
  } catch (error) {
    Swal.fire('新增失敗！', '無法新增電影：' + (error.response?.data || error.message), 'error')
  }
}
</script>

<style scoped>
/* ... (樣式維持原樣) ... */
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #c0c4cc;
  font-size: 14px;
}
.el-table-container {
  margin-top: 20px;
}
.el-main {
  /* padding: 20px; */
  background-color: #eaedf1;
}
:deep(.el-table th.el-table__cell) {
  background-color: #484848 !important;
  color: #e9eaeb !important;
  font-weight: 600;
}
</style>
