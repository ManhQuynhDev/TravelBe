package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDTO {
    private Integer ownerId;       // Thay đổi thành Integer
    private Integer postId;        // Thay đổi thành Integer
    private String content;
    private String mediaUrl;
    private Integer locationId;    // Thay đổi thành Integer
    private String hastag;
    private String status;
    private String type;
    private Integer isShare;    
    private String create_time;
    private Integer shareByUser;
}
