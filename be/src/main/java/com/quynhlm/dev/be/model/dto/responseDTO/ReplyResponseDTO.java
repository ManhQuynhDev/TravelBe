package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class ReplyResponseDTO {
    private Integer replyId;
    private Integer commentId;
    private Integer ownerId;
    private String fullname;
    private String avatar;
    private String content;
    private String create_time;
}
