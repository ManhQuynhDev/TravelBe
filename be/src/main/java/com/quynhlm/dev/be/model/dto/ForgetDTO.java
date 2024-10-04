package com.quynhlm.dev.be.model.dto;

import com.quynhlm.dev.be.core.validation.StrongPassword;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class ForgetDTO {
    @StrongPassword(message = "Incorrect password format")
    private String password;
}