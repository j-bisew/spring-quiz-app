package com.example.quizapp.quiz;

import com.example.quizapp.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("QuizService Tests")
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizMapper quizMapper;

    @InjectMocks
    private QuizService quizService;

    private Quiz quiz1;
    private Quiz quiz2;
    private QuizDto quizDto1;
    private QuizDto quizDto2;

    @BeforeEach
    void setUp() {
        // Create test quiz entities
        quiz1 = Quiz.builder()
                .id(1L)
                .title("Java Quiz")
                .description("Test Java knowledge")
                .active(true)
                .createdBy("testuser")
                .build();

        quiz2 = Quiz.builder()
                .id(2L)
                .title("Spring Quiz")
                .description("Test Spring knowledge")
                .active(true)
                .createdBy("testuser")
                .build();

        // Create test DTOs
        quizDto1 = new QuizDto();
        quizDto1.setId(1L);
        quizDto1.setTitle("Java Quiz");
        quizDto1.setDescription("Test Java knowledge");
        quizDto1.setActive(true);

        quizDto2 = new QuizDto();
        quizDto2.setId(2L);
        quizDto2.setTitle("Spring Quiz");
        quizDto2.setDescription("Test Spring knowledge");
        quizDto2.setActive(true);
    }

    // ==================== GET ALL Tests ====================

    @Test
    @DisplayName("Should get all active quizzes")
    void shouldGetAllActiveQuizzes() {
        // Given
        List<Quiz> quizzes = List.of(quiz1, quiz2);
        when(quizRepository.findByActiveTrue()).thenReturn(quizzes);
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);
        when(quizMapper.toDto(quiz2)).thenReturn(quizDto2);

        // When
        List<QuizDto> result = quizService.getAllActiveQuizzes();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(quizDto1, quizDto2);

        verify(quizRepository).findByActiveTrue();
        verify(quizMapper, times(2)).toDto(any(Quiz.class));
    }

    @Test
    @DisplayName("Should get all active quizzes with pagination")
    void shouldGetAllActiveQuizzesWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Quiz> quizPage = new PageImpl<>(List.of(quiz1, quiz2), pageable, 2);

        when(quizRepository.findByActiveTrue(pageable)).thenReturn(quizPage);
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);
        when(quizMapper.toDto(quiz2)).thenReturn(quizDto2);

        // When
        Page<QuizDto> result = quizService.getAllActiveQuizzes(pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(quizRepository).findByActiveTrue(pageable);
    }

    @Test
    @DisplayName("Should return empty list when no active quizzes")
    void shouldReturnEmptyListWhenNoActiveQuizzes() {
        // Given
        when(quizRepository.findByActiveTrue()).thenReturn(List.of());

        // When
        List<QuizDto> result = quizService.getAllActiveQuizzes();

        // Then
        assertThat(result).isEmpty();
        verify(quizRepository).findByActiveTrue();
        verify(quizMapper, never()).toDto(any(Quiz.class));
    }

    // ==================== GET BY ID Tests ====================

    @Test
    @DisplayName("Should get quiz by ID")
    void shouldGetQuizById() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);

        // When
        QuizDto result = quizService.getQuizById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java Quiz");

        verify(quizRepository).findById(1L);
        verify(quizMapper).toDto(quiz1);
    }

    @Test
    @DisplayName("Should throw exception when quiz not found by ID")
    void shouldThrowExceptionWhenQuizNotFoundById() {
        // Given
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> quizService.getQuizById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found with id: 999");

        verify(quizRepository).findById(999L);
        verify(quizMapper, never()).toDto(any(Quiz.class));
    }

    @Test
    @DisplayName("Should get quiz by ID with questions")
    void shouldGetQuizByIdWithQuestions() {
        // Given
        when(quizRepository.findByIdWithQuestions(1L)).thenReturn(Optional.of(quiz1));
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);

        // When
        QuizDto result = quizService.getQuizByIdWithQuestions(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java Quiz");

        verify(quizRepository).findByIdWithQuestions(1L);
        verify(quizMapper).toDto(quiz1);
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("Should create quiz successfully")
    void shouldCreateQuizSuccessfully() {
        // Given
        QuizDto newQuizDto = new QuizDto();
        newQuizDto.setTitle("New Quiz");
        newQuizDto.setDescription("New description");

        Quiz newQuiz = Quiz.builder()
                .title("New Quiz")
                .description("New description")
                .build();

        Quiz savedQuiz = Quiz.builder()
                .id(3L)
                .title("New Quiz")
                .description("New description")
                .active(true)
                .build();

        when(quizRepository.existsByTitle("New Quiz")).thenReturn(false);
        when(quizMapper.toEntity(newQuizDto)).thenReturn(newQuiz);
        when(quizRepository.save(any(Quiz.class))).thenReturn(savedQuiz);
        when(quizMapper.toDto(savedQuiz)).thenReturn(newQuizDto);

        // When
        QuizDto result = quizService.createQuiz(newQuizDto);

        // Then
        assertThat(result).isNotNull();

        verify(quizRepository).existsByTitle("New Quiz");
        verify(quizMapper).toEntity(newQuizDto);
        verify(quizRepository).save(any(Quiz.class));
        verify(quizMapper).toDto(savedQuiz);
    }

    @Test
    @DisplayName("Should throw exception when creating quiz with existing title")
    void shouldThrowExceptionWhenCreatingQuizWithExistingTitle() {
        // Given
        QuizDto newQuizDto = new QuizDto();
        newQuizDto.setTitle("Java Quiz");

        when(quizRepository.existsByTitle("Java Quiz")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> quizService.createQuiz(newQuizDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quiz with title 'Java Quiz' already exists");

        verify(quizRepository).existsByTitle("Java Quiz");
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("Should update quiz successfully")
    void shouldUpdateQuizSuccessfully() {
        // Given
        QuizDto updateDto = new QuizDto();
        updateDto.setTitle("Updated Java Quiz");
        updateDto.setDescription("Updated description");
        updateDto.setActive(true);

        Quiz updatedQuiz = Quiz.builder()
                .id(1L)
                .title("Updated Java Quiz")
                .description("Updated description")
                .active(true)
                .build();

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizRepository.existsByTitle("Updated Java Quiz")).thenReturn(false);
        when(quizRepository.save(any(Quiz.class))).thenReturn(updatedQuiz);
        when(quizMapper.toDto(updatedQuiz)).thenReturn(updateDto);

        // When
        QuizDto result = quizService.updateQuiz(1L, updateDto);

        // Then
        assertThat(result).isNotNull();

        verify(quizRepository).findById(1L);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent quiz")
    void shouldThrowExceptionWhenUpdatingNonExistentQuiz() {
        // Given
        QuizDto updateDto = new QuizDto();
        updateDto.setTitle("Updated Quiz");

        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> quizService.updateQuiz(999L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found with id: 999");

        verify(quizRepository).findById(999L);
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to existing title")
    void shouldThrowExceptionWhenUpdatingToExistingTitle() {
        // Given
        QuizDto updateDto = new QuizDto();
        updateDto.setTitle("Spring Quiz"); // This title already exists

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizRepository.existsByTitle("Spring Quiz")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> quizService.updateQuiz(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quiz with title 'Spring Quiz' already exists");

        verify(quizRepository).findById(1L);
        verify(quizRepository).existsByTitle("Spring Quiz");
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should allow updating quiz with same title")
    void shouldAllowUpdatingQuizWithSameTitle() {
        // Given
        QuizDto updateDto = new QuizDto();
        updateDto.setTitle("Java Quiz"); // Same title
        updateDto.setDescription("Updated description");
        updateDto.setActive(true);

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz1);
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);

        // When
        QuizDto result = quizService.updateQuiz(1L, updateDto);

        // Then
        assertThat(result).isNotNull();

        verify(quizRepository).findById(1L);
        verify(quizRepository, never()).existsByTitle(anyString());
        verify(quizRepository).save(any(Quiz.class));
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("Should soft delete quiz")
    void shouldSoftDeleteQuiz() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz1);

        // When
        quizService.deleteQuiz(1L);

        // Then
        verify(quizRepository).findById(1L);
        verify(quizRepository).save(any(Quiz.class));
        assertThat(quiz1.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent quiz")
    void shouldThrowExceptionWhenDeletingNonExistentQuiz() {
        // Given
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> quizService.deleteQuiz(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found with id: 999");

        verify(quizRepository).findById(999L);
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should permanently delete quiz")
    void shouldPermanentlyDeleteQuiz() {
        // Given
        when(quizRepository.existsById(1L)).thenReturn(true);

        // When
        quizService.permanentlyDeleteQuiz(1L);

        // Then
        verify(quizRepository).existsById(1L);
        verify(quizRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when permanently deleting non-existent quiz")
    void shouldThrowExceptionWhenPermanentlyDeletingNonExistentQuiz() {
        // Given
        when(quizRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> quizService.permanentlyDeleteQuiz(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Quiz not found with id: 999");

        verify(quizRepository).existsById(999L);
        verify(quizRepository, never()).deleteById(anyLong());
    }

    // ==================== SEARCH Tests ====================

    @Test
    @DisplayName("Should search quizzes by keyword")
    void shouldSearchQuizzesByKeyword() {
        // Given
        when(quizRepository.searchByTitle("Java")).thenReturn(List.of(quiz1));
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);

        // When
        List<QuizDto> result = quizService.searchQuizzes("Java");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).contains("Java");

        verify(quizRepository).searchByTitle("Java");
    }

    @Test
    @DisplayName("Should search quizzes with pagination")
    void shouldSearchQuizzesWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Quiz> quizPage = new PageImpl<>(List.of(quiz1), pageable, 1);

        when(quizRepository.searchByTitle("Java", pageable)).thenReturn(quizPage);
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);

        // When
        Page<QuizDto> result = quizService.searchQuizzes("Java", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(quizRepository).searchByTitle("Java", pageable);
    }

    // ==================== OTHER Tests ====================

    @Test
    @DisplayName("Should get quizzes by creator")
    void shouldGetQuizzesByCreator() {
        // Given
        when(quizRepository.findByCreatedBy("testuser")).thenReturn(List.of(quiz1, quiz2));
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);
        when(quizMapper.toDto(quiz2)).thenReturn(quizDto2);

        // When
        List<QuizDto> result = quizService.getQuizzesByCreator("testuser");

        // Then
        assertThat(result).hasSize(2);
        verify(quizRepository).findByCreatedBy("testuser");
    }

    @Test
    @DisplayName("Should count active quizzes")
    void shouldCountActiveQuizzes() {
        // Given
        when(quizRepository.countByActiveTrue()).thenReturn(5L);

        // When
        long count = quizService.countActiveQuizzes();

        // Then
        assertThat(count).isEqualTo(5);
        verify(quizRepository).countByActiveTrue();
    }

    // ==================== Verification Tests ====================

    @Test
    @DisplayName("Should verify repository is called exactly once")
    void shouldVerifyRepositoryIsCalledExactlyOnce() {
        // Given
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizMapper.toDto(quiz1)).thenReturn(quizDto1);

        // When
        quizService.getQuizById(1L);

        // Then
        verify(quizRepository, times(1)).findById(1L);
        verify(quizMapper, times(1)).toDto(quiz1);
    }

    @Test
    @DisplayName("Should verify no interactions when exception thrown early")
    void shouldVerifyNoInteractionsWhenExceptionThrownEarly() {
        // Given
        when(quizRepository.existsByTitle("Existing Quiz")).thenReturn(true);

        QuizDto newQuizDto = new QuizDto();
        newQuizDto.setTitle("Existing Quiz");

        // When/Then
        assertThatThrownBy(() -> quizService.createQuiz(newQuizDto))
                .isInstanceOf(IllegalArgumentException.class);

        // Verify save was never called
        verify(quizRepository, never()).save(any(Quiz.class));
    }
}