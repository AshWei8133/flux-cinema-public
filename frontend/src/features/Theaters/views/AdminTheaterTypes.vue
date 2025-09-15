<template>
  <div class="table-container">
    <div v-if="isTheaterTypesLoading" class="loading-state">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>資料載入中...</span>
    </div>

    <div v-else-if="theaterTypesError" class="error-state">
      <el-alert title="錯誤" :description="theaterTypesError" type="error" show-icon />
    </div>

    <AdminTable
      v-else
      :data="displayData"
      :columns="tableColumns"
      :editing-cell="editingCell"
      @edit-field="handleEditField"
      @delete="handleDelete"
      @save-edit="handleSaveEdit"
      @cancel-edit="handleCancelEdit"
      @cancel-delete="handleCancelDelete"
    />

    <div class="add-new-row-container">
      <AdminButton type="primary" size="small" plain circle @click="handleAdd">
        <Icon icon="mdi:plus" />
      </AdminButton>
    </div>

    <el-row v-if="hasChanges" justify="end" style="margin-top: 20px; margin-bottom: 10px">
      <AdminButton
        type="info"
        label="重置變更"
        @click="handleResetChanges"
        style="margin-right: 10px"
      />
      <AdminButton type="primary" label="儲存變更" @click="handleConfirmChanges" />
    </el-row>

    <div class="operation-notes">
      <div class="notes-content">
        <ul>
          <li>
            <el-text>《操作說明》</el-text>
          </li>
          <li>
            <el-tag type="success" size="small">新增</el-tag>
            <el-text>
              ：點擊右下角
              <el-icon><Plus /></el-icon>
              按鈕新增一筆空白資料，編輯完成後點擊儲存。
            </el-text>
          </li>
          <li>
            <el-tag type="warning" size="small">編輯</el-tag>
            <el-text
              >：點擊表格列上的
              <el-icon><EditPen /></el-icon> 按鈕，即可修改欄位內容，完成後點擊儲存。</el-text
            >
          </li>
          <li>
            <el-tag type="danger" size="small">刪除</el-tag>
            <el-text
              >：點擊表格列上的 <el-icon><Delete /></el-icon> 按鈕，資料會被標記為刪除。</el-text
            >
          </li>
          <li>
            <el-tag type="primary" size="small">儲存變更</el-tag>
            <el-text
              >：當有任何新增、編輯或刪除操作時，下方會出現「儲存變更」按鈕，點擊後才會保留變更操作。</el-text
            >
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElIcon, ElAlert, ElTag, ElText, ElRow } from 'element-plus'
import { Loading, Plus, EditPen, Delete } from '@element-plus/icons-vue'
import AdminButton from '@/components/admin/AdminButton.vue'
import AdminTable from '@/components/admin/AdminTable.vue'
import { Icon } from '@iconify/vue'
import { useTheaterStore } from '../store/useTheaterStore'
import { storeToRefs } from 'pinia'

// 匯入store
const theaterStore = useTheaterStore()
const { theaterTypes, isTheaterTypesLoading, theaterTypesError } = storeToRefs(theaterStore)

// 定義表格欄位，並設定哪些欄位可編輯
const tableColumns = [
  {
    key: 'theaterTypeName',
    title: '影廳類型名稱',
    isEditable: true,
    minWidth: '100',
    isRequired: true,
  },
  { key: 'description', title: '描述', isEditable: true, minWidth: '200', isRequired: false },
]

// 儲存有進行增刪改操作的
const changes = ref({
  added: [],
  updated: [],
  deleted: [],
})

// 追蹤目前正在編輯的儲存格狀態
const editingCell = ref({
  rowId: null, // 正在編輯的資料 ID
  field: null, // 正在編輯的欄位 key
  originalValue: null, // 編輯前的原始值
})

// 輔助函式，用來獲取正確的ID，可能是 id 或 theaterTypeId
const getRowId = (row) => row.id || row.theaterTypeId

