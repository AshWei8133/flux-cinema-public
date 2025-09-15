import { defineStore } from "pinia";
import { ref } from "vue";
import MemberService from "../services/memberService";


export const useMemberStore = defineStore('member', () => {

    // 狀態
    const members = ref([]); // 存會員列表

    const isMembersLoading = ref(false); // 會員列表載入狀態

    const membersError = ref(null); // 會員列表錯誤訊息

    const selectedMember = ref(null); // 存單一會員資料（可用於編輯/詳細頁）

    const isSelectedMemberLoading = ref(false); // 單筆會員載入狀態

    const selectedMemberError = ref(null); // 單筆會員錯誤訊息

    const allMembers = ref([])


    //方法
    // 取得全部會員
    const fetchAllMembers = async () => {
        isMembersLoading.value = true;
        membersError.value = null; // 先清空錯誤訊息
        try {
            const data = await MemberService.getAllMembers();
            allMembers.value = data
            members.value = data;
        } catch (err) {
            membersError.value = err.message || '獲取會員列表失敗';
        } finally {
            isMembersLoading.value = false;
        }
    };

    // 取得單一會員
    const fetchMemberById = async (id) => {
        isSelectedMemberLoading.value = true;
        selectedMemberError.value = null;
        try {
            const data = await MemberService.getMemberById(id);
            selectedMember.value = data;
        } catch (err) {
            selectedMemberError.value = err.message || `獲取會員 ID=${id} 失敗`;
        } finally {
            isSelectedMemberLoading.value = false;
        }
    };

    // 新增會員（可上傳照片）
    const createMember = async (memberData, file) => {
        try {
            const response = await MemberService.createMember(memberData, file);
            // 新增後自動刷新列表
            await fetchAllMembers();
            return response;
        } catch (error) {
            console.error('新增會員失敗:', error);
            const backendResponse = error.response?.data;
            if (backendResponse && backendResponse.success !== undefined) {
                return {
                    success: backendResponse.success,
                    message: backendResponse.message || '新增會員失敗，但沒有錯誤訊息'
                };
            } else {
                return {
                    success: false,
                    message: error.message || '新增會員時發生未知錯誤'
                };
            }
        }
    };

    // 更新會員（可上傳照片）
    const updateMember = async (id, memberData, file) => {
        try {
            const response = await MemberService.updateMember(id, memberData, file);
            // 更新後自動刷新列表
            await fetchAllMembers();
            return response;
        } catch (error) {
            console.error(`更新會員 ID=${id} 失敗:`, error);
            const backendResponse = error.response?.data;
            if (backendResponse && backendResponse.success !== undefined) {
                return {
                    success: backendResponse.success,
                    message: backendResponse.message || '更新會員失敗，但沒有錯誤訊息'
                };
            } else {
                return {
                    success: false,
                    message: error.message || '更新會員時發生未知錯誤'
                };
            }
        }
    };

    // 刪除會員
    const deleteMember = async (id) => {
        try {
            const response = await MemberService.deleteMember(id);
            // 刪除後自動刷新列表
            await fetchAllMembers();
            return response;
        } catch (error) {
            console.error(`刪除會員 ID=${id} 失敗:`, error);
            const backendResponse = error.response?.data;
            if (backendResponse && backendResponse.success !== undefined) {
                return {
                    success: backendResponse.success,
                    message: backendResponse.message || '刪除會員失敗，但沒有錯誤訊息'
                };
            } else {
                return {
                    success: false,
                    message: error.message || '刪除會員時發生未知錯誤'
                };
            }
        }
    };

    // 前端搜尋會員（client-side filter）
    const filterMembers = (filters) => {
        // 先取得全部會員再過濾
        let filtered = [...allMembers.value];

        if (filters.username) {
            filtered = filtered.filter(m =>
                m.username.toLowerCase().includes(filters.username.toLowerCase())
            );
        }

        if (filters.email) {
            filtered = filtered.filter(m =>
                m.email.toLowerCase().includes(filters.email.toLowerCase())
            );
        }

        if (filters.gender) {
            filtered = filtered.filter(m => m.gender === filters.gender);
        }

        members.value = filtered;
    };


    return {
        // 狀態
        members,
        isMembersLoading,
        membersError,
        selectedMember,
        isSelectedMemberLoading,
        selectedMemberError,
        // 方法
        fetchAllMembers,
        fetchMemberById,
        createMember,
        updateMember,
        deleteMember,
        filterMembers
    };
});