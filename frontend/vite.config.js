import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },

  //Product頁面加入這段我才有辦法載入產品
  server: {
    host: '0.0.0.0',
    proxy: {
      // 將所有以 '/api' 開頭的請求轉發到你的後端伺服器
      '/api': 'http://localhost:8888' // 請將 Port 替換成你的後端實際使用的 Port
    },
    // --- 【新增】允許來自 Cloudflare Tunnel 的請求 ---
    allowedHosts: [
      // 允許所有 trycloudflare.com 的子網域
      // 這樣即使 cloudflared 的隨機網址變了也無需修改
      'www.fluxcinema.com'
    ]
  }
})