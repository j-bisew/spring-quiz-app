package com.example.quizapp.question;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Question operations
 */
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Question Management", description = "APIs for managing quiz questions")
public class QuestionController {

    private final QuestionService questionService;

    /**
     * Get all questions for a quiz
     */
    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get all questions for a quiz", description = "Retrieves all questions for a specific quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved questions"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<QuestionDto>> getQuestionsByQuizId(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/questions/quiz/{} - Fetching questions", quizId);
        List<QuestionDto> questions = questionService.getQuestionsByQuizId(quizId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Get questions ordered by position
     */
    @GetMapping("/quiz/{quizId}/ordered")
    @Operation(summary = "Get ordered questions", description = "Retrieves questions ordered by position")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ordered questions"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<QuestionDto>> getQuestionsOrdered(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/questions/quiz/{}/ordered - Fetching ordered questions", quizId);
        List<QuestionDto> questions = questionService.getQuestionsOrderedByPosition(quizId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Get question by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID", description = "Retrieves a question by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved question"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuestionDto> getQuestionById(
            @PathVariable @Parameter(description = "Question ID") Long id) {
        log.info("GET /api/v1/questions/{} - Fetching question", id);
        QuestionDto question = questionService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }

    /**
     * Create new question
     */
    @PostMapping
    @Operation(summary = "Create new question", description = "Creates a new question for a quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Question created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuestionDto> createQuestion(
            @Valid @RequestBody @Parameter(description = "Question data") QuestionDto questionDto) {
        log.info("POST /api/v1/questions - Creating new question for quiz: {}", questionDto.getQuizId());
        QuestionDto createdQuestion = questionService.createQuestion(questionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    /**
     * Update existing question
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update question", description = "Updates an existing question")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuestionDto> updateQuestion(
            @PathVariable @Parameter(description = "Question ID") Long id,
            @Valid @RequestBody @Parameter(description = "Updated question data") QuestionDto questionDto) {
        log.info("PUT /api/v1/questions/{} - Updating question", id);
        QuestionDto updatedQuestion = questionService.updateQuestion(id, questionDto);
        return ResponseEntity.ok(updatedQuestion);
    }

    /**
     * Delete question (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete question", description = "Soft deletes a question")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Question deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable @Parameter(description = "Question ID") Long id) {
        log.info("DELETE /api/v1/questions/{} - Deleting question", id);
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Permanently delete question
     */
    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete question", description = "Permanently deletes a question from database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Question permanently deleted"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> permanentlyDeleteQuestion(
            @PathVariable @Parameter(description = "Question ID") Long id) {
        log.info("DELETE /api/v1/questions/{}/permanent - Permanently deleting question", id);
        questionService.permanentlyDeleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count questions in a quiz
     */
    @GetMapping("/quiz/{quizId}/count")
    @Operation(summary = "Count questions", description = "Returns the number of questions in a quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved count"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> countQuestions(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/questions/quiz/{}/count - Counting questions", quizId);
        long count = questionService.countQuestions(quizId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get total points for a quiz
     */
    @GetMapping("/quiz/{quizId}/total-points")
    @Operation(summary = "Get total points", description = "Returns total points for all questions in a quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total points"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Integer> getTotalPoints(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/questions/quiz/{}/total-points - Getting total points", quizId);
        int totalPoints = questionService.getTotalPoints(quizId);
        return ResponseEntity.ok(totalPoints);
    }

    /**
     * Validate answer for a question
     */
    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate answer", description = "Validates a user's answer for a question")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer validated successfully"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AnswerValidationResponse> validateAnswer(
            @PathVariable @Parameter(description = "Question ID") Long id,
            @RequestBody @Parameter(description = "User's answer") AnswerValidationRequest request) {
        log.info("POST /api/v1/questions/{}/validate - Validating answer", id);
        boolean isCorrect = questionService.validateAnswer(id, request.getUserAnswer());

        AnswerValidationResponse response = AnswerValidationResponse.builder()
                .questionId(id)
                .isCorrect(isCorrect)
                .build();

        return ResponseEntity.ok(response);
    }
}