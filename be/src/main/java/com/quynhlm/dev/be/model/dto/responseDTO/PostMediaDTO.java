package com.quynhlm.dev.be.model.dto.responseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PostMediaDTO {
    private int owner_id;
    private int post_id;
    private String content;
    private String media_url;
    private int location_id;    
    private String status;
    private String type;
    private String create_time;
}
