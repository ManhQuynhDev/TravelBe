package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ReViewRequestDTO {
    private Integer user_id;
    private Integer location_id;
    private String content;
    private double star;
}
