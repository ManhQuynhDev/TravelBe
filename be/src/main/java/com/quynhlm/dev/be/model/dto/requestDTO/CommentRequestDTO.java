package com.quynhlm.dev.be.model.dto.requestDTO;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class CommentRequestDTO {
    private String content;
    @Column(name = "post_id",nullable = true)
    private Integer postId;
    @Column(name = "share_id",nullable = true)
    private Integer shareId;
    private int userId;
    private String type;
}
