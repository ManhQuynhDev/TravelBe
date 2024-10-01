package com.quynhlm.dev.be.core.validation;

import org.springframework.beans.BeanWrapperImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class FieldMatchingValidator implements ConstraintValidator<FieldMatching, Object> {

    private String field;
    private String fieldMatch;

    @Override
    public void initialize(FieldMatching matching) {
        this.field = matching.field();
        this.fieldMatch = matching.fieldMatch();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        Object fieldValued = new BeanWrapperImpl(value).getPropertyValue(field);
        Object fieldMatchValue = new BeanWrapperImpl(value).getPropertyValue(fieldMatch);

        return (fieldValued != null) ? fieldValued.equals(fieldMatchValue) : fieldMatchValue == null;
    }

}
