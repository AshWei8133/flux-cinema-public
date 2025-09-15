package com.flux.movieproject.model.dto.product.order;

import com.flux.movieproject.model.entity.member.Member;

import java.time.Instant;
import java.util.List;

    public record ProductOrderDTO(Integer orderId,
                                  Instant orderTime,
                                  Integer orderAmount,
                                  Integer discountAmount,
                                  Integer finalPaymentAmount,
                                  Integer couponId,
                                  String customerEmail,
                                  String paymentMethod,
                                  String orderStatus,
                                  String orderNumber,
                                  ProductOrderMemberDTO member,
                                  List<ProductOrderDetailDTO> orderDetails ) {

    }




