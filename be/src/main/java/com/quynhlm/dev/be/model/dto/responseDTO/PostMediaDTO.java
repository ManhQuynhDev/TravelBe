package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostMediaDTO {
    private Integer ownerId;       
    private Integer postId;
    private Integer locationId;
    private String content;
    private String status;
    private String fullname;
    private String avatar;
    private String mediaUrl;
    private String type;
    private String create_time;
    private Integer reaction_count;
    private Integer comment_count;
    private Integer share_count;
    private String user_reaction_type;
}
