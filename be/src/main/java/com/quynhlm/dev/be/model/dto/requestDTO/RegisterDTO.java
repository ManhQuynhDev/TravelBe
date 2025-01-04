package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Length;

import com.quynhlm.dev.be.core.AppConstant.UserAccountRegex;
import com.quynhlm.dev.be.core.validation.StrongPassword;
import com.quynhlm.dev.be.core.validation.UserAccountElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@UserAccountElement.List({
        @UserAccountElement(field = "email", regex = UserAccountRegex.EMAIL, message = "Email is not in correct format , please try again"),
})

public class RegisterDTO {
    private String email;
    @StrongPassword(message = "Incorrect password format . Please try other password")
    private String password;
    @Length(min = 8, message = "Name is less than 8 characters, please try again")
    private String fullname;
    private String location;
}
