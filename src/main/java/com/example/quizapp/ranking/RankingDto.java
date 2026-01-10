package com.example.quizapp.ranking;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingDto {
    private String playerNickname;

    private Integer score;

    private Integer maxScore;

    private Double percentageScore;

    private Integer correctAnswers;

    private Integer wrongAnswers;

    private Integer totalQuestions;

    private Integer timeTakenSeconds;

    private String grade;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    private String quizTitle;
}