package com.quynhlm.dev.be.core.validation;

import com.quynhlm.dev.be.enums.ReactionType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReactionTypeValidator implements ConstraintValidator<ValidReactionType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        try {
            ReactionType.valueOf(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
