package com.example.quizapp.ranking;

import com.example.quizapp.common.exception.QuizNotFoundException;
import com.example.quizapp.common.exception.ResourceNotFoundException;
import com.example.quizapp.game.GameResult;
import com.example.quizapp.game.GameResultRepository;
import com.example.quizapp.player.Player;
import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("RankingService Tests")
class RankingServiceTest {

    @Mock
    private GameResultRepository gameResultRepository;

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private RankingService rankingService;

    private Quiz quiz;
    private Player player1;
    private Player player2;
    private GameResult result1;
    private GameResult result2;

    @BeforeEach
    void setUp() {
        quiz = Quiz.builder()
                .id(1L)
                .title("Java Quiz")
                .build();

        player1 = Player.builder()
                .id(100L)
                .nickname("PlayerOne")
                .gamesPlayed(5)
                .build();

        player2 = Player.builder()
                .id(200L)
                .nickname("PlayerTwo")
                .gamesPlayed(3)
                .build();

        // Result 1: 100 points, 120 seconds
        result1 = GameResult.builder()
                .id(10L)
                .quiz(quiz)
                .player(player1)
                .score(100)
                .maxScore(100)
                .timeTakenSeconds(120)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();

        // Result 2: 90 points, 100 seconds
        result2 = GameResult.builder()
                .id(11L)
                .quiz(quiz)
                .player(player2)
                .score(90)
                .maxScore(100)
                .timeTakenSeconds(100)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();
    }

    // ==================== GET TOP RANKINGS Tests ====================

    @Test
    @DisplayName("Should return top rankings")
    void shouldReturnTopRankings() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(gameResultRepository.findTopScoresByQuizId(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(result1, result2));

        // When
        List<RankingDto> rankings = rankingService.getTopRankings(1L, 10);

        // Then
        assertThat(rankings).hasSize(2);
        assertThat(rankings.get(0).getPlayerNickname()).isEqualTo("PlayerOne");
        assertThat(rankings.get(1).getPlayerNickname()).isEqualTo("PlayerTwo");
    }

    @Test
    @DisplayName("Should throw exception when getting top rankings for non-existent quiz")
    void shouldThrowWhenQuizNotFoundForTopRankings() {
        // Given
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rankingService.getTopRankings(999L, 10))
                .isInstanceOf(QuizNotFoundException.class);
    }

    // ==================== FULL LEADERBOARD Tests (Sorting Logic) ====================

    @Test
    @DisplayName("Should sort leaderboard by score (DESC) and time (ASC)")
    void shouldSortLeaderboardCorrectly() {
        // Given
        // Case:
        // P1: 100 pkt, 120s
        // P2: 100 pkt, 90s  <- should be first (faster)
        // P3: 80 pkt,  50s  <- should be last (lowest score)

        GameResult p1Result = result1; // 100, 120s

        GameResult p2Result = GameResult.builder()
                .id(12L)
                .quiz(quiz)
                .player(player2)
                .score(100)
                .timeTakenSeconds(90) // Faster than P1
                .completed(true)
                .build();

        GameResult p3Result = GameResult.builder()
                .id(13L)
                .quiz(quiz)
                .player(Player.builder().nickname("Slow").build())
                .score(80)
                .timeTakenSeconds(50)
                .completed(true)
                .build();

        // Repository might return unsorted
        List<GameResult> unsorted = new ArrayList<>(List.of(p1Result, p3Result, p2Result));

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(gameResultRepository.findByQuizIdAndCompletedTrue(1L)).thenReturn(unsorted);

        // When
        List<RankingDto> rankings = rankingService.getFullLeaderboard(1L);

        // Then
        assertThat(rankings).hasSize(3);

        // 1st: P2 (100 pts, 90s)
        assertThat(rankings.get(0).getScore()).isEqualTo(100);
        assertThat(rankings.get(0).getTimeTakenSeconds()).isEqualTo(90);

        // 2nd: P1 (100 pts, 120s)
        assertThat(rankings.get(1).getScore()).isEqualTo(100);
        assertThat(rankings.get(1).getTimeTakenSeconds()).isEqualTo(120);

        // 3rd: P3 (80 pts)
        assertThat(rankings.get(2).getScore()).isEqualTo(80);
    }

    // ==================== PLAYER RANKING Tests ====================

    @Test
    @DisplayName("Should calculate player ranking position correctly")
    void shouldGetPlayerRankingPosition() {
        // Given
        // P1: 100 pts
        // P2: 90 pts
        // We look for P2 (should be 2nd)

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(gameResultRepository.findByQuizIdAndCompletedTrue(1L))
                .thenReturn(new ArrayList<>(List.of(result1, result2))); // Service will sort this list

        // When
        RankingPositionDto result = rankingService.getPlayerRanking(1L, 200L); // player2 ID

        // Then
        assertThat(result.getPosition()).isEqualTo(2);
        assertThat(result.getTotalPlayers()).isEqualTo(2);
        assertThat(result.getPlayerNickname()).isEqualTo("PlayerTwo");
        assertThat(result.getScore()).isEqualTo(90);
    }

    @Test
    @DisplayName("Should throw exception if player has no results in quiz")
    void shouldThrowWhenPlayerHasNoResults() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        // POPRAWKA: Używamy new ArrayList<>(), aby lista była mutowalna i metoda sort() w serwisie nie rzuciła błędu
        List<GameResult> results = new ArrayList<>(List.of(result1));

        when(gameResultRepository.findByQuizIdAndCompletedTrue(1L))
                .thenReturn(results);

        // When & Then
        assertThatThrownBy(() -> rankingService.getPlayerRanking(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No results found for player");
    }

    // ==================== GLOBAL RANKING Tests ====================

    @Test
    @DisplayName("Should return global rankings")
    void shouldReturnGlobalRankings() {
        // Given
        when(gameResultRepository.findRecentResults(any(Pageable.class)))
                .thenReturn(List.of(result1));

        // When
        List<GlobalRankingDto> global = rankingService.getGlobalRankings(5);

        // Then
        assertThat(global).hasSize(1);
        assertThat(global.get(0).getPlayerNickname()).isEqualTo("PlayerOne");
        assertThat(global.get(0).getTotalGamesPlayed()).isEqualTo(5);
    }
}