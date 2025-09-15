package com.flux.movieproject.controller.admin.product;

import com.flux.movieproject.model.dto.product.cart.CartDTO;
import com.flux.movieproject.model.entity.product.CartItem;
import com.flux.movieproject.service.product.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/public/cart")
public class UserCartController {

   private CartService cartService;

    @Autowired
    public UserCartController(CartService cartService) {
       this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDTO> getCartItems() {
        CartDTO cartDTO = cartService.getCartDetails();
        return ResponseEntity.ok(cartDTO);
    }


    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            HttpServletRequest request,
            @RequestParam Integer productId,
            @RequestParam Integer quantity) {

        // 從 Interceptor 放進 request attribute 的 memberId 取得會員 ID
        Integer memberId = (Integer) request.getAttribute("memberId");
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 呼叫 Service，把會員 ID、商品 ID 和數量傳進去
        CartItem updatedItem = cartService.addToCart(memberId, productId, quantity);

        System.out.println(request.getAttribute("memberId"));

        return ResponseEntity.ok(updatedItem);
    }


    @PutMapping("/update")
    public ResponseEntity<CartItem> updateCartItem(
            @RequestParam("cartItemId") Integer cartItemId,
            @RequestParam("newQuantity") Integer newQuantity) {

        CartItem updatedItem = cartService.updateProductQuantityByUserIdAndProductId(cartItemId, newQuantity);

        return ResponseEntity.ok(updatedItem);


    }



    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeCartItem(@RequestParam("cartItemId") Integer cartItemId) {
        cartService.removeProductFromCart(cartItemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/minus")
    public ResponseEntity<CartItem> minusFromCart(
            @RequestParam("productId") Integer productId,
            @RequestParam("quantity") Integer quantity) {

        CartItem updatedItem = cartService.minusToCart(productId, quantity);

        if (updatedItem == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(updatedItem);
        }
    }

}
