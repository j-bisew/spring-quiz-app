package com.example.quizapp.question;

import com.example.quizapp.quiz.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QuestionMapper Tests")
class QuestionMapperTest {

    // Używamy Mappers.getMapper, co pozwala testować mapper w izolacji (bez Spring Context)
    private final QuestionMapper questionMapper = Mappers.getMapper(QuestionMapper.class);

    @Test
    @DisplayName("Should map Entity to DTO")
    void shouldMapToDto() {
        // Given
        Quiz quiz = Quiz.builder().id(1L).title("Test Quiz").build();
        Question question = Question.builder()
                .id(100L)
                .quiz(quiz)
                .questionText("What is Java?")
                .questionType(QuestionType.SINGLE_CHOICE)
                .answerOptions("[\"A\", \"B\"]")
                .correctAnswer("0")
                .explanation("It is a language")
                .points(5)
                .timeLimitSeconds(30)
                .active(true)
                .questionOrder(1) // POPRAWKA: używamy questionOrder zamiast orderIndex
                .build();

        // When
        QuestionDto dto = questionMapper.toDto(question);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getQuizId()).isEqualTo(1L); // Sprawdza mapowanie quiz.id -> quizId
        assertThat(dto.getQuestionText()).isEqualTo("What is Java?");
        assertThat(dto.getQuestionType()).isEqualTo(QuestionType.SINGLE_CHOICE);
        assertThat(dto.getPoints()).isEqualTo(5);
        assertThat(dto.getExplanation()).isEqualTo("It is a language");
        assertThat(dto.getQuestionOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should map DTO to Entity")
    void shouldMapToEntity() {
        // Given
        QuestionDto dto = QuestionDto.builder()
                .questionText("New Question")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .answerOptions("[\"1\", \"2\"]")
                .correctAnswer("[0]")
                .points(10)
                .questionOrder(2)
                .build();

        // When
        Question entity = questionMapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getQuestionText()).isEqualTo("New Question");
        assertThat(entity.getQuestionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(entity.getPoints()).isEqualTo(10);
        assertThat(entity.getQuestionOrder()).isEqualTo(2);

        // Pola ignorowane w toEntity (zazwyczaj id, quiz są ustawiane osobno w serwisie)
        assertThat(entity.getId()).isNull();
        assertThat(entity.getQuiz()).isNull();
    }

    @Test
    @DisplayName("Should update Entity from DTO")
    void shouldUpdateEntityFromDto() {
        // Given
        Question entity = Question.builder()
                .id(100L)
                .questionText("Old Text")
                .points(5)
                .build();

        QuestionDto dto = QuestionDto.builder()
                .questionText("Updated Text")
                .points(10)
                .explanation("New Expl")
                .build();

        // When
        questionMapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.getId()).isEqualTo(100L); // ID nie powinno się zmienić
        assertThat(entity.getQuestionText()).isEqualTo("Updated Text");
        assertThat(entity.getPoints()).isEqualTo(10);
        assertThat(entity.getExplanation()).isEqualTo("New Expl");
    }

    @Test
    @DisplayName("Should handle nulls correctly")
    void shouldHandleNulls() {
        // Given
        Question entity = new Question();

        // When
        QuestionDto dto = questionMapper.toDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getQuestionText()).isNull();
        assertThat(dto.getQuizId()).isNull();
    }
}