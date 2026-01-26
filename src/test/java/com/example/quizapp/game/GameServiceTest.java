package com.example.quizapp.game;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private QuizRepository quizRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private GameResultRepository gameResultRepository;
    @Mock
    private QuestionService questionService;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GameService gameService;

    // --- START GAME TESTS ---

    @Test
    @DisplayName("StartGame: Should start game successfully (Happy Path)")
    void shouldStartGameSuccessfully() {
        // Given
        Long quizId = 1L;
        StartGameRequest request = new StartGameRequest(quizId, "PlayerOne", null, null);

        Quiz quiz = Quiz.builder()
                .id(quizId)
                .active(true)
                .questions(new ArrayList<>(List.of(new Question(), new Question())))
                .build();

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> {
            Player p = i.getArgument(0);
            p.setId(100L);
            return p;
        });
        when(questionMapper.toDto(any())).thenReturn(new QuestionDto());

        // When
        StartGameResponse response = gameService.startGame(request);

        // Then
        assertThat(response.getSessionId()).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quizId);
        assertThat(response.getTotalQuestions()).isEqualTo(2);
    }

    @Test
    @DisplayName("StartGame: Should throw exception when quiz inactive")
    void shouldThrowWhenQuizInactive() {
        // Given
        Long quizId = 1L;
        Quiz quiz = Quiz.builder().id(quizId).active(false).build();
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        // When & Then
        assertThatThrownBy(() -> gameService.startGame(new StartGameRequest(quizId, "Nick", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quiz is not active");
    }

    @Test
    @DisplayName("StartGame: Should throw exception when quiz has no active questions")
    void shouldThrowWhenNoActiveQuestions() {
        // Given
        Long quizId = 1L;
        Quiz quiz = Quiz.builder().id(quizId).active(true).questions(Collections.emptyList()).build();
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        // When & Then
        assertThatThrownBy(() -> gameService.startGame(new StartGameRequest(quizId, "Nick", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quiz has no active questions");
    }

    // --- SUBMIT ANSWERS TESTS ---

    @Test
    @DisplayName("SubmitAnswers: Should calculate score correctly")
    void shouldCalculateScoreCorrectly() {
        // Given
        Long quizId = 1L;
        Long playerId = 1L;
        String sessionId = "sess-123";

        Quiz quiz = Quiz.builder().id(quizId).build();
        // Setup questions via setter or builder if possible, ensuring list is mutable if needed
        Question q1 = Question.builder().id(10L).points(5).active(true).build();
        Question q2 = Question.builder().id(11L).points(5).active(true).build();
        quiz.setQuestions(List.of(q1, q2));

        Player player = Player.builder().id(playerId).nickname("Test").gamesPlayed(0).build();

        SubmitAnswersRequest request = SubmitAnswersRequest.builder()
                .quizId(quizId)
                .playerId(playerId)
                .sessionId(sessionId)
                .answers(List.of(
                        new SubmitAnswersRequest.AnswerSubmission(10L, "Correct"),
                        new SubmitAnswersRequest.AnswerSubmission(11L, "Wrong")
                ))
                .build();

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(questionService.validateAnswer(10L, "Correct")).thenReturn(true);
        when(questionService.validateAnswer(11L, "Wrong")).thenReturn(false);
        when(gameResultRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GameResultDto result = gameService.submitAnswers(request);

        // Then
        assertThat(result.getScore()).isEqualTo(5); // 5 for correct, 0 for wrong
        assertThat(result.getMaxScore()).isEqualTo(10);
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
        assertThat(result.getWrongAnswers()).isEqualTo(1);

        verify(playerRepository).save(player); // Should update stats
        assertThat(player.getGamesPlayed()).isEqualTo(1);
    }

    @Test
    @DisplayName("SubmitAnswers: Should apply negative points if enabled")
    void shouldApplyNegativePoints() {
        // Given
        Long quizId = 1L;
        Quiz quiz = Quiz.builder().id(quizId).negativePointsEnabled(true).build();

        Question q1 = Question.builder()
                .id(10L)
                .points(10)
                .negativePoints(5)
                .active(true)
                .build();
        quiz.setQuestions(List.of(q1));

        SubmitAnswersRequest request = SubmitAnswersRequest.builder()
                .quizId(quizId)
                .playerId(1L)
                .sessionId("sess")
                .answers(List.of(new SubmitAnswersRequest.AnswerSubmission(10L, "Wrong")))
                .build();

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(new Player()));
        when(questionService.validateAnswer(10L, "Wrong")).thenReturn(false);
        when(gameResultRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GameResultDto result = gameService.submitAnswers(request);

        // Then
        assertThat(result.getScore()).isZero(); // Cannot be negative total, but let's check logic
        // Logic inside service: if (totalScore < 0) totalScore = 0;
        // In loop: pointsEarned = -5. totalScore becomes -5.
        // Final check sets it to 0.
        // Let's verify via detailed answers or correct/wrong count if possible
        assertThat(result.getWrongAnswers()).isEqualTo(1);
        // Score is floored at 0
        assertThat(result.getScore()).isZero();
    }

    // --- GET RESULT TESTS ---

    @Test
    @DisplayName("GetGameResult: Should return DTO successfully")
    void shouldGetGameResultSuccessfully() throws JsonProcessingException {
        // Given
        Long resultId = 100L;
        Player player = Player.builder().id(1L).nickname("P1").build();
        Quiz quiz = Quiz.builder().id(2L).title("Q1").build();

        GameResult gameResult = GameResult.builder()
                .id(resultId)
                .player(player)
                .quiz(quiz)
                .score(80)
                .answersJson("[{\"questionId\":1}]") // Mock JSON
                .build();

        when(gameResultRepository.findById(resultId)).thenReturn(Optional.of(gameResult));
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of(new GameResultDto.DetailedAnswer()));

        // When
        GameResultDto resultDto = gameService.getGameResult(resultId);

        // Then
        assertThat(resultDto.getId()).isEqualTo(resultId);
        assertThat(resultDto.getPlayerNickname()).isEqualTo("P1");
        verify(objectMapper).readValue(eq("[{\"questionId\":1}]"), any(TypeReference.class));
    }

    @Test
    @DisplayName("GetGameResult: Should throw exception when not found")
    void shouldThrowWhenResultNotFound() {
        when(gameResultRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGameResult(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- STATISTICS TESTS ---

    @Test
    @DisplayName("GetQuizStatistics: Should return calculated stats")
    void shouldCalculateQuizStatistics() {
        // Given
        Long quizId = 1L;
        Quiz quiz = Quiz.builder().id(quizId).title("Stat Quiz").build();
        // Setup quiz mocks
        Question q1 = Question.builder().points(10).build();
        quiz.setQuestions(List.of(q1)); // Total points 10

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(gameResultRepository.countByQuizIdAndCompletedTrue(quizId)).thenReturn(10L);
        when(gameResultRepository.countPassedAttempts(quizId)).thenReturn(8L);
        when(gameResultRepository.getAverageScoreByQuizId(quizId)).thenReturn(7.5);
        when(gameResultRepository.getHighestScoreByQuizId(quizId)).thenReturn(10);
        when(gameResultRepository.getLowestScoreByQuizId(quizId)).thenReturn(2);

        // When
        GameStatisticsDto stats = gameService.getQuizStatistics(quizId);

        // Then
        assertThat(stats.getTotalAttempts()).isEqualTo(10L);
        assertThat(stats.getPassedAttempts()).isEqualTo(8L);
        assertThat(stats.getFailedAttempts()).isEqualTo(2L); // 10 - 8
        assertThat(stats.getPassRate()).isEqualTo(80.0); // 8/10 * 100
        assertThat(stats.getMaxScore()).isEqualTo(10);
    }
}