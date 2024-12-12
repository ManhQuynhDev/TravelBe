package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class PostSaveResponseDTO {
    private Integer ownerId;       
    private Integer postId;
    private Integer locationId;
    private String location;
    private String content;
    private String status;
    private String fullname;
    private String avatar;
    private String type;
    private String create_time;
    private List<String> hashtags;
    private List<String> mediaUrls;
}
