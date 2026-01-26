package com.example.quizapp.quiz;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QuizMapper Tests")
class QuizMapperTest {

    private final QuizMapper mapper = Mappers.getMapper(QuizMapper.class);

    @Test
    @DisplayName("Should map Quiz Entity to DTO")
    void shouldMapToDto() {
        // Given
        Quiz quiz = Quiz.builder()
                .id(1L)
                .title("Integration Test Quiz")
                .description("Desc")
                .timeLimitMinutes(15)
                .active(true)
                .createdBy("admin")
                .build();

        // When
        QuizDto dto = mapper.toDto(quiz);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Integration Test Quiz");
        assertThat(dto.getTimeLimitMinutes()).isEqualTo(15);
        assertThat(dto.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should map QuizDto to Entity")
    void shouldMapToEntity() {
        // Given
        QuizDto dto = QuizDto.builder()
                .title("New Quiz")
                .description("New Desc")
                .timeLimitMinutes(5)
                .active(false)
                .build();

        // When
        Quiz entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getTitle()).isEqualTo("New Quiz");
        assertThat(entity.getDescription()).isEqualTo("New Desc");
        assertThat(entity.isActive()).isFalse();

        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("Should update Entity from DTO")
    void shouldUpdateEntityFromDto() {
        // Given
        Quiz entity = Quiz.builder()
                .id(1L)
                .title("Old Title")
                .description("Old Desc")
                .build();

        QuizDto dto = QuizDto.builder()
                .title("New Title")
                .description("New Desc")
                .build();

        // When
        mapper.updateEntityFromDto(dto, entity);

        // Then
        assertThat(entity.getTitle()).isEqualTo("New Title");
        assertThat(entity.getDescription()).isEqualTo("New Desc");
        assertThat(entity.getId()).isEqualTo(1L);
    }
}