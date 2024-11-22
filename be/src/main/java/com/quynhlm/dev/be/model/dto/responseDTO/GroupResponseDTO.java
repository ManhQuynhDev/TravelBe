package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDTO {
    private Integer groupId;
    private Integer adminId;
    private String group_name;
    private String admin_name;
    private String cover_photo;
    private String status;
    private String bio;
    private String create_time;
    private Integer member_count;
    private List<MemberResponseDTO> userJoined;
}
