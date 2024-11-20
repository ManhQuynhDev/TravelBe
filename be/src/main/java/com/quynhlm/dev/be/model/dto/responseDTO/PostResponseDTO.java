package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

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
    private Integer locationId;
    private String adminName;
    private String avatarUrl;
    private String content;
    private String mediaUrl;
    private String hastag;
    private String status;
    private String type;
    private Integer isShare;
    private String create_time;
    private Integer share_by_user;
    private Integer reaction_count;
    private Integer comment_count;
    private Integer share_count;
    private Integer isTag;
    
    private List<UserTagPostResponse> tags;
}
