package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UserMessageResponseDTO {
    private Integer id;
    private Integer sender_id;
    private Integer receiver_id;
    private String content;
    private String fullname;
    private String avatarUrl;
    private String mediaUrl;
    private Boolean status;
    private String reaction;
    private String send_time;
}
