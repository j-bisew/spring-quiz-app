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
public class RankingPositionDto {
    private Long playerId;

    private String playerNickname;

    private Long quizId;

    private String quizTitle;

    private Integer position;

    private Integer totalPlayers;

    private Integer score;

    private Integer maxScore;

    private Double percentageScore;

    private Integer timeTakenSeconds;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
}