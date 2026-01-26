package com.example.quizapp.game;

import com.example.quizapp.player.Player;
import com.example.quizapp.player.PlayerRepository;
import com.example.quizapp.question.Question;
import com.example.quizapp.question.QuestionService;
import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private GameService gameService;

    @Test
    @DisplayName("StartGame: Should start game successfully (Happy Path)")
    void shouldStartGameSuccessfully() {
        // Given
        Long quizId = 1L;
        StartGameRequest request = new StartGameRequest(quizId, "PlayerOne", null, null);

        Quiz quiz = Quiz.builder()
                .id(quizId)
                .active(true)
                .questions(new ArrayList<>(List.of(new Question(), new Question()))) // Lista modyfikowalna
                .build();

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> {
            Player p = i.getArgument(0);
            p.setId(100L);
            return p;
        });

        // When
        StartGameResponse response = gameService.startGame(request);

        // Then
        assertThat(response.getSessionId()).isNotNull();
        assertThat(response.getQuizId()).isEqualTo(quizId);
        assertThat(response.getTotalQuestions()).isEqualTo(2);

        verify(quizRepository).findById(quizId);
        verify(playerRepository).save(any(Player.class));
    }

    @Test
    @DisplayName("StartGame: Should throw exception when quiz inactive (Error Case)")
    void shouldThrowWhenQuizInactive() {
        // Given
        Long quizId = 1L;
        Quiz quiz = Quiz.builder().id(quizId).active(false).build();
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                gameService.startGame(new StartGameRequest(quizId, "Nick", null, null))
        );
    }

    @Test
    @DisplayName("SubmitAnswers: Should calculate score correctly")
    void shouldCalculateScoreCorrectly() {
        // Given
        Long quizId = 1L;
        Long playerId = 1L;
        String sessionId = "sess-123";

        // POPRAWKA: Usuwamy .totalPoints(10), bo to metoda obliczana dynamicznie.
        // Zamiast tego dodajemy pytania z punktami do listy.
        Quiz quiz = Quiz.builder().id(quizId).build();

        Question q1 = Question.builder().id(10L).points(5).active(true).build();
        Question q2 = Question.builder().id(11L).points(5).active(true).build();

        // Ważne: Ustawiamy listę pytań w obiekcie Quiz, aby quiz.getTotalPoints() zwróciło 10
        quiz.setQuestions(new ArrayList<>(List.of(q1, q2)));

        Player player = Player.builder().id(playerId).build();

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

        // Mockowanie walidacji odpowiedzi
        when(questionService.validateAnswer(10L, "Correct")).thenReturn(true);
        when(questionService.validateAnswer(11L, "Wrong")).thenReturn(false);

        // Mockujemy zapis wyniku i zwracamy to co przyszło
        when(gameResultRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GameResultDto result = gameService.submitAnswers(request);

        // Then
        assertThat(result.getScore()).isEqualTo(5); // 5 pkt za poprawne, 0 za błędne
        assertThat(result.getMaxScore()).isEqualTo(10); // 5 + 5 z pytań w quizie
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
        assertThat(result.getWrongAnswers()).isEqualTo(1);

        verify(gameResultRepository).save(any());
    }
}