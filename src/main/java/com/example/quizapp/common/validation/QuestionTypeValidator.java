package com.example.quizapp.common.validation;

import com.example.quizapp.question.QuestionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class QuestionTypeValidator implements ConstraintValidator<ValidQuestionType, QuestionType> {

    @Override
    public void initialize(ValidQuestionType constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(QuestionType value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        // Check if it's a valid enum value
        try {
            return value.name() != null;
        } catch (Exception e) {
            return false;
        }
    }
}