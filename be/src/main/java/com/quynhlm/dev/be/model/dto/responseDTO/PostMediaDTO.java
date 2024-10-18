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
    private int post_id;
    private String content;
    private String status;
    private int location_id;
    private String hastag;
    private String media_url;
    private String type;
}
