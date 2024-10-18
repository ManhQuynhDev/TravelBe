package com.quynhlm.dev.be.model.dto.requestDTO;

import com.quynhlm.dev.be.core.validation.StrongPassword;
import com.quynhlm.dev.be.core.validation.UserAccountElement;
import com.quynhlm.dev.be.core.AppConstant.UserAccountRegex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
@UserAccountElement.List({
        @UserAccountElement(field = "username", regex = UserAccountRegex.USERNAME, message = "username"),
})
public class LoginDTO {
    private String username;

    @StrongPassword(message = "password")
    private String password;
}