// 整合原始資料和變更狀態，產生最終的顯示資料
const displayData = computed(() => {
  // 將資料展開
  let allData = [...theaterTypes.value]

  // 統一處理原始資料，將 null 或 undefined 轉換為空字串，確保顯示和編輯的一致性
  allData = allData.map((item) => {
    const processedItem = {}
    for (const key in item) {
      // 判斷值是否為 null 或 undefined，是的話則設為空字串
      processedItem[key] = item[key] === null || item[key] === undefined ? '' : item[key]
    }
    return processedItem
  })

  // 處理「刪除」時表格內容
  allData = allData.map((item) => {
    // 檢查影廳資料有沒有在「刪除」清單中
    const isDeleted = changes.value.deleted.some((d) => getRowId(d) === getRowId(item))
    if (isDeleted) {
      return {
        ...item,
        isDeleted: true,
      }
    }

    // 處理「更新」
    // 尋找此項目是否在待更新清單中
    const updatedItem = changes.value.updated.find((u) => getRowId(u) === getRowId(item))
    if (updatedItem) {
      // 如果有找到，則用更新後的資料替換掉原始資料，並加上 isUpdated 標籤
      // 統一處理空值為空字串
      const changesToApply = {}
      for (const key in updatedItem.changes) {
        changesToApply[key] =
          updatedItem.changes[key] === null || updatedItem.changes[key] === undefined
            ? ''
            : updatedItem.changes[key]
      }
      return {
        ...item, // 保留原始 id 等資訊
        ...updatedItem.changes, // 修正：只合併 `changes` 物件，避免覆蓋整個 row
        isUpdated: true,
        changedFields: updatedItem.changes, // 額外儲存變動的欄位，供樣式使用
      }
    }
    // 如果沒有，則保留原樣
    return item
  })

  // 處理「新增」時表單內容
  allData.push(...changes.value.added.map((item) => ({ ...item, isNew: true })))

  return allData
})

// 檢查是否有變更來顯示按鈕
const hasChanges = computed(() => {
  return (
    changes.value.added.length > 0 ||
    changes.value.updated.length > 0 ||
    changes.value.deleted.length > 0
  )
})

// ==================編輯區==================
/**
 * 處理編輯事件，設定正在編輯的狀態(點選鉛筆按鈕觸發的事件)
 */
const handleEditField = ({ row, field }) => {
  editingCell.value = {
    rowId: getRowId(row),
    field: field,
    originalValue: row[field], // 儲存原始值以供取消時恢復
  }
  ElMessage.success(`正在編輯 ${row.theaterTypeName} 的 ${field} 欄位`)
}

/**
 * 處理儲存編輯的函式(點選確認時觸發的事件)
 */
const handleSaveEdit = ({ row, field, newValue }) => {
  // 判斷是在編輯新增資料還是更新資料
  const isNewRow = row.isNew
  const rowId = getRowId(row)

  // 動態判斷當前編輯的欄位是否為必填
  const column = tableColumns.find((col) => col.key === field)
  const isRequired = column && column.isRequired
  const isValueEmpty = newValue === null || newValue.trim() === ''

  // 檢查是否有必填欄位沒填
  if (isRequired && isValueEmpty) {
    ElMessage.error(`「${column.title}」欄位不能為空！`)
    editingCell.value = {
      rowId: rowId,
      field: field,
      originalValue: '',
    }
    return
  }

  // 檢查是否為初次填寫(即判斷填寫初期是不是空值且是一個新增的資料)
  const isFirstFill = editingCell.value.originalValue === '' && isNewRow

  if (isNewRow) {
    // 如果儲存的是「新增」資料，加進新增項目陣列
    const addedItem = changes.value.added.find((item) => getRowId(item) === rowId)
    if (addedItem) {
      addedItem[field] = newValue
    }
    ElMessage.success(`欄位 ${field} 已更新為 ${newValue}`)

    // 如果是初次填寫，自動跳轉到下一個可編輯欄位
    if (isFirstFill) {
      // 自動進入下一個可編輯欄位的編輯狀態
      const currentColumnIndex = tableColumns.findIndex((col) => col.key === field)
      // 這裡如果走出邊界的話，nextColumn 會是 undefined
      const nextColumn = tableColumns[currentColumnIndex + 1]

      if (nextColumn && nextColumn.isEditable) {
        // 找到下一個可編輯欄位，自動進入編輯模式
        editingCell.value = {
          rowId: rowId,
          field: nextColumn.key,
          originalValue: addedItem[nextColumn.key],
        }
        return
      }
    }
  }
  // 如果是編輯舊資料
  else {
    const originalValue = editingCell.value.originalValue

    // 如果輸入值與編輯前的暫存值不同
    if (originalValue !== newValue) {
      // 找出這筆資料的原始內容（從後端載入的）
      const originalItem = theaterTypes.value.find((item) => getRowId(item) === rowId)
      const originalBackendValue = originalItem ? originalItem[field] : null

      // 檢查新值是否與後端原始值相同，如果是，表示「繞了一圈回到原點」
      if (newValue === originalBackendValue) {
        // 找到變更紀錄
        const existingChange = changes.value.updated.find((u) => getRowId(u) === rowId)
        if (existingChange) {
          // 刪除這一個欄位的變更紀錄
          delete existingChange.changes[field]
          // 如果這筆資料的變更紀錄都清空了，就從 updated 陣列中移除整筆資料
          if (Object.keys(existingChange.changes).length === 0) {
            const index = changes.value.updated.findIndex((u) => getRowId(u) === rowId)
            changes.value.updated.splice(index, 1)
          }
          ElMessage.info('內容已恢復至原始狀態，已移除變更紀錄。')
        } else {
          // 如果變更清單裡沒有這筆資料，但新值卻等於後端原始值，
          // 表示這筆資料本來就沒有變更，不需要做任何事
          ElMessage.info('內容未變動')
        }
      } else {
        // 如果新值不等於後端原始值，才繼續處理變更
        const existingChange = changes.value.updated.find((u) => getRowId(u) === rowId)
        if (existingChange) {
          existingChange.changes = {
            ...existingChange.changes,
            [field]: newValue,
          }
        } else {
          // 這是關鍵修正！確保在新增更新紀錄時，也帶入原始的 version
          changes.value.updated.push({
            id: rowId,
            version: originalItem.version, // <-- 在此處加入版本號
            changes: { [field]: newValue },
          })
        }
        ElMessage.success(`欄位 ${field} 已更新為 ${newValue}`)
      }
    } else {
      ElMessage.info('內容未變動')
    }
  }

  // 重設編輯狀態
  editingCell.value = { rowId: null, field: null, originalValue: null }
}

