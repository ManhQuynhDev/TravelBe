package com.quynhlm.dev.be.model.dto.responseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OTPResponse {
    private String otp;
    private LocalDateTime expiryTime;
}
