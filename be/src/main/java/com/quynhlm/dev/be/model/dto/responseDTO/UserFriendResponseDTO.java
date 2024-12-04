package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UserFriendResponseDTO {
    private Integer userId;
    private String fullname;
    private String avatarUrl;
    private boolean isJoiner;
}
