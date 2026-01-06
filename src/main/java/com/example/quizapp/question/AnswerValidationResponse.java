package com.example.quizapp.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerValidationResponse {

    private Long questionId;

    private boolean isCorrect;

    private String feedback;
}