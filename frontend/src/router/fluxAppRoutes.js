import ChangePassword from '@/features/Members/views/ChangePassword.vue'
import ForgotPassword from '@/features/Members/views/ForgotPassword.vue'
import Login from '@/features/Members/views/Login.vue'
import MemberProfile from '@/features/Members/views/MemberProfile.vue'
import Register from '@/features/Members/views/Register.vue' // 1. 引入註冊元件
import ResetPassword from '@/features/Members/views/ResetPassword.vue'
import SeatSelectionPage from '@/features/MovieSessions/views/front/SeatSelectionPage.vue'
import FluxAppLayout from '@/layout/FluxAppLayout.vue'
import Homepage from '@/layout/Homepage.vue'
import PublicProductList from '@/features/Product/views/PublicProductList.vue'
import CartPage from '@/features/Product/views/CartPage.vue'
import OrderSuccessPage from '@/features/Product/views/OrderSuccessPage.vue'
import PublicOrderPage from '@/features/Product/views/PublicOrderPage.vue'
import FilmDetail from '@/features/Movies/views/FilmDetail.vue'
import NowPlaying from '@/features/Movies/views/NowPlaying.vue'
import ComingSoon from '@/features/Movies/views/ComingSoon.vue'
import BookingLayout from '@/features/Ticket/views/front/BookingLayout.vue'
import PaymentMethodSelection from '@/features/Ticket/views/front/PaymentMethodSelection.vue'
import TicketSelection from '@/features/Ticket/views/front/TicketSelection.vue'
import SeatSelection from '@/features/Ticket/views/front/SeatSelection.vue'
import BookingConfirmation from '@/features/Ticket/views/front/BookingConfirmation.vue'
import Announcements from '@/features/Event/views/front/Announcements.vue'
import Events from '@/features/Event/views/front/Events.vue'
import AnnouncementDetail from '@/features/Event/views/front/AnnouncementDetail.vue'
import EventDetail from '@/features/Event/views/front/EventDetail.vue'
import CouponList from '@/features/Event/views/front/CouponList.vue'
import MemberCouponList from '@/features/Event/views/front/MemberCouponList.vue'
import PaymentSuccess from '@/features/Ticket/views/front/PaymentSuccess.vue'
import PaymentFailure from '@/features/Ticket/views/front/PaymentFailure.vue'

// 定義管理員相關的路由陣列
const fluxAppRoutes = [
  {
    path: '/', // 登入頁面的路徑
    //name: 'FluxAppLayout', // 路由名稱
    component: FluxAppLayout, // 對應的 Vue 組件
    children: [
      {
        path: '',
        redirect: '/home',
      },
      {
        path: 'home', // 首頁
        name: 'Home',
        component: Homepage,
        meta: { requiresAuth: false }, // 首頁是公開的
      },
      {
        path: 'products',
        name: 'Products',
        component: PublicProductList,
        meta: { requiresAuth: false }, // 商品頁面不需要登入
      },
      {
        path: 'cart',
        name: 'Cart',
        component: CartPage,
        meta: {
          requiresAuth: true,
          role: 'member'
        },
      },
      {
        path: 'check',
        name: 'Check',
        component: PublicOrderPage,
        meta: {
          requiresAuth: true,
          role: 'member'
        },
      },
      {
        path: '/order-success/:orderId',
        name: 'OrderSuccess',
        component: OrderSuccessPage,
      },
      {
        path: '/movie/:id', //
        name: 'FilmDetail',
        component: FilmDetail,
        props: true,
        meta: { requiresAuth: false }, //
      },
      {
        path: '/movie/nowPlaying', //
        name: 'NowPlaying',
        component: NowPlaying,
        props: true,
        meta: { requiresAuth: false }, //
      },
      {
        path: '/movie/comingSoon', //
        name: 'ComingSoon',
        component: ComingSoon,
        props: true,
        meta: { requiresAuth: false }, //
      },
      {
        path: '/member/profile', // 個人資訊
        name: 'MemberProfile',
        component: MemberProfile,
        meta: { requiresAuth: false },
      },
      {
        path: '/member/changepassword', // 修改密碼
        name: 'MemberChangePassword',
        component: ChangePassword,
        meta: {
          requiresAuth: true,
          role: 'member'
        },
      },
      {
        path: '/seats/:sessionId',
        name: 'SeatSelectionPage',
        component: SeatSelectionPage, // 查看座位
        meta: { requiresAuth: false },
      },
      {
        // 父路由：所有 /booking/... 的路徑都會先載入 BookingLayout 組件
        path: '/booking/:sessionId',
        component: BookingLayout,
        meta: {
          requiresAuth: true,
          role: 'member'
        },
        children: [
          {
            // 當路徑是 /booking/:sessionId (完全匹配父路徑) 時，
            // PaymentMethodSelection 組件會被渲染到 BookingLayout 的 <router-view> 中
            path: '',
            name: 'BookingPaymentMethod',
            component: PaymentMethodSelection,
            meta: { step: 1 }, // 使用 meta 來告訴進度條現在是第幾步
          },
          {
            path: 'tickets', // 新增的票種選擇頁面
            name: 'TicketSelection',
            component: TicketSelection,
            meta: { step: 2 }, // 進度條會顯示在第二步
          },
          {
            path: 'seatSelection',
            name: 'SeatSelection',
            component: SeatSelection,
            meta: { step: 3 }, // 進度條會顯示在第三步
          },
          {
            path: 'bookingConfirmation',
            name: 'BookingConfirmation',
            component: BookingConfirmation,
            meta: { step: 4 }, // 進度條會顯示在第四步
          },
        ],
      },
      // 票券付款成功路由
      {
        path: '/payment-success',
        name: 'PaymentSuccess',
        component: PaymentSuccess,
        meta: { requiresAuth: false },
      },
      // 票券付款失敗路由
      {
        path: '/payment-failure',
        name: 'PaymentFailure',
        component: PaymentFailure,
        meta: { requiresAuth: false },
      },
      // ===== 公告 ===== //
      // 列表頁
      {
        path: 'announcements',
        name: 'Announcements',
        component: Announcements,
        meta: { requiresAuth: false },
      },
      // 詳情頁
      {
        path: 'announcements/:id',
        name: 'AnnouncementDetail',
        component: AnnouncementDetail,
        meta: { requiresAuth: false },
      },

      // ===== 活動 ===== //
      {
        path: 'events',
        name: 'Events',
        component: Events,
        meta: { requiresAuth: false },
      },
      {
        path: 'events/:id',
        name: 'EventDetail',
        component: EventDetail,
        meta: { requiresAuth: false },
      },

      // ===== 前台優惠券 ===== //
      {
        path: 'coupons',
        name: 'coupons',
        component: CouponList,
        meta: { requiresAuth: false },
      },
    ],
  },
  {
    path: '/login', // 登入頁面的路徑
    name: 'FluxAppLogin', // 路由名稱
    component: Login, // 對應的 Vue 組件
    meta: { requiresAuth: false }, // meta 資訊：此頁面不需要認證即可訪問
  },
  // 2. 新增註冊路由
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false },
  },
  {
    path: '/forgot-password', // 忘記密碼的路徑
    name: 'ForgotPassword',
    component: ForgotPassword,
    meta: { requiresAuth: false },
  },
  {
    path: '/reset-password', // 重設密碼的路徑
    name: 'ResetPassword',
    component: ResetPassword,
    meta: { requiresAuth: false },
  },
]

// 導出路由陣列，供 Vue Router 使用
export default fluxAppRoutes
