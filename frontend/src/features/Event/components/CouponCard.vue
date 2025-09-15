<template>
  <el-card class="coupon-card" shadow="hover" v-if="item">
    <div class="img" v-if="item.couponImageBase64">
      <img
        :src="img(item.couponImageBase64)"
        :alt="item.couponName"
        loading="lazy"
        decoding="async"
      />
      <div
        v-if="isSoldOut || isLimitReached"
        class="watermark"
        :class="{ soldout: isSoldOut, claimed: isLimitReached && !isSoldOut }"
      >
        {{ isSoldOut ? '已發放完畢' : '已達上限' }}
      </div>
    </div>

    <div class="content">
      <div class="title-row">
        <h3 class="name">{{ item.couponName }}</h3>
        <el-tag class="tag" v-if="item.minimumSpend">低消 {{ item.minimumSpend }} 元</el-tag>
      </div>
      <p class="desc" v-if="item.couponDescription">{{ item.couponDescription }}</p>
      <div class="meta">
        <el-tag :type="(discountTagForItem && discountTagForItem.type) ?? 'info'" effect="plain">
          {{ (discountTagForItem && discountTagForItem.text) ?? '基準票價' }}
        </el-tag>
        <el-tag class="ml8" effect="dark">{{ item.couponCategory || '—' }}</el-tag>
        <el-tag class="ml8" type="info" v-if="item.quantity != null"
          >數量 {{ item.quantity }}</el-tag
        >
        <el-tag class="ml8" type="info" v-if="item.redeemableTimes"
          >每人限領 {{ item.redeemableTimes }} 張</el-tag
        >
      </div>

      <div class="actions">
        <el-tooltip
          :disabled="!(isLimitReached || isSoldOut)"
          :content="tooltipContent"
          placement="top"
        >
          <div>
            <el-button
              class="claim-btn"
              :class="{ 'is-disabled': isLimitReached || isSoldOut }"
              type="primary"
              :loading="isClaiming"
              :disabled="isClaiming || isLimitReached || isSoldOut"
              @click="onClaim"
            >
              {{ buttonText }}
            </el-button>
          </div>
        </el-tooltip>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed, ref, defineProps, defineEmits } from 'vue'

defineOptions({ name: 'CouponMarketCard' })

const props = defineProps({
  item: { type: Object, required: true },
})
const emit = defineEmits(['claim']) // 父層處理 API，回來可要求刷新

const isClaiming = ref(false)

const isSoldOut = computed(
  () => typeof props.item?.quantity === 'number' && props.item.quantity === 0,
)

// 【核心邏輯】判斷使用者是否已達到該券的領取上限
const isLimitReached = computed(() => {
  const row = props.item || {}
  const myClaims = Number(row.myClaimCount ?? 0)
  const limit = Number(row.redeemableTimes ?? 1)

  // 如果 redeemableTimes 沒有設定或為 0，視為無限領取，永遠不會到達上限
  if (!Number.isFinite(limit) || limit <= 0) {
    return false
  }

  // 核心判斷：我已領取的數量 >= 每人限領數量
  return myClaims >= limit
})

// 提供動態的按鈕文字
const buttonText = computed(() => {
  if (isSoldOut.value) return '已領完'
  if (isLimitReached.value) {
    // 如果限領 1 張，顯示「已領取」即可。若 > 1，顯示「已達上限」更佳
    return (props.item.redeemableTimes ?? 1) > 1 ? '已達上限' : '✔ 已領取'
  }

  // 如果還可以領，且是多張模式，顯示進度
  const myClaims = Number(props.item.myClaimCount ?? 0)
  const limit = Number(props.item.redeemableTimes ?? 1)
  if (limit > 1) {
    return `領取 (${myClaims}/${limit})`
  }

  return '領取' // 預設情況
})

