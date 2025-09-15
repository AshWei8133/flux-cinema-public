import httpClient from "@/services/api";

const publicMemberProfileService = {
    /**
     * 獲取當前登入會員的詳細個人資料
     * @returns 
     */
    getProfile() {
        // 從 localStorage 取得 token 和 memberId
        const memberData = JSON.parse(localStorage.getItem('member_jwt_token'));
        if (!memberData || !memberData.memberInfo) {
            return Promise.reject(new Error('找不到會員資訊，請先登入'));
        }

        const memberId = memberData.memberInfo.memberId;

        // 呼叫後端 API，params 自動帶 memberId，headers 自動帶 Token
        return httpClient.get('/membercenter/profile/by-id', {
            params: { memberId }
        });
    },

    /**
     * 更新會員頭像
     * @param {number} memberId - 會員ID
     * @param {FormData} formData - 包含圖片檔案的表單數據
     * @returns
     */
    updateAvatar(memberId, formData) {
        return httpClient.put(`/membercenter/profile/photo`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    },

    /**
     * 更新會員基本資料
     * @param {*} profileData 
     * @returns 
     */
    updateProfile(profileData) {
        const memberData = JSON.parse(localStorage.getItem('member_jwt_token'));
        if (!memberData || !memberData.memberInfo) {
            return Promise.reject(new Error('找不到會員資訊，請先登入'));
        }
        const memberId = memberData.memberInfo.memberId;

        const dataToSend = {
            ...profileData,
            memberId
        };
        return httpClient.put('/membercenter/profile', dataToSend);
    },

    /**
     * 變更會員密碼  
     * @param {*} passwordData 
     * @returns 
     */
    changePassword(passwordData) {
        return httpClient.put('/membercenter/password', passwordData);
    },

    // ------------ 訂票紀錄 -------------

    /**
     * 獲取會員的訂票紀錄
     * @returns 
     */
    async getTicketOrderHistory() {
        try {
            const response = await httpClient.get('/membercenter/ticket-orders');
            return response;
        } catch (error) {
            console.error('獲取訂票紀錄失敗:', error.message);
            throw error;
        }
    },

    // ------------ 【新增】電影收藏相關方法 -------------

    /**
     * 新增一部電影到會員的收藏列表
     * @param {number} movieId 要收藏的電影 ID
     * @returns {Promise<any>} 後端回傳的成功訊息
     */
    async addFavorite(movieId) {
        try {
            // 向後端的 POST /api/member/favorites/{movieId} 發送請求
            const response = await httpClient.post(`/membercenter/favorites/${movieId}`);
            return response; // 攔截器會自動處理 .data
        } catch (error) {
            console.error(`新增電影 ID: ${movieId} 到收藏時發生錯誤:`, error);
            throw error;
        }
    },

    /**
     * 從會員的收藏列表中移除一部電影
     * @param {number} movieId 要移除的電影 ID
     * @returns {Promise<any>} 後端回傳的成功訊息
     */
    async removeFavorite(movieId) {
        try {
            // 向後端的 DELETE /api/member/favorites/{movieId} 發送請求
            const response = await httpClient.delete(`/membercenter/favorites/${movieId}`);
            return response;
        } catch (error) {
            console.error(`從收藏中移除電影 ID: ${movieId} 時發生錯誤:`, error);
            throw error;
        }
    },

    /**
     * 獲取當前登入會員收藏的所有電影 ID 列表
     * @returns {Promise<Set<number>>} 一個包含電影 ID 的 Set 集合
     */
    async getFavoriteMovieIds() {
        try {
            // 向後端的 GET /api/member/favorites/ids 發送請求
            const response = await httpClient.get('/membercenter/favorites/ids');
            return response;
        } catch (error) {
            console.error('獲取收藏電影 ID 列表時發生錯誤:', error);
            throw error;
        }
    },

    /**
     * 獲取當前登入會員收藏的所有電影的完整資訊列表
     * @returns {Promise<Array<object>>} 一個包含電影 DTO 的陣列
     */
    async getFavoriteMovies() {
        try {
            // 向後端的 GET /api/member/favorites 發送請求
            const response = await httpClient.get('/membercenter/favorites');
            return response;
        } catch (error) {
            console.error('獲取完整收藏列表時發生錯誤:', error);
            throw error;
        }
    },

    // ------------ 商品購買紀錄 -------------

    /**
     * 【新增】獲取當前登入會員的商品訂單歷史紀錄 
     * @returns \
     */
    getProductOrderHistory() {
        return httpClient.get('/membercenter/orders/products')
    }
};

export default publicMemberProfileService;
