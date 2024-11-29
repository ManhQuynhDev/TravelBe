package com.quynhlm.dev.be.model.dto.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class FriendStoryResponseDTO {
    private Integer userId;
    private String fullname;
    private String avatar;
    private List<StoryResponseDTO> storys;
}
