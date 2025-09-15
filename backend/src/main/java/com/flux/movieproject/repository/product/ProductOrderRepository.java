package com.flux.movieproject.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.flux.movieproject.model.entity.product.OrderStatus;
import com.flux.movieproject.model.entity.product.ProductOrder;

public interface ProductOrderRepository extends JpaRepository<ProductOrder,Integer> {

    @EntityGraph(attributePaths = {"member", "productOrderDetails", "productOrderDetails.product"})
    List<ProductOrder> findByMemberMemberIdAndOrderStatus(Integer memberId, OrderStatus status);

    @EntityGraph(attributePaths = {"member", "productOrderDetails", "productOrderDetails.product"})
    Optional<ProductOrder> findById(Integer orderId);

    @EntityGraph(attributePaths = {"member", "productOrderDetails", "productOrderDetails.product"})
    List<ProductOrder> findAll();

    @EntityGraph(attributePaths = {"member", "productOrderDetails", "productOrderDetails.product"})
    List<ProductOrder> findAllByMemberMemberId(Integer memberId);

    @EntityGraph(attributePaths = {"member", "productOrderDetails", "productOrderDetails.product"})
    List<ProductOrder> findAllByOrderStatus(OrderStatus orderStatus);


}
