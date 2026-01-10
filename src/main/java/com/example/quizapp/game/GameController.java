package com.example.quizapp.game;

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

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Game Management", description = "APIs for quiz gameplay (Player Mode)")
public class GameController {

    private final GameService gameService;

//    Start a new game session
    @PostMapping("/start")
    @Operation(
            summary = "Start a new game",
            description = "Start a new quiz game session. Player provides nickname and quiz ID. Returns questions and game settings."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game started successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or quiz not active"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StartGameResponse> startGame(
            @Valid @RequestBody @Parameter(description = "Game start request with quiz ID and player nickname")
            StartGameRequest request) {
        log.info("POST /api/v1/game/start - Starting game for quiz {} with player {}",
                request.getQuizId(), request.getPlayerNickname());

        StartGameResponse response = gameService.startGame(request);
        return ResponseEntity.ok(response);
    }

//    Submit answers and get results
    @PostMapping("/submit")
    @Operation(
            summary = "Submit quiz answers",
            description = "Submit all answers for a quiz game. Returns detailed results with score, correct/wrong answers, and feedback."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Answers submitted successfully, results calculated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Quiz or player not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GameResultDto> submitAnswers(
            @Valid @RequestBody @Parameter(description = "Answer submission with session ID and all answers")
            SubmitAnswersRequest request) {
        log.info("POST /api/v1/game/submit - Submitting answers for session {}", request.getSessionId());

        GameResultDto result = gameService.submitAnswers(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

//    Get game result by ID
    @GetMapping("/result/{resultId}")
    @Operation(
            summary = "Get game result",
            description = "Retrieve detailed game result by result ID. Includes score, answers, and feedback."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Result retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Result not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GameResultDto> getGameResult(
            @PathVariable @Parameter(description = "Game result ID") Long resultId) {
        log.info("GET /api/v1/game/result/{} - Fetching game result", resultId);

        GameResultDto result = gameService.getGameResult(resultId);
        return ResponseEntity.ok(result);
    }

//    Get quiz statistics
    @GetMapping("/statistics/{quizId}")
    @Operation(
            summary = "Get quiz statistics",
            description = "Retrieve statistics for a quiz including total attempts, pass rate, average score, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GameStatisticsDto> getQuizStatistics(
            @PathVariable @Parameter(description = "Quiz ID") Long quizId) {
        log.info("GET /api/v1/game/statistics/{} - Fetching quiz statistics", quizId);

        GameStatisticsDto statistics = gameService.getQuizStatistics(quizId);
        return ResponseEntity.ok(statistics);
    }
}