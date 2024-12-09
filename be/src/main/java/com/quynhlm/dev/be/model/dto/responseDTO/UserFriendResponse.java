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
    private Integer userId;
    private String fullname;
    private String avatarUrl;
    private String status;
    private String send_time;
}
