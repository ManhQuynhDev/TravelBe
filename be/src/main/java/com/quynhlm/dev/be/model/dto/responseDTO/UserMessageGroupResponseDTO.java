package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UserMessageGroupResponseDTO {
    private Integer id;
    private Integer userSendId;
    private Integer groupId;
    private String content;
    private String mediaUrl;
    private String fullname;
    private String avatarUrl;
    private Boolean status;
    private String send_time;
}
