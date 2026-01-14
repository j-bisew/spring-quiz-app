package com.example.quizapp.common.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidJsonValidator implements ConstraintValidator<ValidJson, String> {

    private final ObjectMapper objectMapper;
    private boolean allowNull;

    @Override
    public void initialize(ValidJson constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }

        if (value.trim().isEmpty()) {
            return false;
        }

        try {
            objectMapper.readTree(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}