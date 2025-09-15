<template>
  <section class="coupon-page">
    <div class="page-head">
      <div class="tools">
        <el-input
          v-model="store.keyword"
          placeholder="搜尋名稱/描述…"
          clearable
          @keyup.enter="store.reload"
        />
        <el-button type="primary" @click="store.reload">重新整理</el-button>
      </div>
    </div>

    <div class="grid" v-loading="store.loading">
      <template v-if="store.list.length">
        <CouponCard v-for="c in coupons" :key="c.couponId" :item="c" @claim="onClaim" />
      </template>
      <el-empty v-else description="目前沒有可領取的優惠券" :image-size="80" class="empty" />
    </div>

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="store.total"
        :page-size="store.size"
        v-model:current-page="store.page"
        @current-change="store.reload"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import CouponCard from '@/features/Event/components/CouponCard.vue'
import { usePublicCouponListStore } from '@/features/Event/store/usePublicCouponListStore'

const store = usePublicCouponListStore()

const coupons = computed(() => store.list)

// 連點鎖：避免多次快速點擊同一張券
const claimingSet = reactive(new Set<number>())

async function onClaim(couponId: number) {
  // 已在處理中 → 忽略
  if (claimingSet.has(couponId)) return

  const it = store.list.find((x) => x.couponId === couponId)
  if (!it) return

  // 前端預檢查邏輯，依賴後端回傳的 myClaimCount 和 redeemableTimes，變得非常可靠
  const limit = Number(it.redeemableTimes ?? 1)
  const myClaims = Number(it.myClaimCount ?? 0)
  const soldOut = typeof it.quantity === 'number' && it.quantity === 0

  // 如果 limit 無效或小於等於 0，視為無限領取，不進行 reachedLimit 檢查
  const isUnlimited = !Number.isFinite(limit) || limit <= 0
  const reachedLimit = !isUnlimited && myClaims >= limit

  // 如果前端已經判斷出不能領，就直接 return，節省一次 API 請求
  if (soldOut || reachedLimit) {
    if (soldOut) ElMessage.warning('此優惠券已被領取完畢')
    else if (reachedLimit) ElMessage.warning('您已達到此優惠券的領取上限')
    return
  }

  claimingSet.add(couponId)
  try {
    await store.claim(couponId)
    // ElMessage.success('領取成功，請至「會員中心 > 我的優惠券」查看')

    // 樂觀更新：在 store 層請求成功後，立即更新 UI
    if (it.myClaimCount != null) {
      it.myClaimCount = Number(it.myClaimCount) + 1
    } else {
      it.myClaimCount = 1
    }
    if (typeof it.quantity === 'number' && it.quantity > 0) it.quantity -= 1

    // 正式刷新，確保與後端數據最終一致
    // await store.reload()
  } catch (e: any) {
    // 【關鍵】這裡可以接收到後端回傳的精準錯誤訊息，例如："已達個人領取上限..."
    const msg = e?.response?.data?.message || e.message || '領取失敗'
    ElMessage.error(msg)
  } finally {
    claimingSet.delete(couponId)
  }
}

onMounted(store.reload)
</script>

<style scoped>
:root {
  --page-bg: #0f0f0f;
  --text: #e9eaeb;
  --line: #2a2a2a;
  --accent: #e50914;
}

.coupon-page {
  background: var(--page-bg);
  padding: 24px 16px 36px;
  min-height: 60vh;
  color: var(--text);
}

/* 頁首 + 工具列（與會員優惠券一致） */
.page-head {
  max-width: 1080px;
  margin: 0 auto 16px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: end;
}
.page-head h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 900;
}
.tools {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

/* Element Plus 輸入/按鈕暗色化 */
:deep(.el-input__wrapper) {
  background: #141414;
  border: 1px solid var(--line);
  box-shadow: none !important;
}
:deep(.el-input__inner) {
  color: #f3f4f6;
}
:deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent);
}
:deep(.el-button) {
  background: #141414;
  border-color: var(--line);
  color: #f3f4f6;
}
:deep(.el-button:hover) {
  border-color: var(--accent);
  color: var(--accent);
}

/* 票券牆（兩欄；<860px 一欄） */
.grid {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}
@media (max-width: 860px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
.empty {
  grid-column: 1 / -1;
}

/* 分頁暗色化 */
.pager {
  max-width: 1080px;
  margin: 18px auto 0;
  display: flex;
  justify-content: center;
}
:deep(.el-pagination.is-background .el-pager li) {
  background: #141414;
  border: 1px solid var(--line);
  color: #cfd2d4;
}
:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
}
:deep(.el-pagination button) {
  background: #141414;
  border: 1px solid var(--line);
  color: #cfd2d4;
}

/* Loading 遮罩淡一點 */
:deep(.el-loading-mask) {
  background-color: rgba(0, 0, 0, 0.35);
}

/* ===== 可選強化：讓 CouponCard 與會員券卡片同款質感（不改內文結構） ===== */
:deep(.coupon-card) {
  background: #1f1f1f;
  border: 1px solid #333;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(31, 15, 15, 0.35);
  transition: transform 0.15s ease;
}
:deep(.coupon-card:hover) {
  transform: translateY(-2px);
}
:deep(.coupon-card .thumb img) {
  object-fit: cover;
}
:deep(.coupon-card .meta .el-tag) {
  background: #2a2a2a;
  border-color: #3a3a3a;
  color: #cfcfcf;
}
:deep(.coupon-card .actions .el-button--primary) {
  background: #595757;
  border-color: #595757;
}
:deep(.coupon-card .actions .el-button--warning) {
  background: #493324;
  border-color: #493324;
}
</style>
