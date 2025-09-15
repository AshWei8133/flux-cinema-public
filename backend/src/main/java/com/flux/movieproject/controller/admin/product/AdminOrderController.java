package com.flux.movieproject.controller.admin.product;

import com.flux.movieproject.model.dto.product.order.CreateOrderDTO;
import com.flux.movieproject.model.dto.product.order.CreateOrderResponseDTO;
import com.flux.movieproject.model.dto.product.order.ProductOrderDTO;
import com.flux.movieproject.model.entity.product.Product;
import com.flux.movieproject.model.entity.product.ProductOrder;
import com.flux.movieproject.model.entity.product.ProductOrderDetail;
import com.flux.movieproject.service.product.ProductOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//SQL資料庫移除product_order_detail中的order_time語法

//ALTER TABLE [fluxdb].[dbo].[product_order_detail]
//DROP CONSTRAINT DF__product_o__order__02084FDA;

//ALTER TABLE [fluxdb].[dbo].[product_order_detail]
//DROP COLUMN order_time;

//ALTER TABLE product_order
//ALTER COLUMN shipping_address NVARCHAR(50) NULL;
//ALTER COLUMN shipping_method  NVARCHAR(20) NULL; 兩個欄位改為可空

//  ALTER TABLE cart_item
//DROP COLUMN status;


//DROP TABLE IF EXISTS cart_item;
//
//CREATE TABLE dbo.cart_item (
//        cart_item_id INT IDENTITY(1,1) PRIMARY KEY,
//cart_id INT NOT NULL,
//product_id INT NOT NULL,
//quantity INT NOT NULL,
//product_price INT NOT NULL,
//added_time DATETIME DEFAULT GETDATE()
//);
//CREATE TABLE cart (
//        cart_id INT IDENTITY(1,1) PRIMARY KEY,
//member_id INT NOT NULL,
//created_at DATETIME NOT NULL DEFAULT GETDATE(),
//updated_at DATETIME NOT NULL DEFAULT GETDATE()
//);


//
//ALTER TABLE dbo.cart
//DROP COLUMN status;


//  ALTER TABLE cart_item
//DROP CONSTRAINT DF__cart_item__added__01142BA1;
//  ALTER TABLE dbo.cart_item
//DROP COLUMN added_time;

//ALTER TABLE [fluxdb].[dbo].[product_order]
//ADD order_number NVARCHAR(50) NULL;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private ProductOrderService productOrderService;

    public AdminOrderController(ProductOrderService productOrderService) {
        this.productOrderService = productOrderService;
    }

    @GetMapping
    public ResponseEntity<List<ProductOrderDTO>> getAllOrders() {
        List<ProductOrderDTO> orders = productOrderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponseDTO> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        CreateOrderResponseDTO responseDTO = productOrderService.createOrder(createOrderDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ProductOrderDTO> updateOrderStatus(@PathVariable Integer orderId,
                                                             @RequestBody ProductOrderService.UpdateOrderStatusDTO newstatus) {
        ProductOrderDTO updatedOrder = productOrderService.updateOrderForAdmin(orderId, newstatus);
        return ResponseEntity.ok(updatedOrder);
    }
}

