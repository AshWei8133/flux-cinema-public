package com.flux.movieproject.service.product;

import com.flux.movieproject.model.dto.product.cart.CartDTO;
import com.flux.movieproject.model.dto.product.cart.CartItemDTO;
import com.flux.movieproject.model.entity.product.Cart;
import com.flux.movieproject.model.entity.product.CartItem;
import com.flux.movieproject.model.entity.product.CustomUserDetails;
import com.flux.movieproject.model.entity.product.Product;
import com.flux.movieproject.repository.member.MemberRepository;
import com.flux.movieproject.repository.product.CartItemRepository;
import com.flux.movieproject.repository.product.CartRepository;
import com.flux.movieproject.repository.product.ProductRepository;
import com.github.houbb.heaven.annotation.env.Prod;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CartItemRepository cartItemRepository;
    private MemberRepository memberRepository;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       CartItemRepository cartItemRepository, MemberRepository memberRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public CartItem addToCart(Integer memberId, Integer productId, Integer quantity) {

        if (memberId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        Cart userCart = cartRepository.findByMemberId(memberId).orElseGet(() -> createCart(memberId));

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductProductId(userCart, productId);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(userCart);
            cartItem.setProduct(product);
            cartItem.setProductPrice(product.getPrice());
            cartItem.setQuantity(quantity);
        }

        return cartItemRepository.save(cartItem);
    }

    private Cart createCart(Integer memberId) {
        Cart newCart = new Cart();
        newCart.setCreatedAt(Instant.now());
        newCart.setUpdatedAt(Instant.now());
        newCart.setMemberId(memberId);
        return cartRepository.save(newCart);
    }


    private Integer getCurrentUserId(HttpServletRequest request) {
        Object memberIdObj = request.getAttribute("memberId");
        if (memberIdObj instanceof Integer) {
            return (Integer) memberIdObj;
        }
        return null; // 未登入
    }

    @Transactional
    public CartItem minusToCart(Integer productId, Integer quantity) {
        Integer currentUserId = getCurrentUserId(request);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        Cart userCart = cartRepository.findByMemberId(currentUserId).orElseGet(() -> createCart(currentUserId));

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductProductId(userCart,productId);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            if(cartItem.getQuantity() <= quantity){
                cartItemRepository.deleteByCartAndProductProductId(userCart,productId);
                return null;
            }else{
                cartItem.setQuantity(cartItem.getQuantity() - quantity);
                return cartItemRepository.save(cartItem);
            }
        }else{
            throw new IllegalArgumentException("Product with id " + productId + " is not in the cart.");
        }
    }

    @Transactional
    public void removeProductFromCart(Integer cartItemid) {
        Integer currentUserId = getCurrentUserId(request);
        Cart userCart = cartRepository.findByMemberId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("購物車不存在"));

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndCartItemId(userCart, cartItemid);
        if (existingItem.isPresent()) {
            cartItemRepository.delete(existingItem.get());
        } else {

            throw new CartItemNotFoundException("產品ID " + cartItemid + " 不存在於購物車中。");
        }
    }

    @Transactional
    public void clearCart() {
        Integer currentUserId = getCurrentUserId(request);
        Cart userCart = cartRepository.findByMemberId(currentUserId)
                .orElse(null); // 如果購物車不存在，則不需要做任何事

        if (userCart != null) {
            cartItemRepository.deleteAllByCart(userCart);
        }
    }

    @Transactional
    public CartItem updateProductQuantity(Integer productId, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("數量不能為負數。");
        }
        Integer currentUserId = getCurrentUserId(request);
        Cart userCart = cartRepository.findByMemberId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("購物車不存在"));

        CartItem cartItem = cartItemRepository.findByCartAndProductProductId(userCart, productId)
                .orElseThrow(() -> new EntityNotFoundException("產品ID " + productId + " 不存在於購物車中。"));

        if (newQuantity == 0) {

            cartItemRepository.delete(cartItem);
            return null;
        } else {
            cartItem.setQuantity(newQuantity);
            return cartItemRepository.save(cartItem);
        }
    }


    @Transactional
    public CartItem updateProductQuantityByUserIdAndProductId(Integer cartItemId, Integer newQuantity) {

        if (newQuantity <= 0) {
            System.out.println("錯誤：數量不能小於或等於零，輸入數量 = " + newQuantity);
            throw new IllegalArgumentException("數量必須大於零");
        }

        // 取得當前使用者 ID
        Integer userId = getCurrentUserId(request);
        System.out.println("當前使用者 ID = " + userId);

        // 查詢使用者購物車
        Cart userCart = cartRepository.findByMemberId(userId)
                .orElseThrow(() -> {
                    return new EntityNotFoundException("找不到使用者購物車，使用者 ID: " + userId);
                });

        // 查詢購物車裡的指定商品
        Optional<CartItem> existingItem = cartItemRepository.findById(cartItemId);

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            Product product = existingItem.get().getProduct();

            if(product.getStock() < newQuantity){
                throw new InsufficientStockException(
                        "庫存不足" + "，可用庫存=" + product.getStock()
                );

            }
            //TODO 更新的邏輯

            // 更新數量

            cartItem.setQuantity(newQuantity);
            CartItem savedItem = cartItemRepository.save(cartItem);

            System.out.println("🔹 更新後 CartItem = " + savedItem);
            return savedItem;

        } else {
            System.out.println("❌ 購物車中找不到商品，userId = " + userId + ", productId = " + cartItemId);
            throw new IllegalArgumentException("購物車中沒有此商品，商品 ID: " + cartItemId);
        }
    }


    public CartDTO getCartDetails() {
        Integer currentUserId = getCurrentUserId(request);
        Cart userCart = cartRepository.findByMemberId(currentUserId)
                .orElseGet(() -> createCart(currentUserId));

        List<CartItemDTO> items = cartItemRepository.findByCart(userCart)
                .stream()
                .map(item -> new CartItemDTO(
                        item.getCartItemId(),
                        item.getProduct().getProductId(),
                        item.getProduct().getProductName(),
                        item.getQuantity(),
                        item.getProductPrice(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getIsAvailable(),
                        item.getProduct().getStock()

                ))
                .toList();

        return new CartDTO(
                userCart.getCartId(),
                userCart.getMemberId(),
                userCart.getCreatedAt(),
                userCart.getUpdatedAt(),
                items
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class CartItemNotFoundException extends RuntimeException {
        public CartItemNotFoundException(String message) {
            super(message);
        }
    }

    public class InsufficientStockException extends RuntimeException {
        public InsufficientStockException(String message) {
            super(message);
        }
    }


}