/**
 * 處理取消編輯
 */
const handleCancelEdit = () => {
  // 判斷是否為新增的資料
  const isNewRow = editingCell.value.rowId && String(editingCell.value.rowId).startsWith('new-')
  // 取得列的 id 及 欄位標籤
  const rowId = editingCell.value.rowId
  const field = editingCell.value.field

  // 根據資料類型（新增或舊有）進行不同處理
  if (isNewRow) {
    const addedItem = changes.value.added.find((item) => getRowId(item) === rowId)

    // 動態檢查是否有必填欄位為空
    const requiredField = tableColumns.find((col) => col.isRequired)
    const isRequiredFieldEmpty =
      requiredField &&
      (addedItem[requiredField.key] === '' || addedItem[requiredField.key] === undefined)

    if (isRequiredFieldEmpty) {
      // 如果必填欄位為空，則刪除這條新增資料
      const newRowIndex = changes.value.added.findIndex((item) => getRowId(item) === rowId)
      if (newRowIndex !== -1) {
        changes.value.added.splice(newRowIndex, 1)
        ElMessage.info('已取消新增資料')
      }
    } else {
      // 如果必填欄位已填寫，只恢復當前編輯欄位的值
      const originalValue = editingCell.value.originalValue
      if (addedItem) {
        addedItem[field] = originalValue
      }
      ElMessage.info('已取消編輯，資料恢復至原始狀態')
    }
  } else {
    // 處理舊有資料的取消編輯
    // 找出這筆資料的原始內容
    const originalItem = theaterTypes.value.find((item) => getRowId(item) === rowId)

    // 如果原始資料存在
    if (originalItem) {
      // 在更新清單裡找到這筆資料的變更紀錄
      const updatedItem = changes.value.updated.find((u) => getRowId(u) === rowId)

      // 如果有變更紀錄
      if (updatedItem) {
        // 檢查這個欄位在原始資料中的值，是否和這次編輯前的原始值相同
        const isOriginalUnchanged = originalItem[field] === editingCell.value.originalValue

        // 如果原始資料中的值和編輯前的值不同，表示這個欄位已經有其他變更了
        if (!isOriginalUnchanged) {
          // 將變更清單中的該欄位，恢復到「這次編輯前」的最新值
          updatedItem.changes[field] = editingCell.value.originalValue
        } else {
          // 如果原始資料和編輯前的值相同，表示這個欄位沒有任何變更，直接從變更清單中移除
          delete updatedItem.changes[field]
        }

        // 檢查更新紀錄是否還有其他變動，如果沒有則移除
        if (Object.keys(updatedItem.changes).length === 0) {
          const updatedIndex = changes.value.updated.findIndex((u) => getRowId(u) === rowId)
          changes.value.updated.splice(updatedIndex, 1)
        } else if (updatedItem.changes[field] === originalItem[field]) {
          // 如果取消後的值跟原始值相同，表示這次修改沒有實際變動，就刪除這個變更紀錄
          delete updatedItem.changes[field]
          // 再次檢查是否還有其他變動
          if (Object.keys(updatedItem.changes).length === 0) {
            const updatedIndex = changes.value.updated.findIndex((u) => getRowId(u) === rowId)
            changes.value.updated.splice(updatedIndex, 1)
          }
        }
      }
    }

    ElMessage.info('已取消編輯，資料恢復至上次儲存狀態')
  }

  editingCell.value = { rowId: null, field: null, originalValue: null }
}
//======================================================

