--最終版
-- 管理者表格
DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
	admin_id INT IDENTITY(1,1) PRIMARY KEY, -- 管理者唯一識別碼
	admin_name NVARCHAR(50) NOT NULL UNIQUE, -- 管理者登入帳號，需唯一
	password NVARCHAR(100) NOT NULL -- 管理者密碼
);

-- ==============================
-- 👤 會員相關表格
-- ==============================

-- 會員等級表格：定義不同會員等級的資訊
DROP TABLE IF EXISTS member_level;
CREATE TABLE member_level (
    member_level_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 會員等級的唯一識別碼
    level_name NVARCHAR(20) NOT NULL, -- 等級名稱，例如：'普通會員', '白金會員'
    level_icon VARBINARY(MAX), -- 等級圖示的二進位資料
    threshold_lower_bound INT NOT NULL, -- 晉升到此等級所需的最低點數或消費門檻
    upgrade_condition_description NVARCHAR(MAX) -- 升級條件的詳細描述
);

-- 會員表格
DROP TABLE IF EXISTS member;
CREATE TABLE member (
    member_id INT IDENTITY(1,1) PRIMARY KEY, -- 會員唯一識別碼
    username NVARCHAR(50) NOT NULL UNIQUE, -- 會員登入帳號，必須唯一且不為空
    password NVARCHAR(100) NOT NULL, -- 會員登入密碼
    gender CHAR(1) NOT NULL, -- 性別 (例如 'M' 男性, 'F' 女性, 'O' 其他)
    birth_date DATE, -- 會員生日
    email NVARCHAR(100) NOT NULL UNIQUE, -- 會員電子郵件，必須唯一且不為空
	phone NVARCHAR(20) NOT NULL UNIQUE, -- 會員電話號碼，必須唯一且不為空
    profile_photo VARBINARY(MAX), -- 會員大頭貼的二進位資料
    register_date DATETIME DEFAULT GETDATE(), -- 會員註冊日期，預設為當前時間
    third_party_login_id NVARCHAR(100), -- 第三方登入系統的用戶ID (例如 Google ID, Facebook ID)，允許為空
    login_method NVARCHAR(100),         -- 會員登入方式 (例如 'email', 'google', 'facebook')，允許為空
    last_login_time DATETIME,           -- 會員最後一次登入的時間，允許為空
    member_points INT NOT NULL DEFAULT 0 -- 會員累計點數，預設值為0，且不可為空
);


DROP TABLE IF EXISTS member_level_record;
-- 會員等級紀錄表格：追蹤會員在不同時間區間所屬的等級
CREATE TABLE member_level_record (
    member_level_record_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 會員等級紀錄的唯一識別碼
    member_id INT NOT NULL, -- 關聯的會員ID
    member_level_id INT NOT NULL, -- 關聯的會員等級ID
    start_date DATETIME NOT NULL, -- 等級生效的開始時間
    end_date DATETIME -- 等級失效的結束時間 (若為當前等級，可能為 NULL)
);

DROP TABLE IF EXISTS member_coupon;
-- 會員優惠券表格：記錄會員擁有的優惠券資訊
CREATE TABLE member_coupon (
    member_coupon_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 會員優惠券的唯一識別碼
    member_id INT, -- 關聯的會員ID
    coupon_id INT,  -- 關聯的優惠券ID
    status NVARCHAR(20), -- 優惠券狀態 (例如 '未使用', '已使用', '已過期')
    acquisition_time DATETIME, -- 會員獲得優惠券的時間
    usage_time DATETIME -- 優惠券實際使用的時間 (若未使用則為 NULL)
);

DROP TABLE IF EXISTS event_participation;
-- 活動參加紀錄表格：記錄會員參加各類活動的資訊
CREATE TABLE event_participation (
    event_record_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 活動參加紀錄的唯一識別碼
    member_id INT NOT NULL, -- 關聯的會員ID
    event_id INT NOT NULL, -- 關聯的活動ID (假設 event 表格已存在)
    event_category NVARCHAR(20) NOT NULL, -- 活動類別 (例如 '電影', '商品', '抽獎')
    participation_time DATETIME NOT NULL -- 會員參加活動的時間
);

-- 會員電影收藏清單表格：記錄會員喜愛的電影
DROP TABLE IF EXISTS favorite_movie;
CREATE TABLE favorite_movie (
    member_id INT NOT NULL, -- 關聯的會員ID
    movie_id INT NOT NULL, -- 關聯的電影ID (假設 movie 表格已存在)
    added_to_favorites_time DATETIME NOT NULL, -- 加入收藏清單的時間
    PRIMARY KEY (member_id, movie_id) -- 複合主鍵，確保一個會員不能重複收藏同一部電影
);

