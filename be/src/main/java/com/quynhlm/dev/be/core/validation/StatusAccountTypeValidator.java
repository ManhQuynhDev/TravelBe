package com.quynhlm.dev.be.core.validation;

import java.util.Arrays;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StatusAccountTypeValidator implements ConstraintValidator<ValidStatusAccountType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String[] statusUser = { "ONLINE", "OFFLINE" };

        if (value == null) {
            return false;
        }

        return Arrays.asList(statusUser).contains(value);
    }
}
