package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponseDTO {
    private Integer storyId;
    private Integer ownerId;
    private Integer locationId;               
    private String content;
    private String status;
    private String fullname;
    private String avatar;
    private String musicUrl;
    private String mediaUrl;
    private String create_time;
    private Integer reaction_count;
    private String mediaType;
}
