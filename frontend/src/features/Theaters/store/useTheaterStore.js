import { defineStore } from 'pinia';
import { ref } from 'vue';
import TheaterService from '../services/theaterService';

export const useTheaterStore = defineStore('theater', () => {

  // 數據
  // 影廳列表
  const theaters = ref([]);
  // 影廳類型列表
  const theaterTypes = ref([]);
  // 影廳列表載入狀態
  const isTheatersLoading = ref(false);
  // 影廳列表錯誤訊息
  const theatersError = ref(null);

  // 影廳類型載入狀態
  const isTheaterTypesLoading = ref(false);
  // 影廳類型錯誤訊息
  const theaterTypesError = ref(null);


  // 方法
  // 取得全部影廳資料
  const fetchAllTheaters = async () => {
    isTheatersLoading.value = true;
    theatersError.value = null;
    try {
      // 調用 TheaterService 獲取影廳數據
      const data = await TheaterService.getAllTheaters();
      // 將獲取的數據賦值給 theaters 狀態
      theaters.value = data;
    } catch (err) {
      // 捕獲錯誤，並將錯誤訊息賦值給 theatersError 狀態
      theatersError.value = err.message || '獲取影廳列表失敗';
    } finally {
      // 無論成功或失敗，都將載入狀態設為 false
      isTheatersLoading.value = false;
    }
  };

  // 取得全部影廳類型資料
  const fetchAllTheaterTypes = async () => {
    isTheaterTypesLoading.value = true;
    theaterTypesError.value = null; // 在開始前清空錯誤
    try {
      const data = await TheaterService.getAllTheaterTypes();
      theaterTypes.value = data;
    } catch (err) {
      theaterTypesError.value = err.message || '獲取影廳類別失敗';
      console.log(theaterTypesError.value);
    } finally {
      isTheaterTypesLoading.value = false;
    }
  };

  // 取得特定影廳細節資料
  // 這裡不負責共享數據只負責提供方法，以「細節狀態由元件處理，全局狀態由 Store 處理」為原則
  const fetchTheaterDetailById = async (theaterId) => {
    try {
      const data = await TheaterService.getTheaterDetailById(theaterId);
      return data; // 直接回傳資料給呼叫的組件
    } catch (error) {
      // 拋出錯誤給元件處理
      console.log('API Error：', error);
      throw error;
    }
  }


  // 新增影廳功能
  const createTheater = async (newTheaterData) => {
    try {
      // 調用 service，發送 POST 請求
      // 當後端返回 201 狀態碼時，axios 會將回應數據直接返回
      const response = await TheaterService.createTheater(newTheaterData);

      // 這裡我們直接返回從後端獲取到的 response 物件
      // 它的結構是 { success: true, message: '新增影廳成功' }
      return response;

    } catch (error) {
      // 捕獲來自 service 的錯誤
      // 這將涵蓋後端返回 400 狀態碼 (BadRequest) 的情況
      console.error('新增影廳時發生錯誤:', error);

      // 從錯誤物件中提取後端傳回的錯誤訊息
      // error.response?.data 是後端返回的 { success: false, message: '新增影廳失敗' }
      const backendResponse = error.response?.data;

      if (backendResponse && backendResponse.success !== undefined) {
        // 如果後端返回的物件結構是我們預期的
        return {
          success: backendResponse.success,
          message: backendResponse.message || '新增影廳失敗，但沒有錯誤訊息。'
        };
      } else {
        // 如果不是預期的後端錯誤物件（例如網路錯誤、伺服器內部錯誤500等）
        const errorMessage = error.message || '新增影廳時發生未知錯誤。';
        return {
          success: false,
          message: errorMessage
        };
      }
    }
  };

  // 更新影廳功能
  const updateTheater = async (newTheaterData, theaterId) => {
    try {
      // 調用 service，發送 Put 請求
      // 當後端返回 200 狀態碼時，axios 會將回應數據直接返回
      const response = await TheaterService.updateTheater(newTheaterData, theaterId);

      // 返回更新成功或失敗的訊息
      return response;

    } catch (error) {
      // 捕獲來自 service 的錯誤
      // 這將涵蓋後端返回 400 狀態碼 (BadRequest) 的情況
      console.error('更新影廳時發生錯誤:', error);

      // 從錯誤物件中提取後端傳回的錯誤訊息
      // error.response?.data 是後端返回的 { success: false, message: '更新影廳失敗' }
      const backendResponse = error.response?.data;

      if (backendResponse && backendResponse.success !== undefined) {
        // 如果後端返回的物件結構是我們預期的
        return {
          success: backendResponse.success,
          message: backendResponse.message || '更新影廳失敗，但沒有錯誤訊息。'
        };
      } else {
        // 如果不是預期的後端錯誤物件（例如網路錯誤、伺服器內部錯誤500等）
        const errorMessage = error.message || '更新影廳時發生未知錯誤。';
        return {
          success: false,
          message: errorMessage
        };
      }
    }
  };

  const deleteTheater = async (theaterId) => {
    try {
      // 調用service，執行刪除操作
      const response = await TheaterService.deleteTheater(theaterId);

      // 檢查後端回傳的 success 狀態
      if (response.success) {
        // 在 Store 中直接移除該影廳，實現畫面即時更新
        theaters.value = theaters.value.filter(
          (theater) => theater.theaterId !== theaterId
        );
        return response;
      } else {
        return response; // 失敗回傳 { success: false, message: '...' }
      }
    } catch (error) {
      // 捕獲來自 service 的錯誤
      // 這將涵蓋後端返回 400 狀態碼 (BadRequest) 的情況
      console.error('刪除影廳時發生錯誤:', error);

      // 從錯誤物件中提取後端傳回的錯誤訊息
      // error.response?.data 是後端返回的 { success: false, message: '刪除影廳失敗' }
      const backendResponse = error.response?.data;

      if (backendResponse && backendResponse.success !== undefined) {
        // 如果後端返回的物件結構是我們預期的
        return {
          success: backendResponse.success,
          message: backendResponse.message || '刪除影廳失敗，但沒有錯誤訊息。'
        };
      } else {
        // 如果不是預期的後端錯誤物件（例如網路錯誤、伺服器內部錯誤500等）
        const errorMessage = error.message || '刪除影廳時發生未知錯誤。';
        return {
          success: false,
          message: errorMessage
        };
      }
    }
  }

  const commitTheaterTypeChanges = async (changesToSubmit) => {
    try {
      // 調用service，執行變更操作
      const response = await TheaterService.commitTheaterTypeChanges(changesToSubmit);
      return response;
    } catch (error) {
      throw error;
    }
  }



  // return 給應用程式使用
  return {
    theaters,
    theaterTypes,
    theatersError,
    isTheaterTypesLoading,
    theaterTypesError,
    isTheatersLoading,
    fetchAllTheaters,
    fetchAllTheaterTypes,
    fetchTheaterDetailById,
    createTheater,
    updateTheater,
    deleteTheater,
    commitTheaterTypeChanges,
  };
});