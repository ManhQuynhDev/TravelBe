package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ReviewResponseDTO {
    private Integer review_id;
    private Integer user_id;
    private Integer location_id;
    private String location;
    private String fullname;
    private String avatarUrl;
    private String content;
    private String mediaUrl;
    private double start;
    private String create_time;
}