--DROP TABLE IF EXISTS member_points;
-- 會員點數表格：
-- CREATE TABLE member_points (
--     point_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 會員點數唯一識別碼
-- 	member_id INT NOT NULL, -- 關聯的會員ID
-- 	change_type char(1) NOT NULL, -- 異動類型（加點/扣點）
-- 	points_changed INT NOT NULL, -- 異動的點數數量
-- 	description NVARCHAR(255) not null, -- 異動說明（例：購物折抵、註冊贈點）
-- 	created_at DATETIME not null -- 異動發生時間
-- );

--DROP TABLE IF EXISTS discussion_posts;
-- 討論區表格：
-- CREATE TABLE discussion_posts (
--     post_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 討論區文章唯一識別碼
-- 	member_id INT NOT NULL, -- 關聯的會員ID
-- 	post_title NVARCHAR(50) not null, -- 文章主題
-- 	post_content NVARCHAR(255) not null, -- 文章內容
-- 	post_date DATETIME NOT NULL, -- 發文日期時間
-- 	edited_date DATETIME NOT NULL, -- 編輯時間
-- 	view_count INT not null, -- 瀏覽次數
-- 	reply_count INT not null --回覆數量
-- );

--DROP TABLE IF EXISTS discussion_replies;
-- 討論區回覆表格：
-- CREATE TABLE discussion_replies (
--     reply_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 討論區回覆唯一識別碼
-- 	post_id INT NOT NULL, -- 關聯的討論區文章ID
-- 	member_id INT NOT NULL, -- 關聯的會員ID
-- 	reply_content NVARCHAR(255) not null, -- 回覆內容
-- 	reply_date DATETIME NOT NULL, -- 回覆日期時間
-- 	parent_reply_id INT, -- 父回覆 ID，用於巢狀回覆，可為 NULL
-- 	is_edited BIT DEFAULT 1, -- 是否編輯過
-- );

--DROP TABLE IF EXISTS group_activities;
-- 揪團活動表格：
-- CREATE TABLE group_activities (
--     party_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 揪團活動唯一識別碼
-- 	member_id INT NOT NULL, -- 關聯的發起人會員ID
-- 	movie_id INT NOT NULL, -- 關聯的電影ID
-- 	party_title NVARCHAR(50) not null, -- 揪團活動主題
-- 	party_content NVARCHAR(255) not null, -- 揪團活動內容
-- 	scheduled_date DATETIME NOT NULL, -- 預計觀看時間
-- 	required_members INT not null, -- 預計人數
-- 	current_participants INT not null, -- 目前參與人數
-- 	location NVARCHAR(255) NOT NULL, -- 觀影地點
-- 	party_status NVARCHAR(20) not null, -- 狀態 (進行中/已滿團/已結束)
-- 	creation_date DATETIME not null --創建日期時間
-- );

--DROP TABLE IF EXISTS group_participants;
-- 揪團參與者表格：
-- CREATE TABLE group_participants (
--     participant_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 揪團參與者唯一識別碼
-- 	party_id INT NOT NULL, -- 關聯的揪團活動ID
-- 	member_id INT NOT NULL, -- 關聯的參與會員ID
-- 	join_date DATETIME NOT NULL, -- 加入揪團日期時間
-- 	participant_status NVARCHAR(20) not null -- 參與狀態
-- );

-- ==============================
-- 活動相關表格
-- ==============================
-- 如果表格已存在，則先刪除，避免重複創建
DROP TABLE IF EXISTS announcement;
-- 公告表格：列出所有公告資料
CREATE TABLE announcement (
    announcement_id INT IDENTITY(1,1) PRIMARY KEY, -- 公告的唯一識別碼
    title NVARCHAR(100) NOT NULL, -- 公告標題，不可為空
    content NVARCHAR(MAX) NOT NULL, -- 公告內容或詳細說明
    announcement_image VARBINARY(MAX), -- 公告圖片的二進位資料，可為 NULL
    publish_date DATE NOT NULL -- 公告發布日期，不可為空
);

