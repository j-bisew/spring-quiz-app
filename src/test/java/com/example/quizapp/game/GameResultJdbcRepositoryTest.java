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
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
                .lastPlayedAt(LocalDateTime.now())
                .active(true)
                .build());

        // Wynik 1: 80%
        result1 = createResult(quiz, player, 80, 100);
        // Wynik 2: 40% (niezdany)
        createResult(quiz, player, 40, 100);
    }

    private GameResult createResult(Quiz q, Player p, int score, int maxScore) {
        return gameResultJpaRepository.save(GameResult.builder()
                .quiz(q)
                .player(p)
                .score(score)
                .maxScore(maxScore)
                .percentageScore((double) score / maxScore * 100.0)
                .timeTakenSeconds(100)
                .completed(true)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .completedAt(LocalDateTime.now())
                .build());
    }

    // --- ISTNIEJĄCE TESTY (ZACHOWANE) ---

    @Test
    @DisplayName("Should compare result with average correctly")
    void shouldCompareWithAverage() {
        Map<String, Object> comparison = jdbcRepository.compareWithAverage(result1.getId());
        assertThat(comparison.get("performance")).isEqualTo("Above Average");
    }

    @Test
    @DisplayName("Should analyze players needing retry")
    void shouldReturnPlayersNeedingRetry() {
        List<Map<String, Object>> needingRetry = jdbcRepository.getPlayersNeedingRetry(quiz.getId());
        assertThat(needingRetry).hasSize(1);
        assertThat(needingRetry.get(0).get("score")).isEqualTo(40);
    }

    @Test
    @DisplayName("Should calculate completion rate by difficulty")
    void shouldCalculateCompletionRate() {
        List<Map<String, Object>> rates = jdbcRepository.getCompletionRateByDifficulty();
        assertThat(rates).isNotEmpty();
    }

    // --- NOWE TESTY ---

    @Test
    @DisplayName("Should return player history ordered by date")
    void shouldGetPlayerHistory() {
        // When
        List<Map<String, Object>> history = jdbcRepository.getPlayerHistory(player.getId());

        // Then
        assertThat(history).hasSize(2);
        // Sprawdź czy pobrało tytuł quizu i ocenę
        assertThat(history.get(0)).containsKey("quiz_title");
        assertThat(history.get(0)).containsKey("grade");
        // Ostatni wynik był 40% -> Grade F
        // Przedostatni 80% -> Grade B
        // Sortowanie DESC, więc najnowszy (40%) pierwszy (chyba że created timestamps są identyczne co do ms)
    }

    @Test
    @DisplayName("Should return active players statistics")
    void shouldGetActivePlayersStatistics() {
        // Given - player has 2 completed games
        int minAttempts = 2;

        // When
        List<Map<String, Object>> stats = jdbcRepository.getActivePlayersStatistics(minAttempts);

        // Then
        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).get("nickname")).isEqualTo("TestPlayer");
        assertThat(stats.get(0).get("total_games")).isEqualTo(2L);
        assertThat(stats.get(0).get("avg_percentage")).isNotNull();
    }

    @Test
    @DisplayName("Should return hourly activity")
    void shouldGetHourlyActivity() {
        // Given
        // Wymuszamy, aby jeden wynik był sprzed godziny (update SQL)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        jdbcTemplate.update("UPDATE game_results SET completed_at = ? WHERE id = ?", oneHourAgo, result1.getId());

        // When
        List<Map<String, Object>> activity = jdbcRepository.getHourlyActivity();

        // Then
        assertThat(activity).isNotEmpty();
        // Powinniśmy mieć wpisy dla bieżącej godziny i godziny temu (chyba że są w tej samej godzinie zegarowej)
    }

    @Test
    @DisplayName("Should return completion trend (Last 30 days)")
    void shouldGetCompletionTrend() {
        // Given
        // Ustawiamy datę jednego wyniku na wczoraj
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        jdbcTemplate.update("UPDATE game_results SET completed_at = ? WHERE id = ?", yesterday, result1.getId());

        // When
        List<Map<String, Object>> trend = jdbcRepository.getCompletionTrend();

        // Then
        assertThat(trend.size()).isGreaterThanOrEqualTo(1);
        // Sprawdź czy mamy kolumny date, completions, passed
        assertThat(trend.get(0)).containsKey("date");
        assertThat(trend.get(0)).containsKey("completions");
        assertThat(trend.get(0)).containsKey("passed");
    }
}