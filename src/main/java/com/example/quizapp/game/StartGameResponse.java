package com.example.quizapp.game;

import com.example.quizapp.question.QuestionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartGameResponse {
    private String sessionId;

    private Long quizId;

    private String quizTitle;

    private String quizDescription;

    private Integer totalQuestions;

    private Integer totalPoints;

    private Integer timeLimitMinutes;

    private boolean randomQuestionOrder;

    private boolean randomAnswerOrder;

    private boolean negativePointsEnabled;

    private boolean backButtonBlocked;

    private List<QuestionDto> questions;

    private Long playerId;

    private String playerNickname;
}