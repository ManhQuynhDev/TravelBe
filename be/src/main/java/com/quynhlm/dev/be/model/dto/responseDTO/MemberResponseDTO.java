package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDTO {
    private Integer userId;
    private Integer groupId;
    private Integer memberId;
    private String fullname;
    private String avatar_url;
    private String role;
    private String request_time;
    private String join_time;
}
