package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ConfirmEmailDTO {
    private String email;   
}
