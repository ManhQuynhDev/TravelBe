package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CommentResponseDTO {
    private Integer commentId;
    private Integer ownerId;
    private String fullname;
    private String avatar;
    private String content;
    private String mediaUrl;
    private String mediaType;
    private Integer postId;
    private Integer is_reply;
    private Integer reply_to_id;
    private String create_time;
    private Integer reaction_count;
    private String user_reaction_type;
    private Boolean isAuthor;
    private List<CommentResponseDTO> replys;
}
