package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class RegisterDTO {
    private String email;
    private String password;
    private String fullname;
    private String location;
}
