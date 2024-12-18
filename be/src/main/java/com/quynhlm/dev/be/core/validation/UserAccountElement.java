package com.quynhlm.dev.be.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = UserAccountValidator.class)
@Target({ ElementType.TYPE , ElementType.FIELD , ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UserAccountElement {

    String field();

    String regex();

    String message() default "Field's format is invalid!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        UserAccountElement[] value();
    }
}
