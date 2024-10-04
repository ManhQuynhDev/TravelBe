package com.quynhlm.dev.be.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class VerifyDTO {
    private String email;
    private String otp;
}
