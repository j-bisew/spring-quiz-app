package com.example.quizapp.game;

import com.example.quizapp.player.Player;
import com.example.quizapp.player.PlayerRepository;
import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("GameResultRepository Tests")
class GameResultRepositoryTest {

    @Autowired
    private GameResultRepository gameResultRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private Quiz quiz1;
    private Quiz quiz2;
    private Player player1;
    private Player player2;
    private GameResult result1;
    private GameResult result2;
    private GameResult result3;
    private GameResult incompleteResult;

    @BeforeEach
    void setUp() {
        // Clear repositories
        gameResultRepository.deleteAll();
        playerRepository.deleteAll();
        quizRepository.deleteAll();

        // Create test quizzes
        quiz1 = Quiz.builder()
                .title("Java Quiz")
                .description("Test quiz")
                .active(true)
                .createdBy("testuser")
                .build();
        quiz1 = quizRepository.save(quiz1);

        quiz2 = Quiz.builder()
                .title("Spring Quiz")
                .description("Test quiz 2")
                .active(true)
                .createdBy("testuser")
                .build();
        quiz2 = quizRepository.save(quiz2);

        // Create test players
        player1 = Player.builder()
                .nickname("Alice")
                .sessionId(UUID.randomUUID().toString())
                .build();
        player1 = playerRepository.save(player1);

        player2 = Player.builder()
                .nickname("Bob")
                .sessionId(UUID.randomUUID().toString())
                .build();
        player2 = playerRepository.save(player2);

        // Create test game results
        result1 = GameResult.builder()
                .quiz(quiz1)
                .player(player1)
                .score(90)
                .maxScore(100)
                .percentageScore(90.0)
                .timeTakenSeconds(300)
                .sessionId(UUID.randomUUID().toString())
                .completed(true)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();

        result2 = GameResult.builder()
                .quiz(quiz1)
                .player(player2)
                .score(75)
                .maxScore(100)
                .percentageScore(75.0)
                .timeTakenSeconds(400)
                .sessionId(UUID.randomUUID().toString())
                .completed(true)
                .completedAt(LocalDateTime.now().minusDays(2))
                .build();

        result3 = GameResult.builder()
                .quiz(quiz2)
                .player(player1)
                .score(60)
                .maxScore(100)
                .percentageScore(60.0)
                .timeTakenSeconds(350)
                .sessionId(UUID.randomUUID().toString())
                .completed(true)
                .completedAt(LocalDateTime.now().minusDays(3))
                .build();

        incompleteResult = GameResult.builder()
                .quiz(quiz1)
                .player(player1)
                .score(0)
                .maxScore(100)
                .sessionId(UUID.randomUUID().toString())
                .completed(false)
                .startedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("Should save game result successfully")
    void shouldSaveGameResult() {
        // When
        GameResult saved = gameResultRepository.save(result1);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getScore()).isEqualTo(90);
        assertThat(saved.getQuiz()).isEqualTo(quiz1);
        assertThat(saved.getPlayer()).isEqualTo(player1);
        assertThat(saved.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Should save incomplete game result")
    void shouldSaveIncompleteGameResult() {
        // When
        GameResult saved = gameResultRepository.save(incompleteResult);

        // Then
        assertThat(saved.isCompleted()).isFalse();
        assertThat(saved.getCompletedAt()).isNull();
    }

    // ==================== READ Tests ====================

    @Test
    @DisplayName("Should find game result by ID")
    void shouldFindGameResultById() {
        // Given
        GameResult saved = gameResultRepository.save(result1);

        // When
        Optional<GameResult> found = gameResultRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualTo(90);
    }

    @Test
    @DisplayName("Should find all game results")
    void shouldFindAllGameResults() {
        // Given
        gameResultRepository.save(result1);
        gameResultRepository.save(result2);
        gameResultRepository.save(result3);

        // When
        List<GameResult> all = gameResultRepository.findAll();

        // Then
        assertThat(all).hasSize(3);
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("Should update game result score")
    void shouldUpdateGameResultScore() {
        // Given
        GameResult saved = gameResultRepository.save(result1);

        // When
        saved.setScore(95);
        GameResult updated = gameResultRepository.save(saved);

        // Then
        assertThat(updated.getScore()).isEqualTo(95);
    }

    @Test
    @DisplayName("Should mark game as completed")
    void shouldMarkGameAsCompleted() {
        // Given
        GameResult saved = gameResultRepository.save(incompleteResult);

        // When
        saved.setCompleted(true);
        saved.setCompletedAt(LocalDateTime.now());
        saved.setScore(80);
        saved.setPercentageScore(80.0);
        GameResult updated = gameResultRepository.save(saved);

        // Then
        assertThat(updated.isCompleted()).isTrue();
        assertThat(updated.getCompletedAt()).isNotNull();
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("Should delete game result by ID")
    void shouldDeleteGameResultById() {
        // Given
        GameResult saved = gameResultRepository.save(result1);
        Long id = saved.getId();

        // When
        gameResultRepository.deleteById(id);

        // Then
        assertThat(gameResultRepository.findById(id)).isEmpty();
    }

    // ==================== Custom Query Tests ====================

    @Test
    @DisplayName("Should find results by quiz ID")
    void shouldFindResultsByQuizId() {
        // Given
        gameResultRepository.save(result1);
        gameResultRepository.save(result2);
        gameResultRepository.save(result3);
        gameResultRepository.save(incompleteResult);

        // When
        List<GameResult> results = gameResultRepository.findByQuizIdAndCompletedTrue(quiz1.getId());

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(GameResult::getScore)
                .containsExactlyInAnyOrder(90, 75);
    }

    @Test
    @DisplayName("Should find results by quiz ID with pagination")
    void shouldFindResultsByQuizIdWithPagination() {
        // Given
        gameResultRepository.save(result1);
        gameResultRepository.save(result2);
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<GameResult> page = gameResultRepository.findByQuizIdAndCompletedTrue(quiz1.getId(), pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find results by player ID")
    void shouldFindResultsByPlayerId() {
        // Given
        gameResultRepository.save(result1); // player1, quiz1
        gameResultRepository.save(result3); // player1, quiz2
        gameResultRepository.save(result2); // player2, quiz1

        // When
        List<GameResult> results = gameResultRepository.findByPlayerIdAndCompletedTrue(player1.getId());

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(GameResult::getScore)
                .containsExactlyInAnyOrder(90, 60);
    }

    @Test
    @DisplayName("Should find results by quiz and player")
    void shouldFindResultsByQuizAndPlayer() {
        // Given
        gameResultRepository.save(result1); // player1, quiz1
        gameResultRepository.save(result3); // player1, quiz2

        // When
        List<GameResult> results = gameResultRepository.findByQuizIdAndPlayerIdAndCompletedTrue(
                quiz1.getId(), player1.getId());

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getScore()).isEqualTo(90);
    }

    @Test
    @DisplayName("Should find top scores by quiz ID")
    void shouldFindTopScoresByQuizId() {
        // Given
        gameResultRepository.save(result1); // 90 points, 300s
        gameResultRepository.save(result2); // 75 points, 400s
        Pageable pageable = PageRequest.of(0, 10);

        // When
        List<GameResult> topScores = gameResultRepository.findTopScoresByQuizId(quiz1.getId(), pageable);

        // Then
        assertThat(topScores).hasSize(2);
        // Ordered by score DESC, time ASC
        assertThat(topScores.get(0).getScore()).isEqualTo(90);
        assertThat(topScores.get(1).getScore()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should order by time when scores are equal")
    void shouldOrderByTimeWhenScoresEqual() {
        // Given
        GameResult fast = GameResult.builder()
                .quiz(quiz1)
                .player(player1)
                .score(80)
                .maxScore(100)
                .percentageScore(80.0)
                .timeTakenSeconds(200)
                .sessionId(UUID.randomUUID().toString())
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        GameResult slow = GameResult.builder()
                .quiz(quiz1)
                .player(player2)
                .score(80)
                .maxScore(100)
                .percentageScore(80.0)
                .timeTakenSeconds(500)
                .sessionId(UUID.randomUUID().toString())
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        gameResultRepository.save(slow);
        gameResultRepository.save(fast);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        List<GameResult> topScores = gameResultRepository.findTopScoresByQuizId(quiz1.getId(), pageable);

        // Then
        assertThat(topScores.get(0).getTimeTakenSeconds()).isEqualTo(200);
        assertThat(topScores.get(1).getTimeTakenSeconds()).isEqualTo(500);
    }

    @Test
    @DisplayName("Should find result by session ID")
    void shouldFindResultBySessionId() {
        // Given
        GameResult saved = gameResultRepository.save(result1);

        // When
        Optional<GameResult> found = gameResultRepository.findBySessionId(saved.getSessionId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualTo(90);
    }

    @Test
    @DisplayName("Should count results by quiz ID")
    void shouldCountResultsByQuizId() {
        // Given
        gameResultRepository.save(result1);
        gameResultRepository.save(result2);
        gameResultRepository.save(incompleteResult);

        // When
        long count = gameResultRepository.countByQuizIdAndCompletedTrue(quiz1.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count results by player ID")
    void shouldCountResultsByPlayerId() {
        // Given
        gameResultRepository.save(result1); // player1
        gameResultRepository.save(result3); // player1

        // When
        long count = gameResultRepository.countByPlayerIdAndCompletedTrue(player1.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get average score by quiz ID")
    void shouldGetAverageScoreByQuizId() {
        // Given
        gameResultRepository.save(result1); // 90
        gameResultRepository.save(result2); // 75

        // When
        Double avgScore = gameResultRepository.getAverageScoreByQuizId(quiz1.getId());

        // Then
        assertThat(avgScore).isEqualTo(82.5);
    }

    @Test
    @DisplayName("Should get highest score by quiz ID")
    void shouldGetHighestScoreByQuizId() {
        // Given
        gameResultRepository.save(result1); // 90
        gameResultRepository.save(result2); // 75

        // When
        Integer highest = gameResultRepository.getHighestScoreByQuizId(quiz1.getId());

        // Then
        assertThat(highest).isEqualTo(90);
    }

    @Test
    @DisplayName("Should get lowest score by quiz ID")
    void shouldGetLowestScoreByQuizId() {
        // Given
        gameResultRepository.save(result1); // 90
        gameResultRepository.save(result2); // 75

        // When
        Integer lowest = gameResultRepository.getLowestScoreByQuizId(quiz1.getId());

        // Then
        assertThat(lowest).isEqualTo(75);
    }

    @Test
    @DisplayName("Should find recent results")
    void shouldFindRecentResults() {
        // Given
        gameResultRepository.save(result1); // 1 day ago
        gameResultRepository.save(result2); // 2 days ago
        gameResultRepository.save(result3); // 3 days ago
        Pageable pageable = PageRequest.of(0, 2);

        // When
        List<GameResult> recent = gameResultRepository.findRecentResults(pageable);

        // Then
        assertThat(recent).hasSize(2);
        // Should be ordered by completedAt DESC
        assertThat(recent.get(0).getCompletedAt()).isAfter(recent.get(1).getCompletedAt());
    }

    @Test
    @DisplayName("Should find results by date range")
    void shouldFindResultsByDateRange() {
        // Given
        gameResultRepository.save(result1); // 1 day ago
        gameResultRepository.save(result2); // 2 days ago
        gameResultRepository.save(result3); // 3 days ago

        LocalDateTime start = LocalDateTime.now().minusDays(2).minusHours(1);
        LocalDateTime end = LocalDateTime.now();

        // When
        List<GameResult> results = gameResultRepository.findByDateRange(start, end);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(GameResult::getScore)
                .containsExactlyInAnyOrder(90, 75);
    }

    @Test
    @DisplayName("Should count passed attempts")
    void shouldCountPassedAttempts() {
        // Given
        gameResultRepository.save(result1); // 90% - passed
        gameResultRepository.save(result2); // 75% - passed
        gameResultRepository.save(result3); // 60% - passed

        GameResult failedResult = GameResult.builder()
                .quiz(quiz1)
                .player(player1)
                .score(40)
                .maxScore(100)
                .percentageScore(40.0)
                .timeTakenSeconds(200)
                .sessionId(UUID.randomUUID().toString())
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();
        gameResultRepository.save(failedResult); // 40% - failed

        // When
        long passedCount = gameResultRepository.countPassedAttempts(quiz1.getId());

        // Then
        assertThat(passedCount).isEqualTo(2); // Only result1 and result2 (both >= 50%)
    }

    @Test
    @DisplayName("Should get player best score")
    void shouldGetPlayerBestScore() {
        // Given
        GameResult secondAttempt = GameResult.builder()
                .quiz(quiz1)
                .player(player1)
                .score(95)
                .maxScore(100)
                .percentageScore(95.0)
                .timeTakenSeconds(280)
                .sessionId(UUID.randomUUID().toString())
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        gameResultRepository.save(result1); // player1, quiz1, score 90
        gameResultRepository.save(secondAttempt); // player1, quiz1, score 95

        // When
        Integer bestScore = gameResultRepository.getPlayerBestScore(quiz1.getId(), player1.getId());

        // Then
        assertThat(bestScore).isEqualTo(95);
    }

    @Test
    @DisplayName("Should count all completed games")
    void shouldCountAllCompletedGames() {
        // Given
        gameResultRepository.save(result1);
        gameResultRepository.save(result2);
        gameResultRepository.save(result3);
        gameResultRepository.save(incompleteResult);

        // When
        long count = gameResultRepository.countByCompletedTrue();

        // Then
        assertThat(count).isEqualTo(3);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle quiz with no results")
    void shouldHandleQuizWithNoResults() {
        // When
        List<GameResult> results = gameResultRepository.findByQuizIdAndCompletedTrue(quiz2.getId());

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should handle null average for empty quiz")
    void shouldHandleNullAverageForEmptyQuiz() {
        // When
        Double avg = gameResultRepository.getAverageScoreByQuizId(quiz2.getId());

        // Then
        assertThat(avg).isNull();
    }

    @Test
    @DisplayName("Should handle zero time taken")
    void shouldHandleZeroTimeTaken() {
        // Given
        GameResult instant = GameResult.builder()
                .quiz(quiz1)
                .player(player1)
                .score(100)
                .maxScore(100)
                .percentageScore(100.0)
                .timeTakenSeconds(0)
                .sessionId(UUID.randomUUID().toString())
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        // When
        GameResult saved = gameResultRepository.save(instant);

        // Then
        assertThat(saved.getTimeTakenSeconds()).isZero();
    }
}