package com.example.quizapp.common.exception;

public class InvalidQuestionTypeException extends RuntimeException {
    public InvalidQuestionTypeException(String questionType) {
        super("Invalid or unsupported question type: " + questionType);
    }

    public InvalidQuestionTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}