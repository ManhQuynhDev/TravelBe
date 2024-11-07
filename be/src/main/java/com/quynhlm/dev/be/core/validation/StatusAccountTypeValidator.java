package com.quynhlm.dev.be.core.validation;

import java.util.Arrays;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StatusAccountTypeValidator implements ConstraintValidator<ValidStatusAccountType, String> {

    private static final List<String> ACCOUNT_STATUSES = Arrays.asList("OPEN", "LOOK");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return ACCOUNT_STATUSES.contains(value);
    }
}
