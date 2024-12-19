package com.quynhlm.dev.be.model.dto.requestDTO;

import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class ChangeFullnameDTO {
    @Length(min= 8 , message = "Full name is too short")
    private String fullname;
}
