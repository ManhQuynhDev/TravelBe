package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponseDTO {
    private Integer planId;
    private Integer adminId;
    private String plan_name;
    private String admin_name;
    private String description;
    private String status;
    private String create_time;
    private Integer member_count;
    private List<MemberPlanResponse> userJoined;
}
