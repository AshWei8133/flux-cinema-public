package com.flux.movieproject.model.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 放到訂單摘要中的會員資料
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoForTicketDTO {
    private Integer memberId;
    private String username;
    private String email;
    private String phone;
}
