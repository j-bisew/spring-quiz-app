package com.example.quizapp.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Question entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDto {

    private Long id;

    @NotNull(message = "Question type is required")
    private QuestionType questionType;

    @NotBlank(message = "Question text is required")
    @Size(min = 5, max = 1000, message = "Question text must be between 5 and 1000 characters")
    private String questionText;

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    @Max(value = 100, message = "Points cannot exceed 100")
    private Integer points;

    @Min(value = 0, message = "Negative points cannot be negative")
    @Max(value = 50, message = "Negative points cannot exceed 50")
    private Integer negativePoints;

    private Integer questionOrder;

    @Min(value = 5, message = "Time limit must be at least 5 seconds")
    @Max(value = 600, message = "Time limit cannot exceed 600 seconds (10 minutes)")
    private Integer timeLimitSeconds;

    @NotBlank(message = "Answer options are required")
    private String answerOptions;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    @Size(max = 1000, message = "Explanation must not exceed 1000 characters")
    private String explanation;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    private Boolean active;

    @NotNull(message = "Quiz ID is required")
    private Long quizId;
}