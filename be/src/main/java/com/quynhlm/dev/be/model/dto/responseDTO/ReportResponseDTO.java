package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDTO {
    private Integer id;
    private Integer ownerId;
    private Integer postId;
    private Integer adminId;
    private String fullname;
    private String avatarUrl;
    private String contentPost;
    private String mediaUrl;
    private String mediaType;
    private String reason;
    private String violationType;
    private String status;
    private String create_time;
    private String action;
    private String responseTime;
}
