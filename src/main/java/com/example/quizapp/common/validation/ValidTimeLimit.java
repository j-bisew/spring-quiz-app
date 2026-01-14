package com.example.quizapp.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTimeLimitValidator.class)
@Documented
public @interface ValidTimeLimit {

    String message() default "Time limit must be between 1 and 300 minutes";

    int min() default 1;

    int max() default 300;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}