// 提供動態的 Tooltip 提示文字
const tooltipContent = computed(() => {
  if (isSoldOut.value) return '此優惠券已被領取完畢'
  if (isLimitReached.value) return '您已達到此優惠券的領取上限'
  return '' // 可領取狀態下不顯示 tooltip
})

const discountTagForItem = computed(() => {
  const row = props.item || {}
  const rawType = String(row.discountType || row.type || '').toUpperCase()
  const amt = Number(row.discountAmount ?? row.discount_amount ?? row.amount ?? row.value)
  const pct = Number(row.discountPercent ?? row.percent ?? row.discountRate ?? row.rate)
  const minSpend = Number(row.minimumSpend ?? row.minimum_spend)
  const cat = String(row.couponCategory || row.coupon_category || '')
  const text = `${row.couponName || ''} ${row.couponDescription || ''}`

  const hasPercentText = /(\d+(\.\d+)?)\s*折|折扣|%|％/.test(text)
  const hasFixedText = /折抵|滿|現折|減|元|折百/.test(text)

  let type = rawType
  if (!type) {
    if (Number.isFinite(pct) && pct > 0) type = 'PERCENTAGE'
    else if (hasPercentText) type = 'PERCENTAGE'
    else if (hasFixedText || (Number.isFinite(minSpend) && Number.isFinite(amt) && amt > 0))
      type = 'FIXED'
    else if (cat.includes('折價')) type = 'FIXED'
  }

  if (type === 'FIXED' && Number.isFinite(amt) && amt > 0) {
    return { type: 'warning', text: `折抵 ${Math.abs(amt)} 元` }
  }

  if (type === 'PERCENTAGE') {
    let payPercent = NaN
    const toPercent = (x) => (x <= 1 ? x * 100 : x <= 10 ? x * 10 : x <= 100 ? x : NaN)
    if (Number.isFinite(pct) && pct > 0) payPercent = toPercent(pct)
    else if (hasPercentText && Number.isFinite(amt) && amt > 0) payPercent = toPercent(amt)
    if (Number.isFinite(payPercent)) {
      const pretty = String(Math.round(payPercent * 10) / 10).replace(/\.0$/, '')
      return { type: 'success', text: `折扣 ${pretty}％` }
    }
  }

  return { type: 'info', text: '基準票價' }
})

// ★【核心修改】onClaim 現在變得非常簡單，它只負責發出請求信號
const onClaim = async () => {
  if (isClaiming.value || isLimitReached.value || isSoldOut.value) return
  // 注意：這裡不再需要 try/catch 和樂觀更新邏輯
  emit('claim', props.item.couponId)
}

const img = (b64) => `data:image/png;base64,${b64}`
const item = computed(() => props.item)
</script>

<style scoped>
.coupon-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 圖片 + 浮水印 */
.img {
  position: relative;
}
.img img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
}
.watermark {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-weight: 900;
  font-size: 26px;
  letter-spacing: 4px;
  border-radius: 8px;
  user-select: none;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  text-shadow:
    0 1px 2px rgba(0, 0, 0, 0.6),
    0 0 10px rgba(0, 0, 0, 0.35);
  transform: rotate(-10deg);
}
.watermark.soldout {
  box-shadow: inset 0 0 0 2px rgba(229, 9, 20, 0.6);
}
.watermark.claimed {
  box-shadow: inset 0 0 0 2px rgba(120, 144, 156, 0.6);
}

/* 文字區 */
.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.name {
  color: rgb(237, 187, 125);
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}
.tag {
  color: rgb(229, 100, 100);
  background-color: rgb(211, 203, 203);
}
.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 6px 0;
}
.actions {
  display: flex;
  justify-content: flex-end;
}
.ml8 {
  margin-left: 8px;
}
.desc {
  color: #ca4e4e;
  line-height: 1.4;
  margin: 4px 0;
}

/* 禁用時的視覺回饋 */
.claim-btn.is-disabled {
  cursor: not-allowed;
  opacity: 0.75;
}
</style>
