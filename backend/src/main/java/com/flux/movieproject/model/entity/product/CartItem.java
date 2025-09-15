package com.flux.movieproject.model.entity.product;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id", nullable = false)
    private Integer cartItemId;



    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "product_price", nullable = false)
    private Integer productPrice;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_CartItem_Product",
                    // 不設定級聯刪除，需要手動處理
                    foreignKeyDefinition = "FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE"))
    private Product product;

    @Override
    public String toString() {
        return "CartItem{" +
                "cartItemId=" + cartItemId +
                ", quantity=" + quantity +
                ", productPrice=" + productPrice +
                ", cartId=" + (cart != null ? cart.getCartId() : null) +
                ", productId=" + (product != null ? product.getProductId() : null) +
                '}';
    }
}

