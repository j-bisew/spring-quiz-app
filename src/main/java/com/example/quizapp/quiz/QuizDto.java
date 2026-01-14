package com.example.quizapp.quiz;

import com.example.quizapp.common.validation.ValidTimeLimit;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDto {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private boolean randomQuestionOrder;

    private boolean randomAnswerOrder;

    @ValidTimeLimit(min = 1, max = 300, message = "Time limit must be between 1 and 300 minutes")
    private Integer timeLimitMinutes;

    private boolean negativePointsEnabled;

    private boolean backButtonBlocked;

    private boolean active;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // summary fields
    private Integer questionCount;

    private Integer totalPoints;
}