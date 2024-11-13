package com.quynhlm.dev.be.core.validation;

import java.util.Arrays;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AccountLockedValidator implements ConstraintValidator<AccountLockedType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        String[] statusUser = { "LOOK", "OPEN" };

        if (value == null) {
            return false;
        }

        Boolean isCheck = Arrays.asList(statusUser).contains(value);
        System.err.println("is check : " + isCheck);
        return isCheck;
    }
}
