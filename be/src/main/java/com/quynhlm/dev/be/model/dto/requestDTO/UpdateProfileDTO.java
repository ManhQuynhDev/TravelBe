package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class UpdateProfileDTO {
    private String dob;
    private String bio;
    
}
