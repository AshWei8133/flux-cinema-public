// 引入管理員登入頁面組件
import AdminLogin from '@/features/Auth/views/AdminLogin.vue'
// 引入管理員佈局組件
import AdminLayout from '@/layout/AdminLayout.vue'
// 引入影廳列表頁面組件
import AdminMovies from '@/features/Movies/views/AdminMovies.vue'
import AdminMovieSessions from '@/features/MovieSessions/views/AdminMovieSessions.vue'
import AdminTheaters from '@/features/Theaters/views/AdminTheaters.vue'
import AdminProductEditor from '@/features/Product/views/AdminProductEditor.vue'
import AdminProductListPage from '@/features/Product/views/AdminProductListPage.vue'
import AdminRemoveList from '@/features/Product/views/AdminRemoveList.vue'
import AdminRestockList from '@/features/Product/views/AdminRestockList.vue'
import AdminOrderPage from '@/features/Product/views/AdminOrderPage.vue'
import AdminMembers from '@/features/Members/views/AdminMembers.vue'
import AdminDashboard from '@/features/Auth/views/AdminDashboard.vue'
import AdminTheaterTypes from '@/features/Theaters/views/AdminTheaterTypes.vue'
import AdminTheaterEditor from '@/features/Theaters/views/AdminTheaterEditor.vue'
import AdminAnnouncements from '@/features/Event/views/Announcement/AdminAnnouncements.vue'
import AdminMembersFormPage from '@/features/Members/views/AdminMembersFormPage.vue'
import AdminViewMemberShipPage from '@/features/Members/views/AdminViewMemberShipPage.vue'
import AdminAnnouncementEditor from '@/features/Event/views/Announcement/AdminAnnouncementEditor.vue'
import AdminTMDBMovies from '@/features/Movies/views/AdminTMDBMovies.vue'
import AdminDirectors from '@/features/Movies/views/AdminDirectors.vue'
import AdminActors from '@/features/Movies/views/AdminActors.vue'
import AdminGenre from '@/features/Movies/views/AdminGenre.vue'
import AdminEvents from '@/features/Event/views/Event/AdminEvents.vue'
import MovieSchedulePage from '@/features/MovieSessions/views/MovieSchedulePage.vue'
import AdminMemberLevels from '@/features/MemberLevel/views/AdminMemberLevels.vue'
import ShowMoviePlaying from '@/features/Movies/views/ShowMoviePlaying.vue'
import AdminEventEditor from '@/features/Event/views/Event/AdminEventEditor.vue'
import AdminTickets from '@/features/Ticket/views/AdminTickets.vue'
import AdminPriceRule from '@/features/Ticket/views/AdminPriceRule.vue'
import AdminTicketOrders from '@/features/Ticket/views/AdminTicketOrders.vue'
import AdminNewTMDBMovies from '@/features/Movies/views/AdminNewTMDBMovies.vue'
import AdminCouponCategories from '@/features/Event/views/Coupon/AdminCouponCategories.vue'
import AdminCouponsDesign from '@/features/Event/views/Coupon/AdminCouponsDesign.vue'
import AdminEventCategories from '@/features/Event/views/Event/AdminEventCategories.vue'
import AdminCouponList from '@/features/Event/views/Coupon/AdminCouponList.vue'
import AdminCouponDetail from '@/features/Event/views/Coupon/AdminCouponDetail.vue'

