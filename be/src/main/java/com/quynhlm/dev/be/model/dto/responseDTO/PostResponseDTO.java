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
    private Integer postId;
    private Integer ownerId;
    private Integer shareId;
    private Integer locationId;
    private String location;
    private String ownerName;
    private String avatarUrl;
    private String postContent;
    private String shareContent;
    private String status;
    private String type;
    private Integer isShare;
    private String create_time;
    private Integer share_by_user;
    private String share_by_name;
    private String share_by_avatar; 
    private Integer reaction_count;
    private Integer comment_count;
    private Integer share_count;
    private Integer isTag;
    private String user_reaction_type;
    private double averageRating;
    
    private List<String> hashtags;
    private List<String> mediaUrls;
    private List<UserTagPostResponse> tags;
}
