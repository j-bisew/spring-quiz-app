package com.example.quizapp.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitAnswersRequest {
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotEmpty(message = "Answers are required")
    private List<AnswerSubmission> answers;

    private Integer timeTakenSeconds;

//    Single answer submission
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerSubmission {

        @NotNull(message = "Question ID is required")
        private Long questionId;

        @NotBlank(message = "User answer is required")
        private String userAnswer;
    }
}