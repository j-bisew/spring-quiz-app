package com.example.quizapp.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResultDto implements Serializable {
    private Long id;

    private Long playerId;

    private String playerNickname;

    private Long quizId;

    private String quizTitle;

    private int score;

    private int maxScore;

    private int correctAnswers;

    private int wrongAnswers;

    private int totalQuestions;

    private Integer timeTakenSeconds;

    private Double percentageScore;

    private String grade;

    private boolean passed;

    private List<DetailedAnswer> detailedAnswers;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * Detailed answer information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailedAnswer {
        private Long questionId;
        private String questionText;
        private String userAnswer;
        private String correctAnswer;
        private boolean isCorrect;
        private int pointsEarned;
        private String explanation;
    }
}