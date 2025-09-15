<template>
    <img 
      :src="posterUrl" 
      alt="電影海報" 
      style="width: 70px; height: 100px; object-fit: cover; border-radius: 4px;"
    />
  </template>
  
  <script setup>
  import { ref, watch, onUnmounted } from 'vue';
  import httpClient from '@/services/api'; // 在這裡引入 httpClient
  
  // 1. 接收從父元件傳來的 movieId
  const props = defineProps({
    movieId: {
      type: [String, Number],
      required: true
    }
  });
  
  // 2. 每個元件實例都有自己獨立的 posterUrl
  const posterUrl = ref('https://placehold.co/70x100/cccccc/333333?text=載入中');
  let objectUrl = null; // 用來存放臨時 URL，以便之後釋放
  
  // 3. 獲取單張圖片的異步函數
  const fetchPoster = async (id) => {
    if (!id) return;
    
    try {
      const response = await httpClient.get(
        `/admin/movie/movies/${id}/poster`, 
        { responseType: 'blob' }
      );
      objectUrl = URL.createObjectURL(response);
      posterUrl.value = objectUrl;
    } catch (error) {
      // console.error(`獲取電影 ${id} 海報失敗:`, error);
      posterUrl.value = 'https://placehold.co/70x100/cccccc/333333?text=無海報';
    }
  };
  
  // 4. 監聽 movieId 的變化，當它有值時，就去獲取海報
  watch(() => props.movieId, (newId) => {
    if (objectUrl) { // 如果是重新載入，先釋放舊的 URL
      URL.revokeObjectURL(objectUrl);
    }
    fetchPoster(newId);
  }, { immediate: true });
  
  // 5. 元件銷毀時，釋放臨時 URL 佔用的內存，防止內存洩漏
  onUnmounted(() => {
    if (objectUrl) {
      URL.revokeObjectURL(objectUrl);
    }
  });
  </script>