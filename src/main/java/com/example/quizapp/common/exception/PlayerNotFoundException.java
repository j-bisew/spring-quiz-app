package com.example.quizapp.common.exception;

public class PlayerNotFoundException extends ResourceNotFoundException {
    public PlayerNotFoundException(Long playerId) {
        super("Player not found with id: " + playerId);
    }

    public PlayerNotFoundException(String message) {
        super(message);
    }
}