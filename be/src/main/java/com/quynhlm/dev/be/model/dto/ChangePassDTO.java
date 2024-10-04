package com.quynhlm.dev.be.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class ChangePassDTO {
    private String email;
    private String password;
}
