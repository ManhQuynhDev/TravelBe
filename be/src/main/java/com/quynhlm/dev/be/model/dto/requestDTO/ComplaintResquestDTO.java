package com.quynhlm.dev.be.model.dto.requestDTO;

import com.quynhlm.dev.be.core.AppConstant.UserAccountRegex;
import com.quynhlm.dev.be.core.validation.UserAccountElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@UserAccountElement.List({
        @UserAccountElement(field = "email", regex = UserAccountRegex.EMAIL, message = "Email is not in correct format , please try again"),
})

public class ComplaintResquestDTO {
    private String email;
    private String type;
    private String reason;
}