DROP TABLE IF EXISTS event;
-- 活動表格：定義網站上的各種活動資訊
CREATE TABLE event (
    event_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 活動的唯一識別碼
	event_category_id INT,
    title NVARCHAR(100) NOT NULL, -- 活動標題
    content NVARCHAR(MAX) NOT NULL, -- 活動內容或詳細說明
    event_category NVARCHAR(20) , -- 活動類別 (例如 '促銷', '新片', '會員限定')
    image VARBINARY(MAX), -- 活動宣傳圖片的二進位資料
    start_date DATE NOT NULL, -- 活動開始日期
    end_date DATE,-- 活動結束日期 (若無則為 NULL，表示長期活動)
	session_count INT , -- 活動場次數量
	status nvarchar(50)  -- 活動啟用狀態 
);

-- 如果表格已存在，則先刪除，避免重複創建
DROP TABLE IF EXISTS event_category;
-- 建立活動類別表格
CREATE TABLE event_category (
    event_category_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 類別的唯一識別碼
    event_category_name NVARCHAR(50) NOT NULL, -- 類別名稱，例如：'會員專享', '新片促銷'，必須唯一
    description NVARCHAR(MAX) -- 類別的詳細描述 (可選)
);

DROP TABLE IF EXISTS coupon;
-- 優惠券表格：定義各種優惠券的詳細資訊
CREATE TABLE coupon (
    coupon_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 優惠券的唯一識別碼，自動遞增主鍵
    serial_number NVARCHAR(36), -- 優惠券序號，最長 36 字元，必須是唯一的，不可為空
    event_id INT, -- 關聯到哪個活動產生了此優惠券
	coupon_category_id INT, -- 關聯到哪個活動類別產生了此優惠券
	event_eligibility_id int,
    coupon_name NVARCHAR(50), -- 優惠券名稱
    coupon_description NVARCHAR(MAX), -- 優惠券的詳細描述或使用說明
    coupon_category NVARCHAR(20), -- 優惠券類別 (例如 '折扣券', '贈品券', '滿額折')
	discount_type NVARCHAR(20), -- 折扣類型
    discount_amount INT, -- 折扣金額或折扣比例 (可以是 NULL 如果是贈品券等)
    minimum_spend INT, -- 最低消費金額 (若無則為 NULL)
    coupon_image VARBINARY(MAX), -- 優惠券圖片的二進位資料 (可以為 NULL)
    status NVARCHAR(50), -- 優惠券啟用狀態 (0: 禁用, 1: 啟用)
    redeemable_times INT, -- 每張優惠券實例可被兌換的總次數 (例如每張券只能兌換一次)
    quantity INT -- 新增：此優惠券類型可發放的總數量，不可為空
);

DROP TABLE IF EXISTS coupon_category;
-- 優惠券類別表格：定義各種優惠券類別的詳細資訊
CREATE TABLE coupon_category (
	coupon_category_id int IDENTITY(1,1) NOT NULL,
	coupon_category_name nvarchar(50) NOT NULL,
	description nvarchar(max) NULL
);

DROP TABLE IF EXISTS lottery_winner;
-- 抽獎活動中獎名單表格：記錄抽獎活動的中獎者資訊
CREATE TABLE lottery_winner (
 lottery_winner_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 中獎名單的唯一識別碼
    event_record_id INT NOT NULL, -- 關聯的活動參加紀錄ID，可由此追溯會員及活動資訊
    prize_content NVARCHAR(100) NOT NULL, -- 中獎獎品的內容描述
    draw_time DATETIME NOT NULL -- 開獎或中獎通知的時間
);

DROP TABLE IF EXISTS event_feedback;
-- 活動回饋：會員參加活動後的評論及是否可以參加抽獎
CREATE TABLE event_feedback (
feedback_id INT IDENTITY(1,1) PRIMARY KEY, -- 自動編號主鍵
member_id INT NOT NULL, -- 外鍵,會員
event_id INT NOT NULL, -- 外鍵,活動
rating INT NOT NULL CHECK (Rating BETWEEN 1 AND 5), -- 1~5 星評分
comment NVARCHAR(MAX) NULL, -- 留言內容
feedback_time DATETIME NOT NULL, -- 留言時間
is_eligible BIT NOT NULL, -- 是否符合抽獎資格
);



DROP TABLE IF EXISTS event_draw_qualified;
-- 抽獎資格紀錄：記錄會員抽獎來源
CREATE TABLE event_draw_qualified (
qualified_id INT IDENTITY(1,1) PRIMARY KEY, -- 主鍵,自動編號
event_id INT NOT NULL, -- 外鍵,活動 ID
member_id INT NOT NULL, -- 外鍵,會員 ID
source NVARCHAR(50) NOT NULL, -- 來源:留言、參與活動等
create_time DATETIME NOT NULL, -- 建立時間
);

