package com.flux.movieproject.service.product;

public class CheckoutService {

    //Todo 處理出貨 結帳 退款
}


    //USE [fluxdb]
    //GO
    //SET IDENTITY_INSERT [dbo].[product] ON
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (29, N'咖啡', 5, 35, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972381/bfy3fnoux5onalwrfxjr.jpg', 50, 1, CAST(N'2025-09-04T07:53:01.580' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (30, N'可樂', 5, 30, N'', N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972462/oxqjgsfboeur4wo5uyab.jpg', 10, 1, CAST(N'2025-09-04T07:53:35.910' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (31, N'洋芋片', 4, 55, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972632/wx9vjraes3lix5guu0xs.jpg', 5, 1, CAST(N'2025-09-04T07:57:12.483' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (32, N'礦泉水', 5, 20, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972665/hjbmisg4vwaa46oqw9zg.jpg', 100, 1, CAST(N'2025-09-04T07:57:46.370' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (33, N'熱狗堡', 4, 65, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972691/bpvmndcjzzsqzaispd6n.jpg', 120, 1, CAST(N'2025-09-04T07:58:12.313' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (34, N'焗烤馬鈴薯', 4, 80, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972724/jdgumuixlxgqkwjpj7lk.jpg', 65, 1, CAST(N'2025-09-04T07:58:45.083' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (35, N'柳橙汁', 5, 40, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972798/cerdibc7cmmnbo3txhqn.jpg', 60, 1, CAST(N'2025-09-04T07:59:58.817' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (36, N'爆米花', 4, 50, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972827/rfp2qbbpgbqyufgojxxs.jpg', 99, 1, CAST(N'2025-09-04T08:00:27.570' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (37, N'巧克力爆米花', 4, 70, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972848/pcuhrjxefmjs4irhvht3.jpg', 45, 1, CAST(N'2025-09-04T08:00:49.073' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (38, N'薯條', 4, 45, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972872/dxsugtmyt6vucvvtzuvw.jpg', 67, 1, CAST(N'2025-09-04T08:01:13.313' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (39, N'泡芙', 4, 40, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972892/bq7h1m7tswmms3i3upve.jpg', 50, 1, CAST(N'2025-09-04T08:01:33.400' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (40, N'三明治', 4, 35, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972925/ycokweaetyndb6hyof4g.jpg', 80, 1, CAST(N'2025-09-04T08:02:05.867' AS DateTime))
    //GO
    //INSERT [dbo].[product] ([product_id], [product_name], [category_id], [price], [description], [image_url], [stock], [is_available], [creation_time]) VALUES (41, N'奶茶', 4, 35, NULL, N'http://res.cloudinary.com/dg98ebu8y/image/upload/v1756972970/rhjiedveg1y1dfcafudz.jpg', 50, 1, CAST(N'2025-09-04T08:02:52.537' AS DateTime))
    //GO
    //SET IDENTITY_INSERT [dbo].[product] OFF
    //GO
    //SET IDENTITY_INSERT [dbo].[product_category] ON
    //GO
    //INSERT [dbo].[product_category] ([category_id], [category_name]) VALUES (4, N'餐點')
    //GO
    //INSERT [dbo].[product_category] ([category_id], [category_name]) VALUES (5, N'飲料')
    //GO
    //SET IDENTITY_INSERT [dbo].[product_category] OFF
    //GO
