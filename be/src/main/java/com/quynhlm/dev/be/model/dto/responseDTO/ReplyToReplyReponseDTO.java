package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class ReplyToReplyReponseDTO {
    private Integer id;
    private Integer replyId;
    private Integer ownerId;
    private String fullname;
    private String avatar;
    private String content;
    private String create_time;
    private Integer reaction_count;
    private Boolean isAuthor;
}
