package com.example.quizapp.common.exception;

public class QuizAlreadyExistsException extends RuntimeException {
    public QuizAlreadyExistsException(String title) {
        super("Quiz with title '" + title + "' already exists");
    }

    public QuizAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}