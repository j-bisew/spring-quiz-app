package com.example.quizapp.ranking;

import com.example.quizapp.common.exception.ResourceNotFoundException;
import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient; // Import do lenient()
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingExportService Tests")
class RankingExportServiceTest {

    @Mock
    private RankingService rankingService;

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private RankingExportService rankingExportService;

    private Quiz quiz;
    private RankingDto rankingDto;

    @BeforeEach
    void setUp() {
        // 1. Tworzymy mocka
        quiz = mock(Quiz.class);

        // 2. Używamy lenient(), aby Mockito nie zgłaszał błędu UnnecessaryStubbingException
        // w testach, które rzucają wyjątek zanim te metody zostaną wywołane.
        lenient().when(quiz.getTitle()).thenReturn("Java Quiz");
        lenient().when(quiz.getTotalPoints()).thenReturn(100);

        rankingDto = RankingDto.builder()
                .playerNickname("TestPlayer")
                .score(90)
                .maxScore(100)
                .percentageScore(90.0)
                .correctAnswers(9)
                .wrongAnswers(1)
                .totalQuestions(10)
                .timeTakenSeconds(120)
                .grade("A")
                .completedAt(LocalDateTime.now())
                .quizTitle("Java Quiz")
                .build();
    }

    // ==================== CSV EXPORT Tests ====================

    @Test
    @DisplayName("Should export rankings to CSV successfully")
    void shouldExportToCsv() throws IOException {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(rankingService.getFullLeaderboard(1L)).thenReturn(List.of(rankingDto));

        // When
        Resource resource = rankingExportService.exportToCsv(1L);

        // Then
        assertThat(resource).isNotNull();
        assertThat(resource.contentLength()).isGreaterThan(0);

        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Sprawdzamy czy zawiera nagłówki
        assertThat(content).contains("Position", "Player", "Score", "Percentage");

        // 3. Generujemy oczekiwany format procentów zgodnie z ustawieniami systemu (kropka vs przecinek)
        String expectedPercentage = String.format("%.2f%%", 90.0); // Np. "90,00%" w PL, "90.00%" w US

        // Sprawdzamy czy zawiera dane gracza
        assertThat(content).contains("TestPlayer", "90", expectedPercentage);
    }

    @Test
    @DisplayName("Should throw exception when exporting CSV for non-existent quiz")
    void shouldThrowWhenExportingCsvForUnknownQuiz() {
        // Given
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rankingExportService.exportToCsv(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found");
    }

    // ==================== PDF EXPORT Tests ====================

    @Test
    @DisplayName("Should export rankings to PDF successfully")
    void shouldExportToPdf() throws IOException {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(rankingService.getFullLeaderboard(1L)).thenReturn(List.of(rankingDto));

        // When
        Resource resource = rankingExportService.exportToPdf(1L);

        // Then
        assertThat(resource).isNotNull();
        assertThat(resource.contentLength()).isGreaterThan(0);

        // PDFy zaczynają się od sygnatury "%PDF"
        byte[] content = resource.getInputStream().readAllBytes();
        String header = new String(content, 0, 4, StandardCharsets.UTF_8);
        assertThat(header).startsWith("%PDF");
    }

    @Test
    @DisplayName("Should throw exception when exporting PDF for non-existent quiz")
    void shouldThrowWhenExportingPdfForUnknownQuiz() {
        // Given
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> rankingExportService.exportToPdf(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found");
    }
}