import httpClient from "@/services/api";


/**
 * MemberLevelService
 * 處理會員等級 (Member Level) 相關的 API 請求
 * - 包含：查詢所有會員等級、查詢單一會員等級、新增、更新、刪除
 * - 使用與 MemberService 相同的結構，方便統一維護
 */
const MemberLevelService = {

    /**
     * 獲取所有會員等級
     * @returns 
     */
    getMemberLevels() {
        return httpClient.get('/admin/member-levels');
    },

    /**
     *  根據 ID 獲取單一會員等級
     * @param {*} id 
     * @returns 
     */
    getMemberLevelById(id) {
        return httpClient.get(`/admin/member-levels/${id}`);
    },

    /**
     * 新增一個會員等級
     * @param {*} levelData 
     * @param {*} levelIconFile 
     * @returns 
     */
    createMemberLevel(levelData, levelIconFile) {
        const formData = new FormData();
        formData.append('memberLevelDTO', new Blob([JSON.stringify(levelData)], { type: 'application/json' }));

        if (levelIconFile) {
            formData.append('levelIcon', levelIconFile);
        }

        return httpClient.post('/admin/member-levels', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    /**
     * 更新一個現有的會員等級
     * @param {*} id 
     * @param {*} levelData 
     * @param {*} levelIconFile 
     * @returns 
     */
    updateMemberLevel(id, levelData, levelIconFile) {
        const formData = new FormData();
        formData.append('memberLevelDTO', new Blob([JSON.stringify(levelData)], { type: 'application/json' }));

        if (levelIconFile) {
            formData.append('levelIcon', levelIconFile);
        }

        return httpClient.put(`/admin/member-levels/${id}`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    /**
     * 刪除一個會員等級
     * @param {*} id 
     * @returns 
     */
    deleteMemberLevel(id) {
        return httpClient.delete(`/admin/member-levels/${id}`);
    },

    /**
     * 【新增】獲取當前會員的等級狀態 (進度、下一等級等)
     * @returns {Promise<MemberLevelStatusDTO>} 
     */
    fetchMemberLevelStatus() {
        return httpClient.get('/member/level-status');
    },

    /**
     * 【新增】通知後端更新當前會員的等級
     * 這會觸發後端根據最新的消費總額重新計算會員等級
     * @returns {Promise<void>}
     */
    updateMemberLevelStatus() {
        return httpClient.post('/member/level-up');
    },
};

// 導出 MemberLevelService 物件，方便在組件中引入
export default MemberLevelService;
