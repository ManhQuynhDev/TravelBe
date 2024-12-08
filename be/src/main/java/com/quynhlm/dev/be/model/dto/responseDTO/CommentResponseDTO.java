package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class CommentResponseDTO {
    private Integer commentId;
    private Integer ownerId;
    private String fullname;
    private String avatar;
    private String content;
    private Integer postId;
    private Integer shareId; 
    private String create_time;
    private Integer reaction_count;
    private String user_reaction_type;
    private Boolean isAuthor;
    private List<ReplyResponseDTO> replys;
}
