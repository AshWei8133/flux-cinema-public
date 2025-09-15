import httpClient from "@/services/api";

/**
 * MemberService
 * 專門處理會員相關 API 請求
 */

const MemberService = {
    // 取得所有會員
    async getAllMembers() {
        try {
            const response = await httpClient.get('/admin/members');
            return response; // 直接返回 response.data（由 httpClient 攔截器處理）
        } catch (error) {
            console.error('獲取會員列表失敗:', error.message);
            throw error; // 讓外層可以捕獲
        }
    },

    // 取得單一會員
    async getMemberById(id) {
        try {
            const response = await httpClient.get(`/admin/members/${id}`);
            return response;
        } catch (error) {
            console.error(`獲取會員 ID=${id} 失敗:`, error.message);
            throw error;
        }
    },

    // 新增會員（可上傳檔案）
    async createMember(memberData, file) {
        try {
            const formData = new FormData();
            formData.append(
                'member',
                new Blob([JSON.stringify(memberData)], { type: 'application/json' })
            );
            if (file) formData.append('file', file);

            const response = await httpClient.post('/admin/members', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            return response;
        } catch (error) {
            console.error('新增會員失敗:', error.message);
            throw error;
        }
    },

    // 更新會員（可上傳檔案）
    async updateMember(id, memberData, file) {
        try {
            const formData = new FormData();
            formData.append(
                'member',
                new Blob([JSON.stringify(memberData)], { type: 'application/json' })
            );
            if (file) formData.append('file', file);

            const response = await httpClient.put(`/admin/members/${id}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            return response;
        } catch (error) {
            console.error(`更新會員 ID=${id} 失敗:`, error.message);
            throw error;
        }
    },

    // 刪除會員
    async deleteMember(id) {
        try {
            const response = await httpClient.delete(`/admin/members/${id}`);
            return response;
        } catch (error) {
            console.error(`刪除會員 ID=${id} 失敗:`, error.message);
            throw error;
        }
    }
};

// 導出 MemberService 對象
export default MemberService;