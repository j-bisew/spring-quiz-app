package com.example.quizapp.analytics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Advanced analytics using JdbcTemplate SQL queries")
@SecurityRequirement(name = "basicAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

//    Get comprehensive quiz analytics
    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get quiz analytics", description = "Get detailed analytics for a quiz including top performers and question difficulty")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getQuizAnalytics(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/analytics/quiz/{}", quizId);

        Map<String, Object> analytics = analyticsService.getQuizAnalytics(quizId);
        return ResponseEntity.ok(analytics);
    }

//    Get player performance report
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get player report", description = "Get performance history and statistics for a player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getPlayerReport(
            @PathVariable @Parameter(description = "Player ID") Long playerId) {
        log.info("GET /api/v1/analytics/player/{}", playerId);

        Map<String, Object> report = analyticsService.getPlayerReport(playerId);
        return ResponseEntity.ok(report);
    }

//    Get platform-wide analytics
    @GetMapping("/platform")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get platform analytics", description = "Get comprehensive platform-wide analytics (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getPlatformAnalytics() {
        log.info("GET /api/v1/analytics/platform");

        Map<String, Object> analytics = analyticsService.getPlatformAnalytics();
        return ResponseEntity.ok(analytics);
    }

//    Compare result with average
    @GetMapping("/result/{resultId}/compare")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Compare result", description = "Compare a game result with quiz average")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comparison retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Result not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> compareWithAverage(
            @PathVariable @Parameter(description = "Game result ID") Long resultId) {
        log.info("GET /api/v1/analytics/result/{}/compare", resultId);

        Map<String, Object> comparison = analyticsService.compareResultWithAverage(resultId);
        return ResponseEntity.ok(comparison);
    }

//    Cleanup old incomplete results
    @DeleteMapping("/cleanup/incomplete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cleanup incomplete results", description = "Delete old incomplete game results (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cleanup completed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> cleanupIncompleteResults(
            @RequestParam(defaultValue = "30") @Parameter(description = "Days old") int daysOld) {
        log.info("DELETE /api/v1/analytics/cleanup/incomplete?daysOld={}", daysOld);

        int deleted = analyticsService.cleanupOldIncompleteResults(daysOld);
        return ResponseEntity.ok(Map.of(
                "deleted", deleted,
                "message", "Deleted " + deleted + " incomplete results older than " + daysOld + " days"
        ));
    }

//    Bulk update quiz status
    @PutMapping("/quizzes/bulk-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk update quiz status", description = "Activate or deactivate multiple quizzes (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update completed"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> bulkUpdateQuizStatus(
            @RequestBody @Parameter(description = "Quiz IDs") List<Long> quizIds,
            @RequestParam @Parameter(description = "Active status") boolean active) {
        log.info("PUT /api/v1/analytics/quizzes/bulk-status - {} quizzes to active={}", quizIds.size(), active);

        int updated = analyticsService.bulkUpdateQuizStatus(quizIds, active);
        return ResponseEntity.ok(Map.of(
                "updated", updated,
                "message", "Updated " + updated + " quizzes to active=" + active
        ));
    }
}