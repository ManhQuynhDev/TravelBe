package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoPostDTO {
    private Integer ownerId;       
    private Integer postId;
    private Integer locationId;
    private String location;
    private String content;
    private String status;
    private String fullname;
    private String avatar;
    private String video;
    private String create_time;
    private Integer reaction_count;
    private Integer comment_count;
    private Integer share_count;
    private String user_reaction_type;
    private List<String> hashtags;
}
