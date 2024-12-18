package com.quynhlm.dev.be.model.dto.requestDTO;

import com.quynhlm.dev.be.core.AppConstant.UserAccountRegex;
import com.quynhlm.dev.be.core.validation.UserAccountElement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@UserAccountElement.List({
        @UserAccountElement(field = "phoneNumber", regex = UserAccountRegex.EMAIL, message = "PhoneNumber is not in correct format"),
})
public class UpdateProfileDTO {
    private String phoneNumber;
    private String dob;
    private String bio;
}