DROP TABLE IF EXISTS event_eligibility;
-- 活動適用條件表格：記錄特定活動適用於哪些電影
CREATE TABLE event_eligibility (
event_eligibility_id INT IDENTITY(1, 1) PRIMARY KEY NOT NULL,
event_id INT NOT NULL,                            
movie_id INT NULL,                                  -- 外部鍵：關聯到 'movies' 表格的電影ID (可選)
session_id INT NULL,                                -- 外部鍵：關聯到 'sessions' 表格的場次ID (可選)
member_level_id INT NULL  
);
-- 外部鍵：關聯到 'member_levels' 表格的會員等級ID (可選)

--DROP TABLE IF EXISTS event_movie;
-- 活動適用電影表格：記錄特定活動適用於哪些電影

--DROP TABLE IF EXISTS event_session;
-- 活動適用場次表格：記錄特定活動適用於哪些電影場次

--DROP TABLE IF EXISTS event_level;
-- 活動適用會員等級表格：記錄特定活動適用於哪些會員等級


-- ==============================
-- 🎥 電影資料相關表格
-- ==============================
DROP TABLE IF EXISTS tmdb_movie;
-- TMDB Movie 資料表：儲存從 TMDB (The Movie Database) 導入的原始電影資料
CREATE TABLE tmdb_movie (
    tmdb_movie_id INT PRIMARY KEY, -- TMDB 電影的原始ID，作為主鍵
    title_local NVARCHAR(100), -- 電影的本地化名稱
    title_english NVARCHAR(100), -- 電影的原始英文名稱
    release_date DATE, -- 電影的上映日期
    certification NVARCHAR(50), -- 電影分級 (例如 PG-13, R)
    overview NVARCHAR(MAX), -- 電影簡介或劇情描述
    trailer_url NVARCHAR(500), -- 電影預告片連結
    duration_minutes INT, -- 電影片長 (以分鐘表示)
    poster_image VARBINARY(MAX), -- 電影海報圖片的二進位資料
    original_language NVARCHAR(20), -- 電影的原始語言
    popularity DECIMAL(6,2), -- TMDB 提供的熱門度評分
    vote_average DECIMAL(3,1), -- TMDB 提供的平均投票分數
    vote_count INT, -- TMDB 提供的投票總數
    create_time DATETIME DEFAULT GETDATE(), -- 紀錄資料建立時間，預設為當前時間
    update_time DATETIME -- 紀錄資料最後更新時間
);

-- 刪除原有的 movie 表 (在重建 movie 表之前使用，目前應被註解)
DROP TABLE IF EXISTS movie;

-- Movie 資料表：儲存經過本地化或處理後的電影資料
CREATE TABLE movie (
    movie_id INT IDENTITY(1,1) PRIMARY KEY, -- 電影的唯一識別碼
    tmdb_movie_id INT, -- 關聯的 TMDB 電影ID (若有)
    title_local NVARCHAR(100) NOT NULL, -- 電影的本地化名稱，不可為空
    title_english NVARCHAR(100), -- 電影的英文原始名稱
    release_date DATE NOT NULL, -- 電影的上映日期，不可為空
    off_shelf_date DATE, -- 電影的下檔日期 (若已下檔)
    certification NVARCHAR(50), -- 電影分級，不可為空
    overview NVARCHAR(MAX), -- 電影簡介
    trailer_url NVARCHAR(500), -- 預告片連結
    duration_minutes INT not null, -- 片長 (分鐘)
    poster_image VARBINARY(MAX), -- 電影海報圖片的二進位資料
    original_language NVARCHAR(20), -- 原始語言
    popularity DECIMAL(6,2), -- 熱門度
    vote_average DECIMAL(3,1), -- 平均評分
    vote_count INT, -- 投票總數
	status Bit not null, -- 是否上架
    preview_image_1 VARBINARY(MAX), -- 預覽圖片1
    preview_image_2 VARBINARY(MAX), -- 預覽圖片2
    preview_image_3 VARBINARY(MAX), -- 預覽圖片3
    preview_image_4 VARBINARY(MAX), -- 預覽圖片4
    create_time DATETIME NOT NULL DEFAULT GETDATE(), -- 紀錄資料建立時間，預設為當前時間
    update_time DATETIME -- 紀錄資料最後更新時間
);