/**
 * 處理刪除事件，並處理與其他操作的邏輯衝突
 * @param {object} row - 被刪除的資料列物件
 */
const handleDelete = (row) => {
  const rowId = getRowId(row)

  // 1. 檢查是否為「新增」的資料
  const newRowIndex = changes.value.added.findIndex((item) => getRowId(item) === rowId)
  if (newRowIndex !== -1) {
    // 如果是新增的，直接從新增陣列中移除即可，因為還沒有提交到後端
    changes.value.added.splice(newRowIndex, 1)
    ElMessage.success('已移除新增的資料')
    return
  }

  // 2. 檢查是否為「編輯」中的資料
  const updatedRowIndex = changes.value.updated.findIndex((item) => getRowId(item) === rowId)
  if (updatedRowIndex !== -1) {
    // 如果是正在編輯的資料，先將其從 updated 陣列中移除
    changes.value.updated.splice(updatedRowIndex, 1)
  }

  // 3. 檢查是否已在「刪除」陣列中
  const isDeleted = changes.value.deleted.some((d) => getRowId(d) === rowId)
  if (!isDeleted) {
    // 如果不在刪除陣列中，則將其加入
    // 這裡需要將版本號也傳送給後端
    const originalItem = theaterTypes.value.find((item) => getRowId(item) === rowId)
    changes.value.deleted.push({ id: rowId, version: originalItem.version })
    ElMessage.warning(`已將 ${row.theaterTypeName} 標記為刪除`)
  } else {
    ElMessage.info('此資料已在刪除清單中。')
  }
}

/**
 * 處理取消刪除的函式
 * @param {object} row - 被取消刪除的資料列物件
 */
const handleCancelDelete = (row) => {
  const rowId = getRowId(row)
  // 在 deleted 陣列中找到該筆資料的索引
  const deletedIndex = changes.value.deleted.findIndex((d) => getRowId(d) === rowId)
  if (deletedIndex !== -1) {
    // 移除該筆資料
    changes.value.deleted.splice(deletedIndex, 1)
    ElMessage.info(`已取消刪除 ${row.theaterTypeName}`)
  }
}

// 處理新增事件
const handleAdd = () => {
  // 防呆機制，一筆新增完並確認才可以重複新增
  if (editingCell.value.rowId && String(editingCell.value.rowId).startsWith('new-')) {
    ElMessage.warning('請先完成目前的編輯！')
    return
  }

  // 建立一筆空白資料，ID可以設定為暫時值
  const newRowId = `new-${Date.now()}`
  const newRow = {
    theaterTypeId: newRowId, // 使用時間戳作為臨時 ID
    theaterTypeName: '',
    description: '',
  }
  changes.value.added.push(newRow)

  //進入編輯狀態
  editingCell.value = {
    rowId: newRowId,
    field: tableColumns.find((col) => col.isEditable).key,
    originalValue: '',
  }

  ElMessage.success('已新增一筆資料，請編輯後儲存')
}

