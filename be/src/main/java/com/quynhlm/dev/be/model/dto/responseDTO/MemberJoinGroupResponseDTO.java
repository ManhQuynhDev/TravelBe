package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberJoinGroupResponseDTO {
    private Integer userId;
    private Integer memberId;
    private Integer groupId;
    private String group_name;
    private String admin_name;
    private String cover_photo;
    private String bio;
    private String status;
    private String role;
    private String request_time;
    private String join_time;
    
    private List<MemberResponseDTO> userJoined;
}
