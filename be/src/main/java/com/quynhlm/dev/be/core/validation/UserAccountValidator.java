package com.quynhlm.dev.be.core.validation;

import org.springframework.beans.BeanWrapperImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserAccountValidator implements ConstraintValidator<UserAccountElement, Object> {

    private String field;
    private String regex;

    @Override
    public void initialize(UserAccountElement element) {
        this.field = element.field();
        this.regex = element.regex();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String fieldValued = (String) new BeanWrapperImpl(value).getPropertyValue(field);
        if (fieldValued == null) {
            return false;
        }
        return fieldValued.matches(regex);
    }
}
