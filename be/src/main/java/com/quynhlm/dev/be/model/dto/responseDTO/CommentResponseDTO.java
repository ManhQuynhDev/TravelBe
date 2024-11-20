package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)

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
    private Integer reply_count;
    private List<ReplyResponseDTO> replys;
}
