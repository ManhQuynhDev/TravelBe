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
    private Integer ownerId;       
    private Integer postId;        
    private String content;
    private String mediaUrl;
    private Integer locationId;
    private String hastag;
    private String status;
    private String type;
    private Integer isShare;    
    private String create_time;
    private Integer shareByUser;
    private Integer reaction_count;
    private Integer comment_count;
    private Integer share_count;
}
