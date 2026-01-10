package com.example.quizapp.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStatisticsDto {
    private Long quizId;

    private String quizTitle;

    private long totalAttempts;

    private long passedAttempts;

    private long failedAttempts;

    private Double passRate;

    private Double averageScore;

    private Integer highestScore;

    private Integer lowestScore;

    private Integer maxScore;

    private Double averageTimeSeconds;
}