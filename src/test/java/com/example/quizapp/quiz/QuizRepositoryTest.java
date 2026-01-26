package com.example.quizapp.quiz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("QuizRepository Tests")
class QuizRepositoryTest {
    @Autowired
    private QuizRepository quizRepository;

    private Quiz testQuiz1;
    private Quiz testQuiz2;
    private Quiz inactiveQuiz;

    @BeforeEach
    void setUp() {
        // Clear repository
        quizRepository.deleteAll();

        // Create test data
        testQuiz1 = Quiz.builder()
                .title("Java Basics Quiz")
                .description("Test your Java knowledge")
                .randomQuestionOrder(false)
                .randomAnswerOrder(false)
                .timeLimitMinutes(30)
                .negativePointsEnabled(false)
                .backButtonBlocked(false)
                .active(true)
                .createdBy("testuser")
                .build();

        testQuiz2 = Quiz.builder()
                .title("Spring Framework Quiz")
                .description("Advanced Spring concepts")
                .randomQuestionOrder(true)
                .randomAnswerOrder(true)
                .timeLimitMinutes(45)
                .negativePointsEnabled(true)
                .backButtonBlocked(true)
                .active(true)
                .createdBy("testuser")
                .build();

        inactiveQuiz = Quiz.builder()
                .title("Inactive Quiz")
                .description("This quiz is inactive")
                .active(false)
                .createdBy("testuser")
                .build();
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("Should save quiz successfully")
    void shouldSaveQuiz() {
        // When
        Quiz savedQuiz = quizRepository.save(testQuiz1);

        // Then
        assertThat(savedQuiz).isNotNull();
        assertThat(savedQuiz.getId()).isNotNull();
        assertThat(savedQuiz.getTitle()).isEqualTo("Java Basics Quiz");
        assertThat(savedQuiz.getCreatedAt()).isNotNull();
        assertThat(savedQuiz.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should save quiz with all settings")
    void shouldSaveQuizWithAllSettings() {
        // When
        Quiz savedQuiz = quizRepository.save(testQuiz2);

        // Then
        assertThat(savedQuiz.isRandomQuestionOrder()).isTrue();
        assertThat(savedQuiz.isRandomAnswerOrder()).isTrue();
        assertThat(savedQuiz.getTimeLimitMinutes()).isEqualTo(45);
        assertThat(savedQuiz.isNegativePointsEnabled()).isTrue();
        assertThat(savedQuiz.isBackButtonBlocked()).isTrue();
    }

    // ==================== READ Tests ====================

    @Test
    @DisplayName("Should find quiz by ID")
    void shouldFindQuizById() {
        // Given
        Quiz savedQuiz = quizRepository.save(testQuiz1);

        // When
        Optional<Quiz> foundQuiz = quizRepository.findById(savedQuiz.getId());

        // Then
        assertThat(foundQuiz).isPresent();
        assertThat(foundQuiz.get().getTitle()).isEqualTo("Java Basics Quiz");
    }

    @Test
    @DisplayName("Should return empty when quiz not found")
    void shouldReturnEmptyWhenQuizNotFound() {
        // When
        Optional<Quiz> foundQuiz = quizRepository.findById(999L);

        // Then
        assertThat(foundQuiz).isEmpty();
    }

    @Test
    @DisplayName("Should find all quizzes")
    void shouldFindAllQuizzes() {
        // Given
        quizRepository.save(testQuiz1);
        quizRepository.save(testQuiz2);
        quizRepository.save(inactiveQuiz);

        // When
        List<Quiz> allQuizzes = quizRepository.findAll();

        // Then
        assertThat(allQuizzes).hasSize(3);
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("Should update quiz title")
    void shouldUpdateQuizTitle() {
        // Given
        Quiz savedQuiz = quizRepository.save(testQuiz1);

        // When
        savedQuiz.setTitle("Updated Java Quiz");
        Quiz updatedQuiz = quizRepository.save(savedQuiz);

        // Then
        assertThat(updatedQuiz.getTitle()).isEqualTo("Updated Java Quiz");
        assertThat(updatedQuiz.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update quiz active status")
    void shouldUpdateQuizActiveStatus() {
        // Given
        Quiz savedQuiz = quizRepository.save(testQuiz1);

        // When
        savedQuiz.setActive(false);
        Quiz updatedQuiz = quizRepository.save(savedQuiz);

        // Then
        assertThat(updatedQuiz.isActive()).isFalse();
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("Should delete quiz by ID")
    void shouldDeleteQuizById() {
        // Given
        Quiz savedQuiz = quizRepository.save(testQuiz1);
        Long quizId = savedQuiz.getId();

        // When
        quizRepository.deleteById(quizId);

        // Then
        Optional<Quiz> deletedQuiz = quizRepository.findById(quizId);
        assertThat(deletedQuiz).isEmpty();
    }

    @Test
    @DisplayName("Should delete quiz entity")
    void shouldDeleteQuizEntity() {
        // Given
        Quiz savedQuiz = quizRepository.save(testQuiz1);
        Long quizId = savedQuiz.getId();

        // When
        quizRepository.delete(savedQuiz);

        // Then
        Optional<Quiz> deletedQuiz = quizRepository.findById(quizId);
        assertThat(deletedQuiz).isEmpty();
    }

    // ==================== Custom Query Tests ====================

    @Test
    @DisplayName("Should find only active quizzes")
    void shouldFindOnlyActiveQuizzes() {
        // Given
        quizRepository.save(testQuiz1);
        quizRepository.save(testQuiz2);
        quizRepository.save(inactiveQuiz);

        // When
        List<Quiz> activeQuizzes = quizRepository.findByActiveTrue();

        // Then
        assertThat(activeQuizzes).hasSize(2);
        assertThat(activeQuizzes)
                .extracting(Quiz::getTitle)
                .containsExactlyInAnyOrder("Java Basics Quiz", "Spring Framework Quiz");
    }

    @Test
    @DisplayName("Should find active quizzes with pagination")
    void shouldFindActiveQuizzesWithPagination() {
        // Given
        quizRepository.save(testQuiz1);
        quizRepository.save(testQuiz2);
        quizRepository.save(inactiveQuiz);

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Quiz> activeQuizzes = quizRepository.findByActiveTrue(pageable);

        // Then
        assertThat(activeQuizzes.getContent()).hasSize(2);
        assertThat(activeQuizzes.getTotalElements()).isEqualTo(2);
        assertThat(activeQuizzes.isFirst()).isTrue();
    }

    @Test
    @DisplayName("Should search quizzes by title")
    void shouldSearchQuizzesByTitle() {
        // Given
        quizRepository.save(testQuiz1);
        quizRepository.save(testQuiz2);

        // When
        List<Quiz> foundQuizzes = quizRepository.searchByTitle("Java");

        // Then
        assertThat(foundQuizzes).hasSize(1);
        assertThat(foundQuizzes.getFirst().getTitle()).contains("Java");
    }

    @Test
    @DisplayName("Should search quizzes case-insensitive")
    void shouldSearchQuizzesCaseInsensitive() {
        // Given
        quizRepository.save(testQuiz1);
        quizRepository.save(testQuiz2);

        // When
        List<Quiz> foundQuizzes = quizRepository.searchByTitle("spring");

        // Then
        assertThat(foundQuizzes).hasSize(1);
        assertThat(foundQuizzes.getFirst().getTitle()).containsIgnoringCase("spring");
    }

    @Test
    @DisplayName("Should search quizzes with pagination")
    void shouldSearchQuizzesWithPagination() {
        // Given
        quizRepository.save(testQuiz1);
        quizRepository.save(testQuiz2);

        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Quiz> foundQuizzes = quizRepository.searchByTitle("Quiz", pageable);

        // Then
        assertThat(foundQuizzes.getContent()).hasSize(1);
        assertThat(foundQuizzes.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should count active quizzes")
    void shouldCountActiveQuizzes() {
        // Given
        quizRepository.save(testQuiz1);
        quizRepository.save(testQuiz2);
        quizRepository.save(inactiveQuiz);

        // When
        long count = quizRepository.countByActiveTrue();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find quizzes by created by user")
    void shouldFindQuizzesByCreatedByUser() {
        // Given
        testQuiz1.setCreatedBy("user1");
        testQuiz2.setCreatedBy("user2");
        quizRepository.save(testQuiz1);
        quizRepository.save(testQuiz2);

        // When
        List<Quiz> user1Quizzes = quizRepository.findByCreatedBy("user1");

        // Then
        assertThat(user1Quizzes).hasSize(1);
        assertThat(user1Quizzes.getFirst().getCreatedBy()).isEqualTo("user1");
    }

    @Test
    @DisplayName("Should return empty list when no quizzes found")
    void shouldReturnEmptyListWhenNoQuizzesFound() {
        // When
        List<Quiz> foundQuizzes = quizRepository.searchByTitle("NonExistent");

        // Then
        assertThat(foundQuizzes).isEmpty();
    }
}