package com.quynhlm.dev.be.core.validation;

import com.quynhlm.dev.be.enums.LockUser;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StatusUserTypeValidator implements ConstraintValidator<ValidStatusUserType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        try {
            LockUser.valueOf(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
