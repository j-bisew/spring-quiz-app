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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(GameResultJdbcRepository.class)
class GameResultJdbcRepositoryTest {

    @Autowired
    private GameResultJdbcRepository jdbcRepository;

    @Autowired
    private GameResultRepository gameResultJpaRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private Quiz quiz;
    private Player player;
    private GameResult result1;

    @BeforeEach
    void setUp() {
        quiz = quizRepository.save(Quiz.builder()
                .title("Test Quiz")
                .active(true)
                .timeLimitMinutes(10)
                .build());

        player = playerRepository.save(Player.builder()
                .nickname("TestPlayer")
                .build());

        // Tworzymy 3 wyniki, aby mieć dane do statystyk
        // Wynik 1: 80/100 (80%)
        result1 = createResult(80, 100, 120);
        // Wynik 2: 40/100 (40%) - niezdany
        createResult(40, 100, 100);
        // Wynik 3: 95/100 (95%)
        createResult(95, 100, 90);
    }

    private GameResult createResult(int score, int maxScore, int time) {
        return gameResultJpaRepository.save(GameResult.builder()
                .quiz(quiz)
                .player(player)
                .score(score)
                .maxScore(maxScore)
                .percentageScore((double) score / maxScore * 100.0) // Ważne dla zapytań SQL
                .timeTakenSeconds(time)
                .completed(true)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .completedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("Should compare result with average correctly")
    void shouldCompareWithAverage() {
        // Given:
        // Średnia z (80, 40, 95) = 71.66
        // Result1 (80) jest powyżej średniej.

        // When
        Map<String, Object> comparison = jdbcRepository.compareWithAverage(result1.getId());

        // Then
        assertThat(comparison).isNotEmpty();
        assertThat(comparison.get("player_score")).isEqualTo(80);
        assertThat(comparison.get("performance")).isEqualTo("Above Average");
    }

    @Test
    @DisplayName("Should analyze players needing retry")
    void shouldReturnPlayersNeedingRetry() {
        // When (szukamy graczy z wynikiem < 50%)
        List<Map<String, Object>> needingRetry = jdbcRepository.getPlayersNeedingRetry(quiz.getId());

        // Then
        assertThat(needingRetry).hasSize(1);
        assertThat(needingRetry.get(0).get("score")).isEqualTo(40);
        assertThat(needingRetry.get(0).get("nickname")).isEqualTo("TestPlayer");
    }

    @Test
    @DisplayName("Should calculate completion rate by difficulty")
    void shouldCalculateCompletionRate() {
        // When
        List<Map<String, Object>> rates = jdbcRepository.getCompletionRateByDifficulty();

        // Then
        assertThat(rates).isNotEmpty();

        // Szukamy statystyk dla poziomu "Easy" (bo timeLimit <= 10)
        Map<String, Object> easyStats = rates.stream()
                .filter(m -> "Easy".equals(m.get("difficulty")))
                .findFirst()
                .orElseThrow();

        assertThat(easyStats.get("total_attempts")).isEqualTo(3L);
        // 2 wyniki >= 50% (80, 95), 1 poniżej (40)
        assertThat(easyStats.get("passed")).isEqualTo(2L);
    }
}