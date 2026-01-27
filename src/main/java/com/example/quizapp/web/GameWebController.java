package com.example.quizapp.web;

import com.example.quizapp.game.*;
import com.example.quizapp.question.QuestionDto;
import com.example.quizapp.question.QuestionType;
import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@Slf4j
public class GameWebController {

    private final QuizService quizService;
    private final GameService gameService;
    private final ObjectMapper objectMapper;

    //    Form for player nickname
    @Data
    public static class PlayerForm {
        private String nickname;
    }

    //    Form for collecting quiz answers
    @Data
    public static class GameForm {
        private String sessionId;
        private Long quizId;
        private Long playerId;
        private List<AnswerSubmissionWrapper> answers = new ArrayList<>();
    }

    //    Single answer wrapper
    @Data
    public static class AnswerSubmissionWrapper {
        private Long questionId;
        private String userAnswer;
    }

    // DTO for matching pairs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchingPair {
        private String left;
        private String right;
    }

    // DTO for questions with parsed options
    @Data
    public static class QuestionWithParsedOptions {
        private Long id;
        private String questionText;
        private String questionType;
        private List<String> parsedOptions;        // For regular questions
        private List<MatchingPair> matchingPairs;  // For MATCHING questions
        private Integer points;
        private String imageUrl;
        private QuestionDto originalQuestion;

        public QuestionWithParsedOptions(QuestionDto question, List<String> parsedOptions) {
            this.id = question.getId();
            this.questionText = question.getQuestionText();
            this.questionType = question.getQuestionType().name();
            this.parsedOptions = parsedOptions;
            this.matchingPairs = null;
            this.points = question.getPoints();
            this.imageUrl = question.getImageUrl();
            this.originalQuestion = question;
        }

        public QuestionWithParsedOptions(QuestionDto question, List<String> parsedOptions, List<MatchingPair> matchingPairs) {
            this.id = question.getId();
            this.questionText = question.getQuestionText();
            this.questionType = question.getQuestionType().name();
            this.parsedOptions = parsedOptions;
            this.matchingPairs = matchingPairs;
            this.points = question.getPoints();
            this.imageUrl = question.getImageUrl();
            this.originalQuestion = question;
        }

        // Delegate getter for questionType to return the enum
        public QuestionType getQuestionType() {
            return originalQuestion.getQuestionType();
        }
    }

    //    Show game start page with nickname form
    @GetMapping("/start/{quizId}")
    public String showStartPage(@PathVariable Long quizId, Model model) {
        log.info("GET /game/start/{} - Showing start page", quizId);

        QuizDto quiz = quizService.getQuizById(quizId);

        if (!quiz.isActive()) {
            model.addAttribute("error", "This quiz is not currently available.");
            return "redirect:/quizzes";
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("playerForm", new PlayerForm());
        model.addAttribute("title", "Start Quiz - " + quiz.getTitle());

        return "game-start";
    }

    //    Process game start - create player and session
    @PostMapping("/start/{quizId}")
    public String startGame(@PathVariable Long quizId,
                            @ModelAttribute("playerForm") PlayerForm playerForm,
                            HttpSession session,
                            Model model) {
        log.info("POST /game/start/{} - Starting game for player: {}", quizId, playerForm.getNickname());

        // Validate nickname
        if (playerForm.getNickname() == null || playerForm.getNickname().trim().isEmpty()) {
            QuizDto quiz = quizService.getQuizById(quizId);
            model.addAttribute("quiz", quiz);
            model.addAttribute("error", "Please enter your nickname");
            model.addAttribute("playerForm", playerForm);
            return "game-start";
        }

        try {
            // Create game start request
            StartGameRequest request = StartGameRequest.builder()
                    .quizId(quizId)
                    .playerNickname(playerForm.getNickname().trim())
                    .build();

            // Start the game
            StartGameResponse response = gameService.startGame(request);

            // Store game session data in HTTP session
            session.setAttribute("gameSession", response);
            session.setAttribute("gameStartTime", System.currentTimeMillis());

            log.info("Game started successfully. Session: {}, Player: {}, Questions: {}",
                    response.getSessionId(), response.getPlayerNickname(), response.getQuestions().size());

            return "redirect:/game/play/" + quizId;

        } catch (Exception e) {
            log.error("Error starting game: {}", e.getMessage());
            QuizDto quiz = quizService.getQuizById(quizId);
            model.addAttribute("quiz", quiz);
            model.addAttribute("error", "Could not start game: " + e.getMessage());
            model.addAttribute("playerForm", playerForm);
            return "game-start";
        }
    }

    //    Show quiz play page with questions
    @GetMapping("/play/{quizId}")
    public String playGame(@PathVariable Long quizId, HttpSession session, Model model) {
        log.info("GET /game/play/{} - Loading quiz play page", quizId);

        // Get game session from HTTP session
        StartGameResponse gameSession = (StartGameResponse) session.getAttribute("gameSession");

        // Validate session
        if (gameSession == null || !gameSession.getQuizId().equals(quizId)) {
            log.warn("Invalid game session, redirecting to start");
            return "redirect:/game/start/" + quizId;
        }

        // ========== CRITICAL FIX: Parse answer options BEFORE passing to view ==========
        List<QuestionWithParsedOptions> questionsWithParsedOptions = gameSession.getQuestions().stream()
                .map(this::parseQuestionOptions)
                .collect(Collectors.toList());

        log.info("Parsed {} questions with options", questionsWithParsedOptions.size());
        // ================================================================================

        // Prepare answer form
        GameForm gameForm = new GameForm();
        gameForm.setSessionId(gameSession.getSessionId());
        gameForm.setQuizId(quizId);
        gameForm.setPlayerId(gameSession.getPlayerId());

        // Initialize empty answers for each question
        for (var question : gameSession.getQuestions()) {
            AnswerSubmissionWrapper answer = new AnswerSubmissionWrapper();
            answer.setQuestionId(question.getId());
            answer.setUserAnswer("");
            gameForm.getAnswers().add(answer);
        }

        // Add to model - USE PARSED QUESTIONS!
        model.addAttribute("questions", questionsWithParsedOptions);
        model.addAttribute("gameForm", gameForm);
        model.addAttribute("quizTitle", gameSession.getQuizTitle());
        model.addAttribute("timeLimit", gameSession.getTimeLimitMinutes());
        model.addAttribute("backButtonBlocked", gameSession.isBackButtonBlocked());
        model.addAttribute("title", "Playing: " + gameSession.getQuizTitle());

        return "game-play";
    }

    // Helper method to parse answer options
    private QuestionWithParsedOptions parseQuestionOptions(QuestionDto question) {
        try {
            String answerOptions = question.getAnswerOptions();

            if (answerOptions == null || answerOptions.trim().isEmpty() || answerOptions.equals("null")) {
                log.warn("Question {} has empty answerOptions", question.getId());
                return new QuestionWithParsedOptions(question, List.of());
            }

            // SPECIAL HANDLING FOR MATCHING TYPE
            if (question.getQuestionType() == QuestionType.MATCHING) {
                try {
                    // Parse as List<Map<String, String>> for matching pairs
                    List<Map<String, String>> rawPairs = objectMapper.readValue(
                            answerOptions,
                            new TypeReference<List<Map<String, String>>>() {}
                    );

                    // Convert to MatchingPair objects
                    List<MatchingPair> pairs = rawPairs.stream()
                            .map(map -> new MatchingPair(map.get("left"), map.get("right")))
                            .collect(Collectors.toList());

                    // Also create parsedOptions for right-side values (for dropdown)
                    List<String> rightOptions = pairs.stream()
                            .map(MatchingPair::getRight)
                            .collect(Collectors.toList());

                    log.debug("Parsed {} matching pairs for question {}", pairs.size(), question.getId());
                    return new QuestionWithParsedOptions(question, rightOptions, pairs);

                } catch (Exception e) {
                    log.error("Error parsing matching pairs for question {}: {}", question.getId(), e.getMessage());
                    return new QuestionWithParsedOptions(question, List.of(), List.of());
                }
            }

            // NORMAL HANDLING FOR OTHER TYPES
            List<String> options = objectMapper.readValue(
                    answerOptions,
                    new TypeReference<List<String>>() {}
            );

            log.debug("Parsed {} options for question {}", options.size(), question.getId());
            return new QuestionWithParsedOptions(question, options);

        } catch (Exception e) {
            log.error("Error parsing options for question {}: {} - Options string: '{}'",
                    question.getId(), e.getMessage(), question.getAnswerOptions());
            return new QuestionWithParsedOptions(question, List.of());
        }
    }

    //    Process quiz submission
    @PostMapping("/submit")
    public String submitAnswers(@ModelAttribute GameForm gameForm, HttpSession session, Model model) {
        log.info("POST /game/submit - Submitting answers for session: {}", gameForm.getSessionId());

        // Calculate time taken
        Long startTime = (Long) session.getAttribute("gameStartTime");
        Integer timeTakenSeconds = null;
        if (startTime != null) {
            timeTakenSeconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
        }

        // Filter out empty answers and map to submission format
        List<SubmitAnswersRequest.AnswerSubmission> submissions = new ArrayList<>();
        for (AnswerSubmissionWrapper wrapper : gameForm.getAnswers()) {
            if (wrapper.getUserAnswer() != null && !wrapper.getUserAnswer().trim().isEmpty()) {
                submissions.add(new SubmitAnswersRequest.AnswerSubmission(
                        wrapper.getQuestionId(),
                        wrapper.getUserAnswer().trim()
                ));
            }
        }

        // Create submission request
        SubmitAnswersRequest request = SubmitAnswersRequest.builder()
                .sessionId(gameForm.getSessionId())
                .quizId(gameForm.getQuizId())
                .playerId(gameForm.getPlayerId())
                .answers(submissions)
                .timeTakenSeconds(timeTakenSeconds)
                .build();

        try {
            // Submit and get result
            GameResultDto result = gameService.submitAnswers(request);

            // Clear game session
            session.removeAttribute("gameSession");
            session.removeAttribute("gameStartTime");

            log.info("Game submitted successfully. Result ID: {}, Score: {}/{}",
                    result.getId(), result.getScore(), result.getMaxScore());

            return "redirect:/game/result/" + result.getId();

        } catch (Exception e) {
            log.error("Error submitting game: {}", e.getMessage(), e);
            model.addAttribute("error", "Could not submit quiz: " + e.getMessage());
            return "redirect:/game/play/" + gameForm.getQuizId();
        }
    }

    //    Show game result page
    @GetMapping("/result/{resultId}")
    public String showResult(@PathVariable Long resultId, Model model) {
        log.info("GET /game/result/{} - Showing result page", resultId);

        try {
            GameResultDto result = gameService.getGameResult(resultId);

            model.addAttribute("result", result);
            model.addAttribute("title", "Quiz Result");

            return "game-result";

        } catch (Exception e) {
            log.error("Error loading result: {}", e.getMessage(), e);
            model.addAttribute("error", "Could not load result: " + e.getMessage());
            return "redirect:/quizzes";
        }
    }
}