package com.flux.movieproject.model.dto.event;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class CouponDistributionRequest {

    @NotNull(message = "優惠券ID不能為空")
    private Integer couponId;

    @NotNull(message = "會員ID列表不能為空")
    private List<Integer> memberIds;

}