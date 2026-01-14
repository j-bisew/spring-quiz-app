package com.example.quizapp.common.validation;

import com.example.quizapp.question.QuestionType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuestionTypeValidator.class)
@Documented
public @interface ValidQuestionType {

    String message() default "Invalid question type. Must be one of: SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, DROPDOWN, FILL_BLANKS, SORTING, MATCHING";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}