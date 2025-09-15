package com.flux.movieproject.repository.product;

import com.flux.movieproject.model.entity.product.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByMemberId(Integer memberId);
}
