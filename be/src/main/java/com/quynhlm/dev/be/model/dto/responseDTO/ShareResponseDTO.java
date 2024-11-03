package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareResponseDTO {
    private int owner_id;
    private int post_id;
    private String content;
    private String media_url;
    private int location_id;
    private String hastag;
    private String status;
    private String type;
    private int share_by_user;
}
