package com.example.quizapp.common.exception;

public class GameSessionExpiredException extends RuntimeException {
    public GameSessionExpiredException(String sessionId) {
        super("Game session expired or invalid: " + sessionId);
    }

    public GameSessionExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}