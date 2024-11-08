package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserReactionDTO {
    private Integer ownerId;
    private String fullname;
    private String avatar;
    private String type;
    private String create_time;
}
