package com.flux.movieproject.repository.product;

import com.flux.movieproject.model.entity.product.Cart;
import com.flux.movieproject.model.entity.product.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    Optional<CartItem> findByCartAndProductProductId(Cart userCart, Integer productId);

    List<CartItem> findByCart(Cart cart);

    @Transactional
    void deleteByCartAndProductProductId(Cart cart, Integer productId);

    @Transactional
    void deleteAllByCart(Cart cart);

    Optional<CartItem> findByCartAndCartItemId(Cart userCart, Integer cartItemid);
}
