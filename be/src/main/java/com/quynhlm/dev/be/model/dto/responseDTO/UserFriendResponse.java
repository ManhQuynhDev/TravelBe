package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UserFriendResponse {
    private Integer id;
    private Integer userSendId;
    private String userSendName;
    private String userSendAvatar;
    private Integer userReceiedId;
    private String status;
    private String send_time;
}
