package com.quynhlm.dev.be.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = AccountLockedValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccountLockedType {
    
    String message() default "Invalid status user type. Please try again !";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
