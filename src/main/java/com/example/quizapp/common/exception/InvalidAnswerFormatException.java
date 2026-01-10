package com.example.quizapp.common.exception;

public class InvalidAnswerFormatException extends RuntimeException {
    public InvalidAnswerFormatException(String message) {
        super(message);
    }

    public InvalidAnswerFormatException(String questionType, String expectedFormat) {
        super(String.format("Invalid answer format for question type %s. Expected format: %s",
                questionType, expectedFormat));
    }

    public InvalidAnswerFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}