DROP TABLE IF EXISTS director;
-- 導演資料表：儲存電影導演的資訊
CREATE TABLE director (
    director_id INT IDENTITY(1,1) PRIMARY KEY, -- 導演的唯一識別碼
	tmdb_director_id INT,
    name NVARCHAR(100) NOT NULL, -- 導演姓名，不可為空
	director_summary NVARCHAR(max), -- 導演簡介
);

DROP TABLE IF EXISTS movie_director;
-- 電影與導演對應表：記錄每部電影有哪些導演
CREATE TABLE movie_director (
    movie_id INT NOT NULL, -- 關聯的電影ID
    director_id INT NOT NULL, -- 關聯的導演ID
    PRIMARY KEY (movie_id, director_id) -- 複合主鍵，確保一部電影不能重複關聯同一位導演
);

DROP TABLE IF EXISTS genre;
-- 類型資料表：儲存電影的類型資訊
CREATE TABLE genre (
    genre_id INT　IDENTITY(1,1) PRIMARY KEY, -- 類型的唯一識別碼
    tmdb_genre_id INT, -- 類型的唯一識別碼
    name NVARCHAR(100) NOT NULL -- 類型名稱，不可為空
);

DROP TABLE IF EXISTS movie_genre;
-- 電影與類型對應表：記錄每部電影所屬的類型
CREATE TABLE movie_genre (
    movie_id INT NOT NULL, -- 關聯的電影ID
    genre_id INT NOT NULL, -- 關聯的類型ID
    PRIMARY KEY (movie_id, genre_id) -- 複合主鍵，確保一部電影不能重複關聯同一種類型
);

DROP TABLE IF EXISTS actor;
-- 演員資料表：儲存電影演員的資訊
CREATE TABLE actor (
    actor_id INT IDENTITY(1,1) PRIMARY KEY, -- 演員的唯一識別碼
	tmdb_actor_id INT,
    name NVARCHAR(100) NOT NULL, -- 演員姓名，不可為空
	biography NVARCHAR(MAX), -- 演員簡介
);

DROP TABLE IF EXISTS movie_actor;
-- 電影與演員對應表：記錄每部電影的演員陣容
CREATE TABLE movie_actor (
    movie_id INT NOT NULL, -- 關聯的電影ID
    actor_id INT NOT NULL, -- 關聯的演員ID
	character NVARCHAR(100), -- 飾演角色名
	order_num INT -- 出場順序
    PRIMARY KEY (movie_id, actor_id) -- 複合主鍵，確保一部電影不能重複關聯同一位演員
);

DROP TABLE IF EXISTS tmdb_director;
-- TMDB 導演表：儲存從 TMDB 導入的導演資訊
CREATE TABLE tmdb_director (
    tmdb_director_id INT PRIMARY KEY, -- TMDB 導演的唯一識別碼 (本地產生ID)
    name NVARCHAR(100) NOT NULL -- 導演姓名
);

DROP TABLE IF EXISTS tmdb_movie_director;
-- TMDB 電影與導演對應表：記錄 TMDB 電影與 TMDB 導演的關係
CREATE TABLE tmdb_movie_director (
    tmdb_movie_id INT NOT NULL, -- 關聯的 TMDB 電影ID
    tmdb_director_id INT NOT NULL, -- 關聯的 TMDB 導演ID
    PRIMARY KEY (tmdb_movie_id, tmdb_director_id) -- 複合主鍵
);

DROP TABLE IF EXISTS tmdb_genre;
-- TMDB 類型表：儲存從 TMDB 導入的電影類型資訊
CREATE TABLE tmdb_genre (
    tmdb_genre_id INT PRIMARY KEY, -- TMDB 類型的原始ID，作為主鍵
    name NVARCHAR(100) NOT NULL -- 類型名稱
);

DROP TABLE IF EXISTS tmdb_movie_genre;
-- TMDB 電影與類型對應表：記錄 TMDB 電影與 TMDB 類型的關係
CREATE TABLE tmdb_movie_genre (
    tmdb_movie_id INT NOT NULL, -- 關聯的 TMDB 電影ID
    tmdb_genre_id INT NOT NULL, -- 關聯的 TMDB 類型ID
    PRIMARY KEY (tmdb_movie_id, tmdb_genre_id) -- 複合主鍵
);

