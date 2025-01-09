package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequestDTO {
    private Integer postId;
    private Integer commentId;
    private int userId;
    private String reason;
    private String violationType;
}
