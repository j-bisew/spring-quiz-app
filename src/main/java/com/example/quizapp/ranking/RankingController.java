package com.example.quizapp.ranking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ranking Management", description = "APIs for leaderboards and rankings")
public class RankingController {
    private final RankingService rankingService;
    private final RankingExportService rankingExportService;

//    Get top rankings for a quiz
    @GetMapping("/quiz/{quizId}")
    @Operation(
            summary = "Get leaderboard for quiz",
            description = "Retrieves top rankings for a specific quiz. Default limit is 10."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rankings"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<RankingDto>> getQuizRankings(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId,
            @RequestParam(required = false, defaultValue = "10")
            @Parameter(description = "Maximum number of results") Integer limit) {
        log.info("GET /api/v1/rankings/quiz/{} - Getting top {} rankings", quizId, limit);

        List<RankingDto> rankings = rankingService.getTopRankings(quizId, limit);
        return ResponseEntity.ok(rankings);
    }

//    Get full leaderboard for a quiz (all results)
    @GetMapping("/quiz/{quizId}/full")
    @Operation(
            summary = "Get full leaderboard",
            description = "Retrieves all results for a quiz, sorted by score and time"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved full leaderboard"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<RankingDto>> getFullLeaderboard(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/rankings/quiz/{}/full - Getting full leaderboard", quizId);

        List<RankingDto> rankings = rankingService.getFullLeaderboard(quizId);
        return ResponseEntity.ok(rankings);
    }

//    Get player's ranking position
    @GetMapping("/quiz/{quizId}/player/{playerId}")
    @Operation(
            summary = "Get player's ranking position",
            description = "Retrieves player's position and stats in quiz leaderboard"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved player ranking"),
            @ApiResponse(responseCode = "404", description = "Quiz or player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<RankingPositionDto> getPlayerRanking(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId,
            @PathVariable @Parameter(description = "Player ID") Long playerId) {
        log.info("GET /api/v1/rankings/quiz/{}/player/{} - Getting player ranking", quizId, playerId);

        RankingPositionDto position = rankingService.getPlayerRanking(quizId, playerId);
        return ResponseEntity.ok(position);
    }

//    Get global rankings
    @GetMapping("/global")
    @Operation(
            summary = "Get global rankings",
            description = "Retrieves top players across all quizzes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved global rankings"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<GlobalRankingDto>> getGlobalRankings(
            @RequestParam(required = false, defaultValue = "10")
            @Parameter(description = "Maximum number of results") Integer limit) {
        log.info("GET /api/v1/rankings/global - Getting global rankings, limit: {}", limit);

        List<GlobalRankingDto> rankings = rankingService.getGlobalRankings(limit);
        return ResponseEntity.ok(rankings);
    }

//    Export rankings to CSV
    @GetMapping("/quiz/{quizId}/export/csv")
    @Operation(
            summary = "Export rankings to CSV",
            description = "Downloads quiz rankings as CSV file"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> exportToCsv(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/rankings/quiz/{}/export/csv - Exporting to CSV", quizId);

        Resource resource = rankingExportService.exportToCsv(quizId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"quiz_" + quizId + "_rankings.csv\"")
                .body(resource);
    }

//    Export rankings to PDF
    @GetMapping("/quiz/{quizId}/export/pdf")
    @Operation(
            summary = "Export rankings to PDF",
            description = "Downloads quiz rankings as PDF file"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF file generated successfully"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> exportToPdf(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/rankings/quiz/{}/export/pdf - Exporting to PDF", quizId);

        Resource resource = rankingExportService.exportToPdf(quizId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"quiz_" + quizId + "_rankings.pdf\"")
                .body(resource);
    }
}