DROP TABLE IF EXISTS tmdb_actor;
-- TMDB 演員表：儲存從 TMDB 導入的演員資訊
CREATE TABLE tmdb_actor (
    tmdb_actor_id INT PRIMARY KEY, -- TMDB 演員的唯一識別碼 (本地產生ID)
    name NVARCHAR(100) NOT NULL, -- 演員姓名
);

DROP TABLE IF EXISTS tmdb_movie_actor;
-- TMDB 電影與演員對應表：記錄 TMDB 電影與 TMDB 演員的關係
CREATE TABLE tmdb_movie_actor (
    tmdb_movie_id INT NOT NULL, -- 關聯的 TMDB 電影ID
    tmdb_actor_id INT NOT NULL, -- 關聯的 TMDB 演員ID
	character NVARCHAR(100), -- 飾演角色名
	order_num INT
    PRIMARY KEY (tmdb_movie_id, tmdb_actor_id) -- 複合主鍵
);

-- ==============================
-- 📦 商品相關表格
-- ==============================

-- 商品分類表格：定義商品的分類
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    category_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 商品類別的唯一識別碼
    category_name NVARCHAR(50) NOT NULL -- 類別名稱，不可為空
);

-- 商品表格：儲存商品的基本資訊
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    product_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 商品的唯一識別碼
    product_name NVARCHAR(100) NOT NULL, -- 商品名稱，不可為空
    category_id INT NOT NULL, -- 關聯的商品類別ID
    price INT NOT NULL, -- 商品價格，不可為空
    description NVARCHAR(MAX), -- 商品描述
    image_url NVARCHAR(MAX), -- 商品圖片的url地址
    stock INT NOT NULL, -- 商品庫存量，不可為空
	is_available BIT NOT NULL, -- 商品狀態 (0: 不可購買, 1: 可購買)
    creation_time DATETIME NOT NULL -- 商品創建時間
);

-- 1. 創建 Cart (購物車主表)
DROP TABLE IF EXISTS cart;
CREATE TABLE cart (
    cart_id INT IDENTITY(1,1) PRIMARY KEY, -- 購物車ID，自動遞增
    member_id INT NOT NULL,                  -- 會員ID，不能為空

    -- 如果支援未登入購物車，可以添加此欄位，並允許為 NULL
    -- session_id NVARCHAR(255),             -- 未登入使用者的 Session ID

    --status NVARCHAR(20) NOT NULL,            -- 購物車狀態：'ACTIVE', 'CHECKED_OUT', 'ABANDONED' 等
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(), -- 購物車建立時間
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()  -- 購物車最後更新時間
);

-- 2. 創建 CartItem (購物車明細表)
DROP TABLE IF EXISTS cart_item;
CREATE TABLE cart_item (
    cart_item_id INT IDENTITY(1,1) PRIMARY KEY, -- 購物車項目ID，自動遞增
    cart_id INT NOT NULL,                     -- 關聯到 Cart 表的 cart_id，但沒有資料庫層級的外鍵約束
    product_id INT NOT NULL,                  -- 商品ID

    quantity INT NOT NULL,                    -- 購買數量
    product_price INT NOT NULL, -- 商品加入購物車時的價格

    --status NVARCHAR(20),                      -- 購物車項目狀態，可為空
    --added_time DATETIME2 NOT NULL DEFAULT GETDATE() -- 商品加入購物車項目的時間
);

--已刪除
-- 購物車項目表格：記錄會員購物車中的商品
--CREATE TABLE cart_item (
 --   cart_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 購物車項目的唯一識別碼
 --   member_id INT NOT NULL, -- 關聯的會員ID
 --   product_id INT NOT NULL, -- 關聯的商品ID
  --  quantity INT NOT NULL, -- 購物車中此商品的數量
   -- status NVARCHAR(20), -- 購物車項目狀態 (例如 '活動中', '已結帳')
--    added_time DATETIME NOT NULL DEFAULT GETDATE() -- 商品加入購物車的時間，預設為當前時間
--);

-- 商品細節表格：記錄商品的各種規格或選項
DROP TABLE IF EXISTS product_detail;
CREATE TABLE product_detail (
    product_detail_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 商品細節的唯一識別碼
    product_order_detail INT NOT NULL, -- 關聯的商品ID
    size NVARCHAR(50), -- 尺寸選項 (例如 'S', 'M', 'L')
    color NVARCHAR(50), -- 顏色選項 (例如 '紅', '黑')
    ice_level NVARCHAR(50), -- 冰量選項 (例如 '少冰', '正常冰')
    sweetness_level DECIMAL(5,2), -- 甜度選項 (例如 '半糖', '全糖' 或數字百分比)
	topping NVARCHAR(50) --配料
);

