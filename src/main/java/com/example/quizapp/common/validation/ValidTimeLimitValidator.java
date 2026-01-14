package com.example.quizapp.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTimeLimitValidator implements ConstraintValidator<ValidTimeLimit, Integer> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidTimeLimit constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Null is allowed (optional field)
        }

        return value >= min && value <= max;
    }
}