// 處理確認變更事件
const handleConfirmChanges = async () => {
  try {
    // 檢查是否有正在編輯的欄位，如果有則阻止提交
    if (editingCell.value.rowId) {
      ElMessage.warning('請先完成目前的編輯後再提交。')
      return
    }

    // 準備要傳送給後端的資料物件
    const changesToSubmit = {
      added: [],
      updated: [],
      deleted: [],
    }

    // 1. 處理新增資料
    if (changes.value.added.length > 0) {
      const newItemsToSend = changes.value.added.map((item) => {
        // 移除臨時 ID，只傳送後端需要的欄位
        // 這裡確保只包含 'theaterTypeName' 和 'description'
        const { theaterTypeName, description } = item
        return { theaterTypeName, description }
      })
      changesToSubmit.added = newItemsToSend
    }

    // 2. 處理更新資料
    if (changes.value.updated.length > 0) {
      changesToSubmit.updated = changes.value.updated.map((item) => {
        // 後端需要 id、version 和 changes 物件
        const { id, version, changes: updatedChanges } = item
        return { theaterTypeId: id, version, changes: updatedChanges }
      })
    }

    // 3. 處理刪除資料
    // ==== 這是關鍵修改部分！ ====
    if (changes.value.deleted.length > 0) {
      // 根據後端API的設計，如果它需要一個單純的 Long 或 Integer 陣列
      // 則應該只傳送 ID，而不是整個物件
      const deletedIds = changes.value.deleted.map((item) => item.id)
      changesToSubmit.deleted = deletedIds
    }
    // ==== 這裡假設後端接收的是一個 ID 陣列。如果你後端需要 id 和 version 的物件，請使用我上一個回答的程式碼。
    // 你需要根據後端 API 的實際要求來決定這裡的格式。
    // 如果後端需要 { theaterTypeId: 1, version: 1 } 這樣的物件，那就不需要修改。
    // 如果後端只需要 [1, 2, 3] 這樣的陣列，就需要這樣修改。
    // 根據你遇到的錯誤，後端很可能只預期接收一個 ID 陣列。

    // 如果沒有任何變更，則不發送請求
    if (
      changesToSubmit.added.length === 0 &&
      changesToSubmit.updated.length === 0 &&
      changesToSubmit.deleted.length === 0
    ) {
      ElMessage.info('沒有任何變更需要提交。')
      return
    }

    console.log('提交給後端的物件=', changesToSubmit)
    const response = await theaterStore.commitTheaterTypeChanges(changesToSubmit)

    await theaterStore.fetchAllTheaterTypes()

    // 業務邏輯成功時，清空暫存並重新載入資料
    if (response && response.success) {
      changes.value = {
        added: [],
        updated: [],
        deleted: [],
      }
      editingCell.value = { rowId: null, field: null, originalValue: null }
      ElMessage.success(response.message || '所有變更已成功提交。')
    }
  } catch (error) {
    console.error('提交變更失敗:', error)
    if (error.response && error.response.status === 409) {
      ElMessage.error('資料已被其他使用者修改，請刷新頁面後再試。')
    }
    return
  }
}

/**
 * 處理重置變更的函式
 * 將所有暫存的變更都清空，回到頁面剛載入的樣子
 */
const handleResetChanges = () => {
  // 核心邏輯：將 changes ref 重設為初始狀態
  changes.value = {
    added: [],
    updated: [],
    deleted: [],
  }

  // 確保如果正在編輯，也一併取消編輯狀態
  editingCell.value = {
    rowId: null,
    field: null,
    originalValue: null,
  }

  // 給予使用者反饋
  ElMessage.info('所有變更已重置，頁面恢復至初始狀態。')
}

// 生命週期鉤子
onMounted(() => {
  theaterStore.fetchAllTheaterTypes()
})
</script>

<style scoped>
/* 增加這段 CSS 樣式 */
.table-container {
  padding: 20px;
}

/* 移除 el-card 的相關樣式，改用簡單的 div */
.operation-notes {
  margin-top: 10px; /* 調整與上方按鈕的間距 */
  /* border-radius: 8px; */
  /* padding: 20px; */ /* 移除內邊距，讓內容更緊湊 */
}

/* 新增按鈕區塊樣式 */
.add-new-row-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px; /* 與表格間的間距 */
}

/* 新增讀取與錯誤狀態的樣式 */
.loading-state,
.error-state {
  text-align: center;
  padding: 20px;
}

.notes-content ul {
  list-style: none; /* 移除預設的項目符號 */
  padding-left: 0;
  margin: 0;
}

.notes-content li {
  display: flex; /* 使用 flex 排列 */
  align-items: center; /* 垂直置中 */
  margin-bottom: 4px;
}

/* 調整 el-tag 與文字的間距 */
.notes-content .el-tag {
  margin-right: 1px;
}

/* 調整 el-icon 的樣式 */
.notes-content .el-icon {
  vertical-align: -0.15em;
}

/* 新增樣式，將文字變小 */
.notes-content,
.notes-content .el-text {
  font-size: 0.8em;
}
</style>