-- 商品訂單表格：儲存商品訂單的主體資訊
DROP TABLE IF EXISTS product_order;
CREATE TABLE product_order (
    order_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 訂單的唯一識別碼
    member_id INT NOT NULL, -- 關聯的會員ID
    order_time DATETIME NOT NULL, -- 訂單成立時間
    order_amount INT NOT NULL, -- 訂單原始總金額 (未折扣前)
	order_number nvarchar(50), --訂單編號
    discount_amount INT NOT NULL, -- 訂單折扣金額
    final_payment_amount INT NOT NULL, -- 最終支付金額 (扣除折扣後)
    coupon_id INT, -- 使用的優惠券ID (若未使用則為 NULL)
    payment_method NVARCHAR(20), -- 支付方式 (例如 '信用卡', 'LINE Pay')
    order_status NVARCHAR(20),-- 訂單狀態 (例如 '待付款', '已完成', '已取消')
          --shipping_address NVARCHAR(50),  --寄送地址
        --shipping_method NVARCHAR(20),-- 寄送方式 超商 or 宅配
        customer_email NVARCHAR(255)--訂單Email
);

-- 商品訂單明細表格：記錄每筆商品訂單中的具體商品項目
DROP TABLE IF EXISTS product_order_detail;
CREATE TABLE product_order_detail (
    detail_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 訂單明細的唯一識別碼
    order_id INT NOT NULL, -- 關聯的訂單ID
    product_id INT NOT NULL, -- 關聯的商品ID
    quantity INT NOT NULL, -- 此商品購買數量
    unit_price INT NOT NULL, -- 購買時此商品的單價
	subtotal INT NOT NULL, --某商品的總額小計
	extra_price INT,
	product_name NVARCHAR(50)  -- 根據條件需要加的金額，例如加大+5元
	--order_time DATETIME NOT NULL DEFAULT GETDATE() -- 訂單下訂時間，預設為當前時間
);

--儲存商品額外金額規則的表格
DROP TABLE IF EXISTS product_optional_price;
CREATE TABLE product_optional_price(
	option_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL,
	product_id INT NOT NULL, --不用建立關聯，單純查詢用
	size NVARCHAR(50),   -- 飲料類別的大小
	topping NVARCHAR(50), --配料
	extra_price INT NOT NULL -- 各種組合的額外金額,例如商品id=1,size='S'額外金額是10，會有多個組合用於查詢
);

-- ==============================
-- 🎫 票務與場次相關表格
-- ==============================

-- 影廳表格：定義電影院的各個影廳
DROP TABLE IF EXISTS theater;
CREATE TABLE theater (
    theater_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 影廳的唯一識別碼
	theater_type_id INT NOT NULL, --影廳類型(imax、一般)
    theater_name NVARCHAR(50) NOT NULL, -- 影廳名稱 (例如 'A廳', 'IMAX廳')
    total_seats INT, -- 影廳總座位數
	theater_photo VARBINARY(MAX) -- 影廳預覽圖(供後台維護顯示使用)
);

DROP TABLE IF EXISTS theater_type;
-- 影廳類型：定義影廳的類型(影響票價計算)
CREATE TABLE theater_type (
	theater_type_id INT identity(1,1) NOT NULL, --影廳類型ID(imax、一般)
	theater_type_name NVARCHAR(50) NOT NULL, --影廳類型名稱
	description nvarchar(max) -- 描述
);

-- 票券定價規則
DROP TABLE IF EXISTS ticket_price_rule;
CREATE TABLE ticket_price_rule (
	theater_type_id INT NOT NULL, -- 影廳類型ID
	ticket_type_id INT NOT NULL, -- 票種ID
	price INT NOT NULL, -- 定價
	valid_s_date DATETIME NOT NULL, -- 生效起日
	valid_e_date DATETIME , -- 生效迄日
	description NVARCHAR(max) , -- 說明描述
	PRIMARY KEY (theater_type_id, ticket_type_id)
);