// 定義管理員相關的路由陣列
const adminRoutes = [
  {
    path: '/admin/login', // 登入頁面的路徑
    name: 'AdminLogin', // 路由名稱
    component: AdminLogin, // 對應的 Vue 組件
    meta: { requiresAuth: false }, // meta 資訊：此頁面不需要認證即可訪問
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: '',
        redirect: '/admin/members',
      },
      // {
      //   path: 'dashboard',
      //   component: AdminDashboard,
      //   name: 'AdminDashboard',
      // },
      {
        path: 'members',
        component: AdminMembers,
        name: 'AdminMembers',
        meta: { title: '會員管理' },
      },
      {
        path: 'members/information/:id',
        component: AdminViewMemberShipPage,
        name: 'MemberInformation',
        props: (route) => ({ memberId: Number(route.params.id) }),
        meta: { title: '會員管理 > 會員資訊' },
      },
      {
        path: 'members/add',
        name: 'AdminAddMember',
        component: AdminMembersFormPage,
        meta: { title: '會員管理 > 新增會員' },
      },
      {
        path: 'members/edit/:id',
        name: 'AdminEditMember',
        component: AdminMembersFormPage,
        meta: { title: '會員管理 > 編輯會員' },
      },
      {
        path: 'memberlevels',
        component: AdminMemberLevels,
        name: 'AdminMemberLevels',
        meta: { title: '會員等級' },
      },
      {
        path: 'movies',
        component: AdminMovies,
        name: 'AdminMovies',
        meta: { title: '電影維護' },
      },
      {
        path: 'movies/TMDBMovies',
        component: AdminTMDBMovies,
        name: 'AdminTMDBMovies',
        meta: { title: 'TMDB電影維護' },
      },
      {
        path: 'movies/TMDBMovies/new',
        component: AdminNewTMDBMovies,
        name: 'AdminNewTMDBMovies',
        meta: { title: 'TMDB電影維護' },
      },
      {
        path: 'movies/Directors',
        component: AdminDirectors,
        name: 'AdminDirectors',
        meta: { title: '電影導演維護' },
      },
      {
        path: 'movies/Actors',
        component: AdminActors,
        name: 'AdminActors',
        meta: { title: '電影演員維護' },
      },
      {
        path: 'movies/Genre',
        component: AdminGenre,
        name: 'AdminGenre',
        meta: { title: '電影類型維護' },
      },
      // {
      //   path: 'movies/ShowMoviePlaying',
      //   component: ShowMoviePlaying,
      //   name: 'ShowMoviePlaying',
      //   meta: { title: '前台測試' },
      // },
      {
        path: 'theaters',
        component: AdminTheaters,
        name: 'AdminTheaters',
        // meta屬性定義title，即該路由的標題名稱
        meta: { title: '影廳維護' },
      },
      {
        path: 'theaters/add',
        name: 'AdminTheaterAdd',
        component: AdminTheaterEditor,
        meta: { title: '影廳維護 > 新增影廳' },
      },
      {
        path: 'theaters/:id',
        name: 'AdminTheaterUpdate',
        component: AdminTheaterEditor,
        props: true,
        meta: { title: '影廳維護 > 影廳詳情' },
      },
      {
        path: 'theaterTypes',
        component: AdminTheaterTypes,
        name: 'AdminTheaterTypes',
        meta: { title: '影廳類型維護' },
      },
      {
        path: 'movieSessions',
        component: AdminMovieSessions,
        name: 'AdminMovieSessions',
        meta: { title: '場次管理' },
      },
      {
        path: 'movieSessions/schedule/:date',
        component: MovieSchedulePage,
        name: 'movieScheduleEditor',
        props: true,
        meta: { title: '場次管理 > 排片編輯' },
      },
      {
        path: 'movieSessions/history/:date',
        component: MovieSchedulePage,
        name: 'movieScheduleHistory',
        props: true,
        meta: { title: '場次管理 > 排片歷史紀錄' },
      },
      {
        path: 'tickets',
        component: AdminTickets,
        name: 'AdminTickets',
        meta: { title: '票券管理 > 票種管理' },
      },
      {
        path: 'tickets/priceRule',
        component: AdminPriceRule,
        name: 'AdminPriceRule',
        meta: { title: '票券管理 > 票價規則管理' },
      },
      {
        path: 'tickets/order',
        component: AdminTicketOrders,
        name: 'AdminTicketOrders',
        meta: { title: '票券管理 > 票券訂單管理' },
      },
      // {
      //   path: 'tickets',
      //   component: AdminTickets,
      //   name: 'AdminTickets',
      // },

      {
        path: 'announcements',
        component: AdminAnnouncements,
        name: 'AdminAnnouncements',
        meta: { title: '公告管理' },
      },
      {
        path: 'announcements/add',
        name: 'AdminAnnouncementAdd',
        component: AdminAnnouncementEditor,
        meta: { title: '公告管理 > 新增公告' },
      },
      {
        path: 'announcements/edit/:id',
        name: 'AdminAnnouncementEdit',
        component: AdminAnnouncementEditor,
        props: true,
        meta: { title: '公告管理 > 公告編輯' },
      },
      {
        path: 'events',
        component: AdminEvents,
        name: 'AdminEvents',
        meta: { title: '活動列表' },
      },
      {
        path: 'events/add',
        name: 'AdminEventAdd',
        component: AdminEventEditor,
        meta: { title: '活動管理 > 新增活動' },
      },
      {
        path: 'events/edit/:id',
        name: 'AdminEventEdit',
        component: AdminEventEditor,
        props: true,
        meta: { title: '活動管理 > 活動編輯' },
      },
      {
        path: 'eventCategories',
        component: AdminEventCategories,
        name: 'AdminEventCategories',
        meta: { title: '活動管理 > 活動類型管理' },
      },
      {
        path: 'coupons/design',
        component: AdminCouponsDesign,
        name: 'AdminCouponsDesign',
        meta: { title: '優惠券管理 > 設計' },
      },
      {
        path: 'coupons',
        component: AdminCouponList,
        name: 'AdminCouponList',
        meta: { title: '優惠券管理 > 列表' },
      },
      {
        path: 'coupons/edit/:id',
        name: 'AdminCouponDetail',
        component: AdminCouponDetail,
        props: true,
        meta: { title: '優惠券管理 > 詳情' },
      },
      {
        path: 'coupon-categories',
        component: AdminCouponCategories,
        name: 'AdminCouponCategories',
        meta: { title: '優惠券管理 > 類型管理' },
      },
      {
        path: 'products',
        name: 'ProductList',
        component: AdminProductListPage,
        meta: { title: '產品列表 ' },
      },
      {
        path: 'products/add',
        name: 'AdminProductAdd',
        component: AdminProductEditor,
      },
      {
        path: 'products/edit/:id',
        name: 'AdminProductEdit',
        component: AdminProductEditor,
      },
      {
        path: '/admin/products/removeList',
        name: 'AdminProductRemoveList',
        component: AdminRemoveList,
        meta: { title: '已下架產品 ' },
      },
      {
        path: '/admin/products/restockList',
        name: 'AdminProductRestockList',
        component: AdminRestockList,
        meta: { title: '需補貨產品 ' },
      },
      {
        path: 'orders',
        name: 'OrderLists',
        component: AdminOrderPage,
        meta: { title: '訂單管理 ' },
      },
    ],
  },
]

// 導出路由陣列，供 Vue Router 使用
export default adminRoutes
