package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequestDTO {
    private int user_id;
    private String name;
    private String status;
    private String bio;
}
