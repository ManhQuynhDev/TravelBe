package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoryRequestDTO {
    private String content;
    private int user_id;
    private int location_id;
    private String hastag;
    private String status;
}
