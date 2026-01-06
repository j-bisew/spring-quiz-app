package com.example.quizapp.quiz;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Quiz operations
 */
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz Management", description = "APIs for managing quizzes")
public class QuizController {

    private final QuizService quizService;

    /**
     * Get all active quizzes
     */
    @GetMapping
    @Operation(summary = "Get all active quizzes", description = "Retrieves a list of all active quizzes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quizzes"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<QuizDto>> getAllQuizzes() {
        log.info("GET /api/v1/quizzes - Fetching all active quizzes");
        List<QuizDto> quizzes = quizService.getAllActiveQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Get all active quizzes with pagination
     */
    @GetMapping("/paginated")
    @Operation(summary = "Get all active quizzes with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated quizzes"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<QuizDto>> getAllQuizzesPaginated(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("GET /api/v1/quizzes/paginated - Fetching quizzes with pagination");
        Page<QuizDto> quizzes = quizService.getAllActiveQuizzes(pageable);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Get quiz by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID", description = "Retrieves a quiz by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quiz"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuizDto> getQuizById(
            @PathVariable @Parameter(description = "Quiz ID") Long id) {
        log.info("GET /api/v1/quizzes/{} - Fetching quiz", id);
        QuizDto quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Get quiz by ID with questions
     */
    @GetMapping("/{id}/with-questions")
    @Operation(summary = "Get quiz by ID with questions", description = "Retrieves a quiz with all its questions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quiz with questions"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuizDto> getQuizByIdWithQuestions(
            @PathVariable @Parameter(description = "Quiz ID") Long id) {
        log.info("GET /api/v1/quizzes/{}/with-questions - Fetching quiz with questions", id);
        QuizDto quiz = quizService.getQuizByIdWithQuestions(id);
        return ResponseEntity.ok(quiz);
    }

    /**
     * Create new quiz
     */
    @PostMapping
    @Operation(summary = "Create new quiz", description = "Creates a new quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quiz created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuizDto> createQuiz(
            @Valid @RequestBody @Parameter(description = "Quiz data") QuizDto quizDto) {
        log.info("POST /api/v1/quizzes - Creating new quiz: {}", quizDto.getTitle());
        QuizDto createdQuiz = quizService.createQuiz(quizDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    /**
     * Update existing quiz
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update quiz", description = "Updates an existing quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuizDto> updateQuiz(
            @PathVariable @Parameter(description = "Quiz ID") Long id,
            @Valid @RequestBody @Parameter(description = "Updated quiz data") QuizDto quizDto) {
        log.info("PUT /api/v1/quizzes/{} - Updating quiz", id);
        QuizDto updatedQuiz = quizService.updateQuiz(id, quizDto);
        return ResponseEntity.ok(updatedQuiz);
    }

    /**
     * Delete quiz (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete quiz", description = "Soft deletes a quiz (sets active to false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quiz deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable @Parameter(description = "Quiz ID") Long id) {
        log.info("DELETE /api/v1/quizzes/{} - Deleting quiz", id);
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Permanently delete quiz
     */
    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete quiz", description = "Permanently deletes a quiz from database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quiz permanently deleted"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> permanentlyDeleteQuiz(
            @PathVariable @Parameter(description = "Quiz ID") Long id) {
        log.info("DELETE /api/v1/quizzes/{}/permanent - Permanently deleting quiz", id);
        quizService.permanentlyDeleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search quizzes by title
     */
    @GetMapping("/search")
    @Operation(summary = "Search quizzes", description = "Search quizzes by title keyword")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<QuizDto>> searchQuizzes(
            @RequestParam @Parameter(description = "Search keyword") String keyword) {
        log.info("GET /api/v1/quizzes/search?keyword={} - Searching quizzes", keyword);
        List<QuizDto> quizzes = quizService.searchQuizzes(keyword);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Search quizzes by title with pagination
     */
    @GetMapping("/search/paginated")
    @Operation(summary = "Search quizzes with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated search results"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<QuizDto>> searchQuizzesPaginated(
            @RequestParam @Parameter(description = "Search keyword") String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("GET /api/v1/quizzes/search/paginated?keyword={} - Searching quizzes with pagination", keyword);
        Page<QuizDto> quizzes = quizService.searchQuizzes(keyword, pageable);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Get quizzes by creator
     */
    @GetMapping("/creator/{createdBy}")
    @Operation(summary = "Get quizzes by creator", description = "Retrieves all quizzes created by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quizzes"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<QuizDto>> getQuizzesByCreator(
            @PathVariable @Parameter(description = "Creator username") String createdBy) {
        log.info("GET /api/v1/quizzes/creator/{} - Fetching quizzes by creator", createdBy);
        List<QuizDto> quizzes = quizService.getQuizzesByCreator(createdBy);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Count active quizzes
     */
    @GetMapping("/count")
    @Operation(summary = "Count active quizzes", description = "Returns the total number of active quizzes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved count"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Long> countActiveQuizzes() {
        log.info("GET /api/v1/quizzes/count - Counting active quizzes");
        long count = quizService.countActiveQuizzes();
        return ResponseEntity.ok(count);
    }
}