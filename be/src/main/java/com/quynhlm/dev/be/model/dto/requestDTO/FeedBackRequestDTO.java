package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class FeedBackRequestDTO {
    private String content;
    private Integer postId; 
    private Integer userId;
    private Integer replyToId;
}