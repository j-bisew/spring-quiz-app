package com.example.quizapp.game;

import com.example.quizapp.common.exception.PlayerNotFoundException;
import com.example.quizapp.common.exception.QuizNotFoundException;
import com.example.quizapp.common.exception.ResourceNotFoundException;
import com.example.quizapp.player.Player;
import com.example.quizapp.player.PlayerRepository;
import com.example.quizapp.question.Question;
import com.example.quizapp.question.QuestionDto;
import com.example.quizapp.question.QuestionMapper;
import com.example.quizapp.question.QuestionService;
import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GameService {

    private final QuizRepository quizRepository;
    private final PlayerRepository playerRepository;
    private final GameResultRepository gameResultRepository;
    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    /**
     * Start a new game session
     */
    @Transactional
    public StartGameResponse startGame(StartGameRequest request) {
        log.info("Starting game for quiz {} with player {}", request.getQuizId(), request.getPlayerNickname());

        Quiz quiz = quizExists(request.getQuizId());

        if (!quiz.isActive()) {
            throw new IllegalArgumentException("Quiz is not active");
        }

        // Get or create player
        Player player = getOrCreatePlayer(request);

        // Generate session ID
        String sessionId = request.getSessionId() != null ?
                request.getSessionId() :
                UUID.randomUUID().toString();

        // Get questions
        List<Question> questions = quiz.getQuestions().stream()
                .filter(Question::isActive)
                .collect(Collectors.toList());

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("Quiz has no active questions");
        }

        // Apply random question order if enabled
        if (quiz.isRandomQuestionOrder()) {
            Collections.shuffle(questions);
        }

        // Convert to DTOs
        List<QuestionDto> questionDtos = questions.stream()
                .map(questionMapper::toDto)
                .collect(Collectors.toList());

        // If random answer order is enabled, shuffle answer options
        if (quiz.isRandomAnswerOrder()) {
            questionDtos = shuffleAnswerOptions(questionDtos);
        }

        // Remove correct answers from response (don't send to frontend)
        questionDtos.forEach(q -> q.setCorrectAnswer(null));

        log.info("Game started successfully. Session: {}, Player: {}, Questions: {}",
                sessionId, player.getNickname(), questions.size());

        return StartGameResponse.builder()
                .sessionId(sessionId)
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .quizDescription(quiz.getDescription())
                .totalQuestions(questions.size())
                .totalPoints(quiz.getTotalPoints())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .randomQuestionOrder(quiz.isRandomQuestionOrder())
                .randomAnswerOrder(quiz.isRandomAnswerOrder())
                .negativePointsEnabled(quiz.isNegativePointsEnabled())
                .backButtonBlocked(quiz.isBackButtonBlocked())
                .questions(questionDtos)
                .playerId(player.getId())
                .playerNickname(player.getNickname())
                .build();
    }

    /**
     * Submit answers and calculate results
     */
    @Transactional
    public GameResultDto submitAnswers(SubmitAnswersRequest request) {
        log.info("Submitting answers for session: {}", request.getSessionId());

        // Verify quiz exists
        Quiz quiz = quizExists(request.getQuizId());

        // Verify player exists
        Player player = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new PlayerNotFoundException(request.getPlayerId()));

        // Get all questions
        List<Question> questions = quiz.getQuestions().stream()
                .filter(Question::isActive)
                .collect(Collectors.toList());

        // Calculate results
        int totalScore = 0;
        int correctAnswers = 0;
        int wrongAnswers = 0;
        List<GameResultDto.DetailedAnswer> detailedAnswers = new ArrayList<>();

        for (SubmitAnswersRequest.AnswerSubmission submission : request.getAnswers()) {
            Question question = questions.stream()
                    .filter(q -> q.getId().equals(submission.getQuestionId()))
                    .findFirst()
                    .orElse(null);

            if (question == null) {
                log.warn("Question not found: {}", submission.getQuestionId());
                continue;
            }

            // Validate answer
            boolean isCorrect = questionService.validateAnswer(
                    question.getId(),
                    submission.getUserAnswer()
            );

            int pointsEarned = 0;
            if (isCorrect) {
                pointsEarned = question.getPoints();
                totalScore += pointsEarned;
                correctAnswers++;
            } else {
                wrongAnswers++;
                // Apply negative points if enabled
                if (quiz.isNegativePointsEnabled() && question.getNegativePoints() != null) {
                    pointsEarned = -question.getNegativePoints();
                    totalScore += pointsEarned;
                }
            }

            // Add detailed answer
            detailedAnswers.add(GameResultDto.DetailedAnswer.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .userAnswer(submission.getUserAnswer())
                    .correctAnswer(question.getCorrectAnswer())
                    .isCorrect(isCorrect)
                    .pointsEarned(pointsEarned)
                    .explanation(question.getExplanation())
                    .build());
        }

        // Ensure score is not negative
        if (totalScore < 0) {
            totalScore = 0;
        }

        int maxScore = quiz.getTotalPoints();

        // Create game result
        GameResult gameResult = GameResult.builder()
                .player(player)
                .quiz(quiz)
                .score(totalScore)
                .maxScore(maxScore)
                .correctAnswers(correctAnswers)
                .wrongAnswers(wrongAnswers)
                .totalQuestions(questions.size())
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .sessionId(request.getSessionId())
                .startedAt(LocalDateTime.now().minusSeconds(request.getTimeTakenSeconds() != null ? request.getTimeTakenSeconds() : 0))
                .completed(true)
                .build();

        // Convert detailed answers to JSON
        try {
            gameResult.setAnswersJson(objectMapper.writeValueAsString(detailedAnswers));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize detailed answers", e);
        }

        // Save result
        gameResult = gameResultRepository.save(gameResult);

        // Update player stats
        player.setLastPlayedAt(LocalDateTime.now());
        player.setGamesPlayed(player.getGamesPlayed() + 1);
        playerRepository.save(player);

        log.info("Game completed. Score: {}/{}, Correct: {}, Wrong: {}",
                totalScore, maxScore, correctAnswers, wrongAnswers);

        return GameResultDto.builder()
                .id(gameResult.getId())
                .playerId(player.getId())
                .playerNickname(player.getNickname())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .score(totalScore)
                .maxScore(maxScore)
                .correctAnswers(correctAnswers)
                .wrongAnswers(wrongAnswers)
                .totalQuestions(questions.size())
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .percentageScore(gameResult.getPercentageScore())
                .grade(gameResult.getGrade())
                .passed(gameResult.isPassed())
                .detailedAnswers(detailedAnswers)
                .startedAt(gameResult.getStartedAt())
                .completedAt(gameResult.getCompletedAt())
                .build();
    }

    /**
     * Get game result by ID
     */
    public GameResultDto getGameResult(Long resultId) {
        log.info("Fetching game result: {}", resultId);

        GameResult gameResult = gameResultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Game result not found with id: " + resultId));

        // Parse detailed answers
        List<GameResultDto.DetailedAnswer> detailedAnswers = new ArrayList<>();
        if (gameResult.getAnswersJson() != null) {
            try {
                detailedAnswers = objectMapper.readValue(
                        gameResult.getAnswersJson(),
                        new TypeReference<List<GameResultDto.DetailedAnswer>>() {}
                );
            } catch (JsonProcessingException e) {
                log.error("Failed to parse detailed answers", e);
            }
        }

        return GameResultDto.builder()
                .id(gameResult.getId())
                .playerId(gameResult.getPlayer().getId())
                .playerNickname(gameResult.getPlayer().getNickname())
                .quizId(gameResult.getQuiz().getId())
                .quizTitle(gameResult.getQuiz().getTitle())
                .score(gameResult.getScore())
                .maxScore(gameResult.getMaxScore())
                .correctAnswers(gameResult.getCorrectAnswers())
                .wrongAnswers(gameResult.getWrongAnswers())
                .totalQuestions(gameResult.getTotalQuestions())
                .timeTakenSeconds(gameResult.getTimeTakenSeconds())
                .percentageScore(gameResult.getPercentageScore())
                .grade(gameResult.getGrade())
                .passed(gameResult.isPassed())
                .detailedAnswers(detailedAnswers)
                .startedAt(gameResult.getStartedAt())
                .completedAt(gameResult.getCompletedAt())
                .build();
    }

    /**
     * Get or create player
     */
    private Player getOrCreatePlayer(StartGameRequest request) {
        // Try to find existing player by session ID
        if (request.getSessionId() != null) {
            Optional<Player> existingPlayer = playerRepository.findBySessionId(request.getSessionId());
            if (existingPlayer.isPresent()) {
                return existingPlayer.get();
            }
        }

        // Create new player
        Player player = Player.builder()
                .nickname(request.getPlayerNickname())
                .email(request.getEmail())
                .sessionId(request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString())
                .lastPlayedAt(LocalDateTime.now())
                .active(true)
                .build();

        return playerRepository.save(player);
    }

    /**
     * Shuffle answer options for questions
     */
    private List<QuestionDto> shuffleAnswerOptions(List<QuestionDto> questions) {
        return questions.stream()
                .map(this::shuffleQuestionAnswers)
                .collect(Collectors.toList());
    }

    /**
     * Shuffle answers for a single question
     */
    private QuestionDto shuffleQuestionAnswers(QuestionDto question) {
        // Only shuffle for question types that have options
        switch (question.getQuestionType()) {
            case SINGLE_CHOICE, MULTIPLE_CHOICE, DROPDOWN, TRUE_FALSE -> {
                try {
                    List<String> options = objectMapper.readValue(
                            question.getAnswerOptions(),
                            new TypeReference<List<String>>() {}
                    );

                    // Create index mapping before shuffle
                    Map<Integer, Integer> oldToNewIndex = new HashMap<>();
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < options.size(); i++) {
                        indexes.add(i);
                    }
                    Collections.shuffle(indexes);

                    for (int i = 0; i < indexes.size(); i++) {
                        oldToNewIndex.put(indexes.get(i), i);
                    }

                    // Shuffle options
                    List<String> shuffled = new ArrayList<>();
                    for (int index : indexes) {
                        shuffled.add(options.get(index));
                    }

                    question.setAnswerOptions(objectMapper.writeValueAsString(shuffled));

                    // Note: We don't update correctAnswer here because it's already set to null
                    // in the startGame method
                } catch (JsonProcessingException e) {
                    log.error("Failed to shuffle answer options", e);
                }
            }
        }

        return question;
    }

    /**
     * Get quiz statistics
     */
    public GameStatisticsDto getQuizStatistics(Long quizId) {
        log.info("Getting statistics for quiz: {}", quizId);

        Quiz quiz = quizExists(quizId);

        long totalAttempts = gameResultRepository.countByQuizIdAndCompletedTrue(quizId);
        long passedAttempts = gameResultRepository.countPassedAttempts(quizId);
        long failedAttempts = totalAttempts - passedAttempts;

        Double passRate = totalAttempts > 0 ? (double) passedAttempts / totalAttempts * 100 : 0.0;
        Double averageScore = gameResultRepository.getAverageScoreByQuizId(quizId);
        Integer highestScore = gameResultRepository.getHighestScoreByQuizId(quizId);
        Integer lowestScore = gameResultRepository.getLowestScoreByQuizId(quizId);

        return GameStatisticsDto.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalAttempts(totalAttempts)
                .passedAttempts(passedAttempts)
                .failedAttempts(failedAttempts)
                .passRate(passRate)
                .averageScore(averageScore != null ? averageScore : 0.0)
                .highestScore(highestScore != null ? highestScore : 0)
                .lowestScore(lowestScore != null ? lowestScore : 0)
                .maxScore(quiz.getTotalPoints())
                .build();
    }

    private Quiz quizExists(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizNotFoundException(quizId));
    }
}