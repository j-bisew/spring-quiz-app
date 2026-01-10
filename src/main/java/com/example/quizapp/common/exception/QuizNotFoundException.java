package com.example.quizapp.common.exception;

public class QuizNotFoundException extends ResourceNotFoundException {
    public QuizNotFoundException(Long quizId) {
        super("Quiz not found with id: " + quizId);
    }

    public QuizNotFoundException(String message) {
        super(message);
    }
}