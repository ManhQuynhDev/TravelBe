package com.quynhlm.dev.be.model.dto.requestDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDTO {
    private String content;
    private String status;
    private Integer user_id;    
    private String location;
    private List<String> hashtags;
}
    