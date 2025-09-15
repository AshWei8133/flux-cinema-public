<template>
  <section class="page">
    <h1>優惠專區</h1>

    <div class="toolbar">
      <input
        v-model.trim="keyword"
        class="search"
        type="search"
        placeholder="輸入關鍵字或代碼搜尋…"
        @keyup.enter="fetchList(0)"
      />
      <button class="btn" @click="fetchList(0)">搜尋</button>
    </div>

    <div v-if="loading" class="state">載入中…</div>
    <div v-else-if="error" class="state error">{{ error }}</div>

    <template v-else>
      <ul v-if="rows.length" class="coupons">
        <li v-for="c in rows" :key="c.id ?? c.couponId" class="coupon">
          <div class="left">
            <div class="name">{{ c.title ?? c.name }}</div>
            <div class="meta">
              <span class="code">代碼：{{ c.code }}</span>
              <span class="date"
                >效期：{{ fmtDate(c.validFrom ?? c.startDate) }} ~
                {{ fmtDate(c.validTo ?? c.endDate) }}</span
              >
            </div>
            <div class="desc">{{ c.description ?? '' }}</div>
          </div>
          <div class="right">
            <div class="price">
              <template v-if="isPercent(c)"> 折扣 {{ displayPercent(c) }}% </template>
              <template v-else> 折抵 ${{ c.discountAmount ?? c.value ?? 0 }} </template>
            </div>
            <router-link class="btn apply" to="/products">前往使用</router-link>
          </div>
        </li>
      </ul>

      <div v-else class="state">目前沒有可用優惠券</div>

      <div class="pager" v-if="totalPages > 1">
        <button class="btn" :disabled="page === 0" @click="fetchList(page - 1)">上一頁</button>
        <span>第 {{ page + 1 }} / {{ totalPages }} 頁</span>
        <button class="btn" :disabled="page + 1 >= totalPages" @click="fetchList(page + 1)">
          下一頁
        </button>
      </div>
    </template>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import httpClient from '@/services/api'

const loading = ref(true)
const error = ref('')
const rows = ref([])

const page = ref(0)
const size = ref(10)
const totalPages = ref(0)
const keyword = ref('')

const pickList = (d) => (Array.isArray(d) ? d : (d?.content ?? d?.list ?? d?.records ?? []))
const pickTotalPages = (d) => d?.totalPages ?? d?.total_pages ?? 1
const fmtDate = (s) => {
  if (!s) return ''
  const t = Date.parse(String(s).replace(/\//g, '-'))
  if (Number.isNaN(t)) return String(s)
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function fetchList(p = page.value) {
  loading.value = true
  error.value = ''
  try {
    const { data } = await httpClient.get('/coupons', {
      params: {
        page: p,
        size: size.value,
        sort: 'validFrom,desc',
        keyword: keyword.value || undefined,
      },
    })
    rows.value = pickList(data)
    totalPages.value = pickTotalPages(data)
    page.value = p
  } catch (e) {
    error.value = e?.response?.data?.message || '優惠券載入失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
const isPercent = (c) =>
  String(c.discountType || c.type || '')
    .toUpperCase()
    .includes('PERCENT')

const displayPercent = (c) => {
  const raw = Number(c.discountPercent ?? c.percent ?? c.discountRate ?? c.rate ?? c.value ?? NaN)
  if (!Number.isFinite(raw)) return '—'

  let percent
  if (raw > 0 && raw <= 1)
    percent = raw * 100 // 0.8 -> 80
  else if (raw > 1 && raw <= 10)
    percent = raw * 10 // 8 折 -> 80
  else if (raw > 10 && raw <= 100)
    percent = raw // 95 -> 95
  else return '—'

  // 保留 1 位，去掉尾端 .0
  return String(Math.round(percent * 10) / 10).replace(/\.0$/, '')
}

onMounted(() => fetchList(0))
</script>

<style scoped>
.page {
  color: #fff;
  padding: 24px;
}
h1 {
  margin: 0 0 16px;
  font-size: 28px;
  border-left: 4px solid #e50914;
  padding-left: 12px;
}
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.search {
  flex: 1;
  min-width: 240px;
  background: #1f1f1f;
  border: 1px solid #333;
  color: #fff;
  border-radius: 6px;
  padding: 8px 12px;
}
.btn {
  background: #e50914;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  cursor: pointer;
}
.state {
  color: #bbb;
}
.error {
  color: #ff8a8a;
}

.coupons {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.coupon {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  background: #1f1f1f;
  border: 1px dashed #e50914;
  border-radius: 12px;
  padding: 14px;
}
.left {
  flex: 1;
  min-width: 0;
}
.name {
  font-weight: 700;
  margin-bottom: 6px;
}
.meta {
  color: #9aa0a6;
  font-size: 14px;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}
.desc {
  color: #cfcfcf;
}
.right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}
.price {
  font-size: 22px;
  font-weight: 800;
  color: #ffd54f;
}
.apply {
  background: transparent;
  border: 1px solid #e50914;
  color: #e50914;
}
.pager {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}
</style>
