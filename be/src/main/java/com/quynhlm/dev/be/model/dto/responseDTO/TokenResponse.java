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

public class TokenResponse {
    private Boolean success;
    private String message;
    private String token;
}
