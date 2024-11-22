package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class MemberPlanResponse {
    private Integer userId;
    private Integer planId;
    private Integer memberId;
    private String fullname;
    private String avatar_url;
    private String role;
    private String join_time;
}
