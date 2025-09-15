// src/store/useMovieStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import MovieService from '@/features/Movies/services/MovieService' // 導入 MovieService.js
import axios from 'axios' // 這裡仍然需要 axiosapi 用於 TMDB 匯入
import httpClient from '@/services/api' // 導入 httpClient 以便使用 axiosapi

export const useMovieStore = defineStore('movie', () => {
  // 狀態 (State)
  const movies = ref([]) // 存放本地電影列表
  const isMoviesLoading = ref(false) // 本地電影載入狀態
  const moviesError = ref(null) // 本地電影錯誤訊息

  const tmdbMovies = ref([]) // 存放 TMDBMovie 列表
  const isTmdbMoviesLoading = ref(false) // TMDBMovie 載入狀態
  const tmdbMoviesError = ref(null) // TMDBMovie 錯誤訊息

  const isImporting = ref(false) // 匯入狀態 (用於 TMDB 匯入)
  const importError = ref(null) // 匯入錯誤訊息 (用於 TMDB 匯入)

  // Director 相關狀態
  const directors = ref([])
  const isDirectorsLoading = ref(false)
  const directorsError = ref(null)
  // 演員相關狀態
  const actors = ref([])
  const isActorsLoading = ref(false)
  const actorsError = ref(null)

  // 類型相關狀態
  const genres = ref([])
  const isGenresLoading = ref(false)
  const genresError = ref(null)

  // 後段擷取的現正熱映電影列表
  const nowPlayingMoviesFromAPI = ref([])
  const isNowPlayingMoviesLoading = ref(false)
  const nowPlayingMoviesError = ref(null)

  // --- 1. 在檔案頂部，與其他 ref() 一起，新增以下狀態 ---
  const nowShowingMovies = ref([]) // 用來存放篩選後的「現正熱映」電影
  const isLoading = ref(false) // 沿用或新增一個共用的載入狀態
  const error = ref(null) // 沿用或新增一個共用的錯誤狀態

  // 動作 (Actions)
  // 獲取所有本地電影資料
  // 動作 (Actions)
  // 獲取所有本地電影資料
  async function fetchAllMovies() {
    isMoviesLoading.value = true
    moviesError.value = null

    try {
      // 1. 從後端獲取原始的 DTO 列表，格式為: [{ movie: {...}, genres: [...] }, ...]
      const dtoList = await MovieService.getAllMovies()

      // 2. 使用 JavaScript 的 map 方法來轉換陣列中每個元素的格式
      const formattedMovies = dtoList.map((dto) => {
        // 對於列表中的每一個 dto 物件，我們建立一個新的物件
        // 這個新物件會包含 ...dto.movie 的所有屬性 (id, titleLocal 等)
        // 然後再新增一個 genres 屬性，值就是 dto.genres
        return {
          ...dto.movie,
          genres: dto.genres,
        }
      })

      // 3. 將轉換後、格式更簡潔的電影陣列，存入 movies 狀態中
      movies.value = formattedMovies
    } catch (error) {
      console.error('從後端獲取本地電影資料時發生錯誤：', error)
      moviesError.value = error
    } finally {
      isMoviesLoading.value = false
      console.log('fetchAllMovies 完成，轉換後的 movies 狀態:', movies.value) // 確認新格式
    }
  }

  // 獲取所有 TMDBMovie 資料
  // async function fetchAllTmdbMovies() {
  //   isTmdbMoviesLoading.value = true;
  //   tmdbMoviesError.value = null;

  //   try {
  //     tmdbMovies.value = await MovieService.getAllTmdbMovies();
  //   } catch (error) {
  //     console.error("從後端獲取 TMDBMovie 資料時發生錯誤：", error);
  //     tmdbMoviesError.value = error;
  //   } finally {
  //     isTmdbMoviesLoading.value = false;
  //   }
  // }

  // 將所有 TMDBMovie 的電影匯入到本地 Movie 表 (此動作仍直接呼叫 axiosapi，因為它屬於 TMDB 匯入邏輯)
  async function importAllMoviesToLocal() {
    isImporting.value = true
    importError.value = null

    try {
      const response = await httpClient.post('/admin/tmdb/import-all-to-local')
      console.log('批量匯入成功：', response.data)
      await fetchAllMovies() // 匯入成功後，重新獲取本地電影列表以更新顯示
      return true
    } catch (error) {
      console.error('批量匯入時發生錯誤：', error)
      importError.value = error
      throw error // 拋出錯誤以便組件處理
    } finally {
      isImporting.value = false
    }
  }

  // 匯入選定的 TMDBMovie 電影到本地 Movie 表
  async function importSelectedTmdbMovies(tmdbMovieIds) {
    isImporting.value = true
    importError.value = null

    try {
      await MovieService.importSelectedTmdbMovies(tmdbMovieIds) // 呼叫 MovieService
      console.log('選定電影匯入成功。')
      await fetchAllMovies() // 匯入成功後，重新獲取本地電影列表以更新顯示
      return true
    } catch (error) {
      console.error('匯入選定電影時發生錯誤：', error)
      importError.value = error
      throw error
    } finally {
      isImporting.value = false
    }
  }

  // 新增動作：觸發後端從 TMDB API 匯入電影並刷新 TMDBMovie 列表
  async function fetchAllTmdbMovies() {
    isTmdbMoviesLoading.value = true
    tmdbMoviesError.value = null

    try {
      // 1. 先從後端獲取原始的電影列表
      const moviesFromBackend = await MovieService.getAllTmdbMovies()

      // 2. 【核心】使用 JavaScript 的 sort 方法進行排序
      //    我們將 releaseDate (上映日期) 字串轉換成 Date 物件來比較
      //    b - a 會得到由新到舊 (降序) 的結果
      moviesFromBackend.sort((a, b) => new Date(b.releaseDate) - new Date(a.releaseDate))

      // 3. 將排序後的電影列表存入 Pinia 狀態中
      tmdbMovies.value = moviesFromBackend
      console.log('TMDB 電影列表已獲取並依照上映日期降序排序。')
    } catch (error) {
      console.error('從後端獲取 TMDBMovie 資料時發生錯誤：', error)
      tmdbMoviesError.value = error
    } finally {
      isTmdbMoviesLoading.value = false
    }
  }
/**
   * 觸發後端從 TMDB API 匯入電影，並將後端的回應傳回給 Vue 元件
   * 這個方法會呼叫您在 Canvas 中選取的 MovieService.triggerTmdbDiscoverImport
   * @param {string} startDate 
   * @param {string} endDate 
   * @param {string} region 
   * @param {number} page 
   * @returns {Promise<any>} 從後端收到的、包含 total_pages 的完整物件
   */
async function fetchAndImportTmdbMovies(startDate, endDate, region, page) {
  // 偵錯日誌 1：確認 Store Action 有被正確呼叫
  console.log('[Store] 1. fetchAndImportTmdbMovies - 開始執行');
  isImporting.value = true;
  importError.value = null;

  try {
    // 偵錯日誌 2：準備呼叫 MovieService
    console.log('[Store] 2. 準備呼叫 MovieService.triggerTmdbDiscoverImport...');
    
    // 呼叫您 Canvas 中的 Service 方法，並用一個變數來「接住」它回傳的結果
    const responseFromService = await MovieService.triggerTmdbDiscoverImport(startDate, endDate, region, page);
    
    // 偵錯日誌 3：【最關鍵】印出從 Service 收到的回傳值
    console.log('[Store] 3. 成功從 MovieService 收到後端回應:', responseFromService);

    // 偵錯日誌 4：檢查這個回傳值是否有效
    if (responseFromService && typeof responseFromService.total_pages !== 'undefined') {
        console.log('[Store] 4. ✅ 檢查通過！從 Service 拿到的回應物件有效。');
    } else {
        console.error('[Store] 4. ❌ 致命錯誤：從 Service 拿到的回應是無效的 (undefined) 或缺少 total_pages！');
    }
    
    // 匯入成功後，可以選擇性地刷新本地的 TMDB 電影列表
    // await fetchAllTmdbMovies(); 
    
    // 偵錯日誌 5：確認 Store 準備回傳什麼東西給 Vue 元件
    console.log('[Store] 5. 準備將回應物件 return 給 Vue 元件...');
    
    // 【最關鍵】將從 Service 接住的物件，再回傳出去給 Vue 元件
    return responseFromService; 

  } catch (error) {
    console.error('[Store] X. 函式在執行過程中發生了無法預期的錯誤:', error);
    importError.value = error;
    // 將錯誤再往上拋，讓 Vue 元件的 catch 也能捕捉到
    throw error;
  } finally {
    isImporting.value = false;
    console.log('[Store] 6. finally 區塊 - 函式執行結束');
  }
}
  // 刪除電影
  async function deleteMovie(movieId) {
    try {
      await MovieService.deleteMovie(movieId)
      console.log(`電影 ID: ${movieId} 刪除成功。`)
      await fetchAllMovies()
      return true
    } catch (error) {
      console.error(`刪除電影 ID: ${movieId} 時發生錯誤：`, error)
      throw error
    }
  }

  // 更新電影 (部分更新)
  async function updateMovie(movieId, updatedData) {
    try {
      await MovieService.updateMovie(movieId, updatedData)
      console.log(`電影 ID: ${movieId} 更新成功。`)
      await fetchAllMovies()
      return true
    } catch (error) {
      console.error(`更新電影 ID: ${movieId} 時發生錯誤：`, error)
      throw error
    }
  }

  // 新增動作：新增電影
  async function createMovie(movieData) {
    try {
      const createdMovieResponse = await MovieService.createMovie(movieData)
      console.log('新增電影成功：', createdMovieResponse ? createdMovieResponse : '無回傳數據')
      await fetchAllMovies()
      return true
    } catch (error) {
      console.error('新增電影時發生錯誤：', error)
      throw error
    }
  }
  // --- Director 相關動作 ---

  /**
   * 獲取所有導演資料
   */
  async function fetchAllDirectors() {
    isDirectorsLoading.value = true
    directorsError.value = null
    try {
      directors.value = await MovieService.getAllDirectors()
    } catch (error) {
      console.error('獲取導演資料時發生錯誤：', error)
      directorsError.value = error
    } finally {
      isDirectorsLoading.value = false
    }
  }

  /**
   * 新增導演
   * @param {object} directorData 導演資料
   */
  async function createDirector(directorData) {
    try {
      const newDirector = await MovieService.createDirector(directorData)
      console.log('新增導演成功：', newDirector)
      await fetchAllDirectors() // 新增後刷新列表
      return newDirector
    } catch (error) {
      console.error('新增導演時發生錯誤：', error)
      throw error
    }
  }

  /**
   * 更新導演
   * @param {number} directorId 導演 ID
   * @param {object} updatedData 更新資料
   */
  async function updateDirector(directorId, updatedData) {
    try {
      const updatedDirector = await MovieService.updateDirector(directorId, updatedData)
      console.log('更新導演成功：', updatedDirector)
      await fetchAllDirectors() // 更新後刷新列表
      return updatedDirector
    } catch (error) {
      console.error('更新導演時發生錯誤：', error)
      throw error
    }
  }

  /**
   * 刪除導演
   * @param {number} directorId 導演 ID
   */
  async function deleteDirector(directorId) {
    try {
      await MovieService.deleteDirector(directorId)
      console.log(`導演 ID: ${directorId} 刪除成功。`)
      await fetchAllDirectors() // 刪除後刷新列表
      return true
    } catch (error) {
      console.error(`刪除導演 ID: ${directorId} 時發生錯誤：`, error)
      throw error
    }
  }
  async function getDirectorDetail(directorId) {
    try {
      return await MovieService.getDirectorDetail(directorId)
    } catch (error) {
      console.error(`獲取導演 ID: ${directorId} 詳細資訊時發生錯誤：`, error)
      throw error
    }
  }

  // --- 演員相關動作 ---
  async function fetchAllActors() {
    isActorsLoading.value = true
    actorsError.value = null
    try {
      actors.value = await MovieService.getAllActors() // 呼叫 MovieService
    } catch (error) {
      console.error('獲取演員列表時發生錯誤：', error)
      actorsError.value = error
    } finally {
      isActorsLoading.value = false
    }
  }

  async function createActor(actorData) {
    try {
      const newActor = await MovieService.createActor(actorData)
      console.log('新增演員成功：', newActor)
      await fetchAllActors() // 刷新列表
      return newActor
    } catch (error) {
      console.error('新增演員時發生錯誤：', error)
      throw error
    }
  }

  async function updateActor(actorId, updatedData) {
    try {
      const updatedActor = await MovieService.updateActor(actorId, updatedData)
      console.log(`演員 ID: ${actorId} 更新成功：`, updatedActor)
      await fetchAllActors() // 刷新列表
      return updatedActor
    } catch (error) {
      console.error(`更新演員 ID: ${actorId} 時發生錯誤：`, error)
      throw error
    }
  }

  async function deleteActor(actorId) {
    try {
      await MovieService.deleteActor(actorId)
      console.log(`演員 ID: ${actorId} 刪除成功。`)
      await fetchAllActors() // 刷新列表
      return true
    } catch (error) {
      console.error(`刪除演員 ID: ${actorId} 時發生錯誤：`, error)
      throw error
    }
  }

  async function getActorDetail(actorId) {
    try {
      return await MovieService.getActorDetail(actorId)
    } catch (error) {
      console.error(`獲取演員 ID: ${actorId} 詳細資訊時發生錯誤：`, error)
      throw error
    }
  }

  // --- 類型相關動作 ---
  async function fetchAllGenres() {
    isGenresLoading.value = true
    genresError.value = null
    try {
      genres.value = await MovieService.getAllGenres() // 呼叫 MovieService
    } catch (error) {
      console.error('獲取類型列表時發生錯誤：', error)
      genresError.value = error
    } finally {
      isGenresLoading.value = false
    }
  }

  async function createGenre(genreData) {
    try {
      const newGenre = await MovieService.createGenre(genreData)
      console.log('新增類型成功：', newGenre)
      await fetchAllGenres() // 刷新列表
      return newGenre
    } catch (error) {
      console.error('新增類型時發生錯誤：', error)
      throw error
    }
  }

  async function updateGenre(genreId, updatedData) {
    try {
      const updatedGenre = await MovieService.updateGenre(genreId, updatedData)
      console.log(`更新類型 ID: ${genreId} 更新成功：`, updatedGenre)
      await fetchAllGenres() // 刷新列表
      return updatedGenre
    } catch (error) {
      console.error(`更新類型 ID: ${genreId} 時發生錯誤：`, error)
      throw error
    }
  }

  async function deleteGenre(genreId) {
    try {
      await MovieService.deleteGenre(genreId)
      console.log(`類型 ID: ${genreId} 刪除成功。`)
      await fetchAllGenres() // 刷新列表
      return true
    } catch (error) {
      console.error(`刪除類型 ID: ${genreId} 時發生錯誤：`, error)
      throw error
    }
  }

  async function getGenreDetail(genreId) {
    try {
      return await MovieService.getGenreDetail(genreId)
    } catch (error) {
      console.error(`獲取類型 ID: ${genreId} 詳細資訊時發生錯誤：`, error)
      throw error
    }
  }
  async function getMovieGenres(movieId) {
    try {
      const response = await MovieService.getMovieGenreIds(movieId) // 修正點：確保 MovieService 回傳的是陣列
      return Array.isArray(response) ? response : [] // 防禦性檢查
    } catch (error) {
      console.error(`獲取電影 ID: ${movieId} 類型 ID 列表時發生錯誤：`, error)
      throw error
    }
  }

  async function updateMovieGenres(movieId, genreIds) {
    // 修正點：新增更新電影類型關聯的動作
    try {
      await MovieService.updateMovieGenres(movieId, genreIds)
      console.log(`電影 ID: ${movieId} 類型更新成功。`)
      await fetchAllMovies() // 更新類型後刷新電影列表
      return true
    } catch (error) {
      console.error(`更新電影 ID: ${movieId} 類型時發生錯誤：`, error)
      throw error
    }
  }
  // 新增動作：獲取電影卡司
  async function getMovieCast(movieId) {
    try {
      return await MovieService.getMovieCast(movieId)
    } catch (error) {
      console.error(`獲取電影 ID: ${movieId} 卡司時發生錯誤：`, error)
      throw error
    }
  }

  // 新增動作：更新電影演員角色
  async function updateMovieActorRole(movieId, actorId, characterName) {
    try {
      await MovieService.updateMovieActorRole(movieId, actorId, characterName)
      console.log(`電影 ID: ${movieId} 演員 ID: ${actorId} 角色更新成功。`)
      return true
    } catch (error) {
      console.error(`更新電影 ID: ${movieId} 演員 ID: ${actorId} 角色時發生錯誤：`, error)
      throw error
    }
  }
  // 【新增2】新增一個演員到電影卡司中
  async function addActorToMovie(movieId, actorId, characterName) {
    try {
      // 呼叫 MovieService 中對應的方法
      await MovieService.addActorToMovie(movieId, actorId, characterName)
      console.log(`已成功將演員 ID: ${actorId} 加入電影 ID: ${movieId} 的卡司中`)
      return true
    } catch (error) {
      console.error(`新增演員到電影 ID: ${movieId} 時發生錯誤：`, error)
      throw error
    }
  }

  // 【新增3】從電影卡司中移除一個演員
  async function removeActorFromMovie(movieId, actorId) {
    try {
      // 呼叫 MovieService 中對應的方法
      await MovieService.removeActorFromMovie(movieId, actorId)
      console.log(`已成功從電影 ID: ${movieId} 的卡司中移除演員 ID: ${actorId}`)
      return true
    } catch (error) {
      console.error(`從電影 ID: ${movieId} 移除演員時發生錯誤：`, error)
      throw error
    }
  }
  /**
   * 獲取所有電影，並篩選出「現正熱映」的電影
   * 條件：status 為 true，且上映日期在今天往前推兩個月內
   */
  async function fetchNowShowingMovies() {
    isLoading.value = true
    error.value = null
    try {
      // --- 這是篩選邏輯的核心 ---

      // a. 取得今天的日期和兩個月前的日期
      const today = new Date()
      const twoMonthsAgo = new Date()
      twoMonthsAgo.setMonth(today.getMonth() - 6)

      // 【偵錯日誌 1】印出我們用來比較的日期範圍
      console.log(
        `[偵錯] 日期篩選範圍: 從 ${twoMonthsAgo.toISOString().split('T')[0]} 到 ${today.toISOString().split('T')[0]}`,
      )

      // b. 呼叫 Service 取得所有電影
      const allMoviesDTOs = await MovieService.getAllMovies()

      // 【偵錯日誌 2】印出從後端拿到的最原始的電影列表，確認資料有進來
      console.log('[偵錯] 從後端收到的原始電影列表 (DTOs):', allMoviesDTOs)

      // c. 【已修正】使用 .filter() 方法篩選出符合條件的電影
      const filteredMovies = allMoviesDTOs
        .filter((dto) => {
          // 【修正1】從 DTO 包裹中，先把真正的 movie 物件拿出來
          const movie = dto.movie

          // 如果因為某些原因 movie 物件不存在，就直接過濾掉，避免錯誤
          if (!movie) {
            return false
          }

          // 【偵錯日誌 3】對於每一部電影，都印出它的詳細資料和判斷過程
          console.log(`\n--- 正在檢查電影: "${movie.titleLocal}" (ID: ${movie.id}) ---`)

          const releaseDate = new Date(movie.releaseDate)

          // 【修正2】所有條件判斷都改用 movie.xxx 來存取
          // 條件1: 狀態必須是 true
          const isStatusOk = movie.status === true
          console.log(
            `  - 狀態 (status): ${movie.status} (型別: ${typeof movie.status}) --> 是否符合 (=== true): ${isStatusOk}`,
          )

          // 條件2: 上映日期必須晚於(或等於)兩個月前
          const isAfterStartDate = releaseDate >= twoMonthsAgo
          console.log(
            `  - 上映日期: ${movie.releaseDate} --> 是否 >= ${twoMonthsAgo.toISOString().split('T')[0]}: ${isAfterStartDate}`,
          )

          // 條件3: 上映日期必須早於(或等於)今天
          const isBeforeEndDate = releaseDate <= today
          console.log(
            `  - 上映日期: ${movie.releaseDate} --> 是否 <= ${today.toISOString().split('T')[0]}: ${isBeforeEndDate}`,
          )

          const shouldBeIncluded = isStatusOk && isAfterStartDate && isBeforeEndDate
          console.log(`  ==> 最終結果: ${shouldBeIncluded ? '✅ 顯示' : '❌ 過濾'}`)

          return shouldBeIncluded
        })
        // 【修正3】在篩選完後，我們需要的是 movie 物件的列表，而不是 DTO 的列表
        .map((dto) => dto.movie)

      // 【偵錯日誌 4】印出經過篩選後，剩下多少部電影
      console.log(`\n[偵錯] 篩選完成，共有 ${filteredMovies.length} 部電影符合條件。`)

      // d. 將篩選後的電影依照上映日期排序 (由新到舊)
      filteredMovies.sort((a, b) => new Date(b.releaseDate) - new Date(a.releaseDate))

      // e. 將最終結果存入狀態
      nowShowingMovies.value = filteredMovies
    } catch (e) {
      console.error('載入現正熱映電影時發生錯誤:', e)
      error.value = '無法載入電影資料，請稍後再試。'
    } finally {
      isLoading.value = false
    }
  }

  // 取得特定日期現正熱映電影列表
  const getNowPlayingMoviesByDate = async (date) => {
    isNowPlayingMoviesLoading.value = true
    nowPlayingMoviesError.value = null
    try {
      // 取得特定日期上映中電影
      const data = await MovieService.getNowPlayingMoviesByDate(date)
      // 將獲取的數據賦值給 nowPlayingMoviesFromAPI 狀態
      nowPlayingMoviesFromAPI.value = data
      // console.log(nowPlayingMoviesFromAPI.value);
    } catch (err) {
      // 現正熱映電影資料獲取失敗
      nowPlayingMoviesError.value = `[MovieStore] 獲取 ${date} 現正熱映電影失敗`
      console.error(nowPlayingMoviesError.value, err)
    } finally {
      // 無論成功或失敗，都將載入狀態設為 false
      isNowPlayingMoviesLoading.value = false
    }
  }

  async function getMoviePosterBlobUrl(movieId) {
    try {
      // 呼叫 Service 層獲取原始 Blob 數據
      const blobData = await MovieService.getMoviePoster(movieId);
      return URL.createObjectURL(blobData);
    } catch (error) {
      // 即使攔截器已提示，這裡仍然返回一個預設圖，確保UI穩定
      console.error(`getMoviePosterBlobUrl action for ID ${movieId} failed:`, error);
      return 'https://placehold.co/70x100/cccccc/333333?text=無海報';
    }
  }

  // 返回所有狀態和動作
  return {
    // --- State (狀態) ---
    movies,
    isMoviesLoading,
    moviesError,
    tmdbMovies,
    isTmdbMoviesLoading,
    tmdbMoviesError,
    isImporting,
    importError,
    directors,
    isDirectorsLoading,
    directorsError,
    actors,
    isActorsLoading,
    actorsError,
    genres,
    isGenresLoading,
    genresError,
    nowPlayingMoviesFromAPI,
    isNowPlayingMoviesLoading,
    nowPlayingMoviesError,
    nowShowingMovies,
    isLoading, // 來自第二個物件
    error, // 來自第二個物件

    // --- Actions (方法/動作) ---
    fetchAllMovies,
    fetchAllTmdbMovies,
    importAllMoviesToLocal,
    importSelectedTmdbMovies,
    fetchAndImportTmdbMovies,
    deleteMovie,
    updateMovie,
    createMovie,
    fetchAllDirectors,
    createDirector,
    updateDirector,
    deleteDirector,
    getDirectorDetail,
    fetchAllActors,
    createActor,
    updateActor,
    deleteActor,
    getActorDetail,
    getMovieCast,
    updateMovieActorRole,
    addActorToMovie, // 來自第二個物件
    removeActorFromMovie, // 來自第二個物件
    fetchAllGenres,
    createGenre,
    updateGenre,
    deleteGenre,
    getGenreDetail,
    getMovieGenres,
    updateMovieGenres,
    getNowPlayingMoviesByDate,
    fetchNowShowingMovies, // 來自第二個物件
    getMoviePosterBlobUrl
  }
})
