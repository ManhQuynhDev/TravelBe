package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

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
    private Integer locationId;
    private String location;
    private String ownerName;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String avatarUrl;
    private String postContent;
    private String status;
    private String create_time;
    private Integer isShare;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer user_share_id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String user_share_name;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String user_share_avatar; 
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String shareContent;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String share_time;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String share_status;
    private Integer reaction_count;
    private Integer comment_count;
    private Integer share_count;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String user_reaction_type;
    private double averageRating;
    private List<ReactionCountDTO> reactionStatistics;
    private List<String> hashtags;
    private List<MediaResponseDTO> mediaUrls;
    private List<UserTagPostResponse> tags;
}
