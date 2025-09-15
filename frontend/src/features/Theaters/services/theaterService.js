// src/services/TheaterService.js (將檔案副檔名從 .ts 改為 .js)

// 引入已配置的 axios 實例，用於發送 HTTP 請求
import httpClient from '@/services/api';

// 由於 JavaScript 沒有內建的型別，這裡我們移除 TypeScript 的型別定義引入。
// 如果您需要對數據結構有清晰的了解，可以在 JSDoc 註解中說明預期的數據格式。
// import { ShowTheaters } from '@/types/theater';

const TheaterService = {
  // 取得所有影廳資料
  async getAllTheaters() {
    try {
      // 發送 GET 請求到 '/admin/theater' 端點
      // TypeScript 中的 Promise<ShowTheaters[]> 型別斷言在這裡被移除
      const response = await httpClient.get('/admin/theater');
      return response; // httpClient 的響應攔截器已經返回了 response.data
    } catch (error) {
      // 捕獲並處理獲取影廳失敗的錯誤
      // error: any 型別註釋被移除
      console.error('獲取所有影廳失敗:', error.message);
      throw error; // 重新拋出錯誤，以便調用處可以捕獲並處理
    }
  },
  // 取得所有影廳類別資料
  async getAllTheaterTypes() {
    try {
      const response = await httpClient.get('/admin/theater/theaterTypes');
      return response;
    } catch (error) {
      console.error('獲取影廳類別失敗:', error.message);
      throw error;
    }
  },

  // 新增影廳
  async createTheater(newTheaterData) {
    try {
      // 發送 POST 請求到 '/admin/theater' 端點，並帶上 newTheaterData
      const response = await httpClient.post('/admin/theater', newTheaterData);
      return response; // 成功時，返回後端響應
    } catch (error) {
      // 捕獲並處理新增影廳失敗的錯誤
      console.error('新增影廳失敗:', error.message);
      // 拋出錯誤，讓 pinia store 可以捕獲並處理
      throw error;
    }
  },

  // 查詢單一影廳細節資料
  async getTheaterDetailById(theaterId) {
    try {
      // 發送 GET 請求，帶上影廳 id 在 url 中取得影廳詳細資料
      const response = await httpClient.get(`/admin/theater/${theaterId}`);
      return response;
    } catch (error) {
      console.log('取得影廳細節資料失敗', error.message);
      throw error;
    }
  },

  // 更新影廳
  async updateTheater(newTheaterData, theaterId) {
    try {
      // 發送 PUT 請求，帶上要更新的影廳內容及在 url 中加入影廳 id
      const response = await httpClient.put(`/admin/theater/${theaterId}`, newTheaterData);
      return response;
    } catch (error) {
      console.log('更新影廳失敗', error.message);
      throw error;
    }
  },

  // 刪除影廳資料
  async deleteTheater(theaterId) {
    try {
      // 發送 delete 請求，帶上影廳 id 在 url 中
      const response = await httpClient.delete(`/admin/theater/${theaterId}`);
      return response;
    } catch (error) {
      console.log('刪除影廳失敗', error.message);
      throw error;
    }
  },

  // 處理影廳類別的變更提交確認
  async commitTheaterTypeChanges(changesToSubmit) {
    try {
      // 發送 post 請求，參數為包含deleted、added、updated三個陣列給後端增、刪、改
      const response = await httpClient.post('/admin/theater/theaterTypes/commitChanges', changesToSubmit);
      return response;
    } catch (error) {
      console.log('提交影廳類別變更操作失敗');
      throw error;
    }
  }

};


// 導出 TheaterService 對象
export default TheaterService;
