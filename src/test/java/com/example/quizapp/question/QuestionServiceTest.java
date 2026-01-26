package com.example.quizapp.question;

import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuestionMapper questionMapper;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private QuestionService questionService;

    private Question question;
    private QuestionDto questionDto;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        quiz = Quiz.builder().id(1L).active(true).build();

        question = Question.builder()
                .id(100L)
                .quiz(quiz)
                .active(true)
                .build();

        questionDto = QuestionDto.builder()
                .quizId(1L)
                .questionText("Sample")
                .points(1)
                .build();
    }

    // ==========================================
    // SEKCJA 1: ANSWER VALIDATION LOGIC (Uzupełnienie 0% pokrycia)
    // ==========================================

    @Test
    @DisplayName("Validate Fill Blanks: Correct and Incorrect")
    void shouldValidateFillBlanksAnswer() {
        // Given
        question.setQuestionType(QuestionType.FILL_BLANKS);
        // Oczekujemy tablicy stringów w JSON
        question.setCorrectAnswer("[\"java\", \"spring\"]");

        when(questionRepository.findById(100L)).thenReturn(Optional.of(question));

        // When & Then
        // 1. Poprawna (case insensitive często jest implementowany, sprawdźmy)
        assertThat(questionService.validateAnswer(100L, "[\"Java\", \"Spring\"]")).isTrue();

        // 2. Błędna kolejność
        assertThat(questionService.validateAnswer(100L, "[\"Spring\", \"Java\"]")).isFalse();

        // 3. Zła liczba odpowiedzi
        assertThat(questionService.validateAnswer(100L, "[\"Java\"]")).isFalse();

        // 4. Błędna treść
        assertThat(questionService.validateAnswer(100L, "[\"Python\", \"Spring\"]")).isFalse();
    }

    @Test
    @DisplayName("Validate Multiple Choice: Set logic vs List logic")
    void shouldValidateMultipleChoiceAnswer() {
        question.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        // Indeksy 0 i 2 są poprawne
        question.setCorrectAnswer("[0, 2]");
        when(questionRepository.findById(100L)).thenReturn(Optional.of(question));

        // When & Then
        // 1. Dokładne dopasowanie
        assertThat(questionService.validateAnswer(100L, "[0, 2]")).isTrue();

        // 2. Odwrócona kolejność (powinno być uznane, jeśli to zbiór)
        assertThat(questionService.validateAnswer(100L, "[2, 0]")).isTrue();

        // 3. Nadmiarowa opcja (błędna)
        assertThat(questionService.validateAnswer(100L, "[0, 2, 1]")).isFalse();

        // 4. Brakująca opcja (błędna - chyba że system uznaje punkty częściowe, ale validateAnswer zwraca boolean)
        assertThat(questionService.validateAnswer(100L, "[0]")).isFalse();
    }

    @Test
    @DisplayName("Validate Sorting: Strict Order")
    void shouldValidateSortingAnswer() {
        question.setQuestionType(QuestionType.SORTING);
        question.setCorrectAnswer("[2, 0, 1]");
        when(questionRepository.findById(100L)).thenReturn(Optional.of(question));

        // When & Then
        assertThat(questionService.validateAnswer(100L, "[2, 0, 1]")).isTrue();
        assertThat(questionService.validateAnswer(100L, "[0, 2, 1]")).isFalse(); // Zła kolejność
    }

    @Test
    @DisplayName("Validate Matching: Pairs check")
    void shouldValidateMatchingAnswer() {
        question.setQuestionType(QuestionType.MATCHING);
        // JSON: [{"left":"A", "right":"1"}, {"left":"B", "right":"2"}]
        String correctJson = "[{\"left\":\"A\",\"right\":\"1\"},{\"left\":\"B\",\"right\":\"2\"}]";
        question.setCorrectAnswer(correctJson);
        when(questionRepository.findById(100L)).thenReturn(Optional.of(question));

        // When & Then
        assertThat(questionService.validateAnswer(100L, correctJson)).isTrue();

        // Złe parowanie
        String wrongJson = "[{\"left\":\"A\",\"right\":\"2\"},{\"left\":\"B\",\"right\":\"1\"}]";
        assertThat(questionService.validateAnswer(100L, wrongJson)).isFalse();
    }

    @Test
    @DisplayName("Validate Short Answer: Trim and Case Insensitivity")
    void shouldValidateShortAnswer() {
        question.setQuestionType(QuestionType.SHORT_ANSWER);
        question.setCorrectAnswer("Paris");
        when(questionRepository.findById(100L)).thenReturn(Optional.of(question));

        assertThat(questionService.validateAnswer(100L, "Paris")).isTrue();
        assertThat(questionService.validateAnswer(100L, "paris")).isTrue();
        assertThat(questionService.validateAnswer(100L, "  Paris  ")).isTrue(); // Trim check
        assertThat(questionService.validateAnswer(100L, "London")).isFalse();
    }

    // ==========================================
    // SEKCJA 2: CREATE/UPDATE VALIDATION (Low coverage methods)
    // ==========================================

    @Test
    @DisplayName("Create: Should throw IllegalArgumentException for Invalid JSON in Matching")
    void shouldThrowForInvalidMatchingJson() {
        questionDto.setQuestionType(QuestionType.MATCHING);
        questionDto.setAnswerOptions("INVALID_JSON_STRING");
        questionDto.setCorrectAnswer("[]");

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> questionService.createQuestion(questionDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JSON");
    }

    @Test
    @DisplayName("Create: Should throw for Sorting mismatch (options size != correct order size)")
    void shouldThrowForSortingSizeMismatch() {
        questionDto.setQuestionType(QuestionType.SORTING);
        questionDto.setAnswerOptions("[\"A\", \"B\", \"C\"]"); // 3 elementy
        questionDto.setCorrectAnswer("[0, 1]"); // Tylko 2 w odpowiedzi

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> questionService.createQuestion(questionDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Create: Should throw for Multiple Choice with invalid indices")
    void shouldThrowForInvalidMultipleChoiceIndex() {
        questionDto.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        questionDto.setAnswerOptions("[\"A\", \"B\"]");
        questionDto.setCorrectAnswer("[0, 5]"); // Indeks 5 nie istnieje

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> questionService.createQuestion(questionDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Correct answer index is out of range");
    }

    @Test
    @DisplayName("Create: Should throw for Single Choice if correct answer is not an integer")
    void shouldThrowForNonIntegerSingleChoice() {
        questionDto.setQuestionType(QuestionType.SINGLE_CHOICE);
        questionDto.setAnswerOptions("[\"A\", \"B\"]");
        questionDto.setCorrectAnswer("A"); // Powinien być indeks "0" lub "1"

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> questionService.createQuestion(questionDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==========================================
    // SEKCJA 3: SERVICE OPERATIONS (Delete, Get)
    // ==========================================

    @Test
    @DisplayName("Should delete question (soft delete check)")
    void shouldDeleteQuestion() {
        when(questionRepository.findById(100L)).thenReturn(Optional.of(question));

        questionService.deleteQuestion(100L);

        verify(questionRepository).save(question);
        assertThat(question.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should get questions by quiz ID")
    void shouldGetQuestionsByQuizId() {
        when(questionRepository.findByQuizIdAndActiveTrue(1L)).thenReturn(java.util.List.of(question));
        when(questionMapper.toDto(question)).thenReturn(questionDto);

        var result = questionService.getQuestionsByQuizId(1L);

        assertThat(result).hasSize(1);
    }

    // Test parametryzowany, aby upewnić się, że switch w validateQuestionByType jest pokryty dla wszystkich typów
    @ParameterizedTest
    @EnumSource(QuestionType.class)
    @DisplayName("Validate Question By Type - Happy Path Stub")
    void shouldValidateAllTypesOnCreate(QuestionType type) {
        questionDto.setQuestionType(type);

        // POPRAWKA: Dodano DROPDOWN do listy przypadków
        switch (type) {
            case SINGLE_CHOICE, TRUE_FALSE, DROPDOWN -> {
                questionDto.setAnswerOptions("[\"A\", \"B\"]");
                questionDto.setCorrectAnswer("0");
            }
            case MULTIPLE_CHOICE -> {
                questionDto.setAnswerOptions("[\"A\", \"B\", \"C\"]");
                questionDto.setCorrectAnswer("[0, 1]");
            }
            case SORTING -> {
                questionDto.setAnswerOptions("[\"A\", \"B\"]");
                questionDto.setCorrectAnswer("[0, 1]");
            }
            case FILL_BLANKS -> {
                questionDto.setAnswerOptions("[\"_\", \"_\"]");
                questionDto.setCorrectAnswer("[\"A\", \"B\"]");
            }
            case SHORT_ANSWER -> {
                questionDto.setAnswerOptions("[]");
                questionDto.setCorrectAnswer("Answer");
            }
            case MATCHING -> {
                String json = "[{\"left\":\"A\",\"right\":\"1\"},{\"left\":\"B\",\"right\":\"2\"}]";
                questionDto.setAnswerOptions(json);
                questionDto.setCorrectAnswer(json);
            }
        }

        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

        // Mockowanie zachowania dla createQuestion (żeby przeszło do końca)
        when(questionRepository.findNextQuestionOrder(1L)).thenReturn(1);
        when(questionMapper.toEntity(any())).thenReturn(question);
        when(questionRepository.save(any())).thenReturn(question);
        when(questionMapper.toDto(any())).thenReturn(questionDto);

        // Wywołanie metody serwisu
        questionService.createQuestion(questionDto);
    }
}