-- 票種表格：定義不同票券的類型、規則與基礎折扣
DROP TABLE IF EXISTS ticket_type;
CREATE TABLE ticket_type (
    ticket_type_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 票種的唯一識別碼
    ticket_type_name NVARCHAR(20) NOT NULL, -- 票種名稱 (例如 '全票', '學生票')
    description NVARCHAR(MAX), -- 票種的詳細描述或使用限制
    is_enabled BIT NOT NULL, -- 票種是否啟用 (0: 禁用, 1: 啟用)

    -- [新增] 折扣類型: 'PERCENTAGE' (百分比折扣), 'FIXED' (固定金額折抵)。可為 NULL 代表此為基準價(如全票)
    discount_type NVARCHAR(20),

    -- [新增] 折扣值: 若為百分比則存小數(如0.5代表5折)；若為固定金額則存折抵數(如-30代表便宜30元)。
    -- 使用 DECIMAL 型別來精確計算，避免浮點數誤差。
    discount_value DECIMAL(10, 2) NOT NULL DEFAULT 0
);

-- 座位表格：定義每個影廳的具體座位資訊
DROP TABLE IF EXISTS seat;
CREATE TABLE seat (
    seat_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 座位的唯一識別碼
    theater_id INT NOT NULL, -- 關聯的影廳ID
    seat_type NVARCHAR(10) NOT NULL, -- 座位類型 (例如 '一般座', '情侶座', 'VIP')
    row_number NVARCHAR(20) NOT NULL, -- 座位排數 (例如 'A', 'B')
    column_number INT NOT NULL, -- 座位列數 (例如 1, 2, 3)
    seat_number AS (row_number + CAST(column_number AS NVARCHAR(10))) PERSISTED, -- 計算列：自動組合排數與列數形成座位號 (例如 'A1', 'B2')
    is_active BIT NOT NULL -- 座位是否可用 (0: 不可用/損壞, 1: 可用)
);

-- 場次表格：定義電影的播放場次
DROP TABLE IF EXISTS movie_session;
CREATE TABLE movie_session (
    session_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 場次的唯一識別碼
    theater_id INT NOT NULL, -- 關聯的影廳ID
    movie_id INT NOT NULL, -- 關聯的電影ID
    start_time DATETIME NOT NULL, -- 場次開始時間
    end_time DATETIME NOT NULL -- 場次結束時間
);

CREATE INDEX idx_moviesession_movie_id ON movie_session(movie_id);
CREATE INDEX idx_moviesession_theater_id ON movie_session(theater_id);

-- 場次座位狀態表
DROP TABLE IF EXISTS session_seat;
CREATE TABLE session_seat(
	session_seat_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 場次座位唯一識別
	session_id INT NOT NULL, -- 場次id
	seat_id INT NOT NULL, -- 座位id
	status nvarchar(50) not null, -- 可用、已預約、已售出
	reserved_date datetime, -- 預訂時間
	reserved_expired_date datetime, -- 已預約到期日期時間
	ticket_order_detail_id INT -- 票券訂單明細id
);

CREATE INDEX idx_sessionseat_session_id ON session_seat(session_id);
CREATE INDEX idx_sessionseat_seat_id ON session_seat(seat_id);

-- 票券訂單表格：儲存票券訂單的主體資訊
DROP TABLE IF EXISTS ticket_order;
CREATE TABLE ticket_order (
    ticket_order_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 票券訂單的唯一識別碼
    member_id INT NOT NULL, -- 關聯的會員ID
    total_amount INT NOT NULL, -- 訂單總金額
    total_ticket_amount INT NOT NULL, -- 總基礎票價
    total_discount INT, -- 總折扣金額
	coupon_id INT, -- 優惠券ID
    status NVARCHAR(10) NOT NULL, -- 訂單狀態 (例如 '待付款', '已完成', '已取消')
    created_time DATETIME NOT NULL, -- 訂單建立時間
    payment_time DATETIME, -- 訂單支付時間 (若未支付則為 NULL)

	payment_type NVARCHAR(50), -- 支付方式 (例如 'Credit Card', 'LINE Pay')
    payment_transaction_id NVARCHAR(255) -- 第三方金流平台返回的唯一交易ID
);

-- 票券訂單明細表格：記錄每筆票券訂單中的具體票券項目
DROP TABLE IF EXISTS ticket_order_detail;
CREATE TABLE ticket_order_detail (
    ticket_order_detail_id INT IDENTITY(1,1) PRIMARY KEY NOT NULL, -- 票券訂單明細的唯一識別碼
    ticket_order_id INT NOT NULL, -- 關聯的票券訂單ID
    session_seat_id INT NOT NULL, -- 關聯的場次座位ID
	ticket_type_id INT NOT NULL, -- 關聯的票種ID
    unit_price INT NOT NULL, -- 此票券的單價
	status NVARCHAR(20)
);