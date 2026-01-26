package com.example.quizapp.game;

import com.example.quizapp.player.Player;
import com.example.quizapp.quiz.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("GameResultMapper Tests")
class GameResultMapperTest {

    private final GameResultMapper mapper = Mappers.getMapper(GameResultMapper.class);

    @Test
    @DisplayName("Should map GameResult Entity to DTO")
    void shouldMapToDto() {
        // Given
        Player player = Player.builder().id(100L).nickname("Gamer").build();
        Quiz quiz = Quiz.builder().id(200L).title("Hard Quiz").build();
        LocalDateTime now = LocalDateTime.now();

        GameResult entity = GameResult.builder()
                .id(1L)
                .player(player)
                .quiz(quiz)
                .score(80)
                .maxScore(100)
                .percentageScore(80.0)
                .timeTakenSeconds(120)
                .completed(true)
                .completedAt(now)
                .build();

        // When
        GameResultDto dto = mapper.toDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPlayerId()).isEqualTo(100L);
        assertThat(dto.getPlayerNickname()).isEqualTo("Gamer");
        assertThat(dto.getQuizId()).isEqualTo(200L);
        assertThat(dto.getQuizTitle()).isEqualTo("Hard Quiz");
        assertThat(dto.getScore()).isEqualTo(80);
        assertThat(dto.getGrade()).isEqualTo("B"); // 80% -> B
        assertThat(dto.isPassed()).isTrue();       // 80% >= 50%
        assertThat(dto.getCompletedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should map DTO to Entity (Ignoring read-only/calculated fields)")
    void shouldMapToEntity() {
        // Given
        GameResultDto dto = GameResultDto.builder()
                .score(50)
                .maxScore(100)
                .correctAnswers(5)
                .wrongAnswers(5)
                .timeTakenSeconds(60)
                .percentageScore(50.0)
                .build();

        // When
        GameResult entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getScore()).isEqualTo(50);
        assertThat(entity.getMaxScore()).isEqualTo(100);
        assertThat(entity.getCorrectAnswers()).isEqualTo(5);

        // Fields ignored in mapping (should be null)
        assertThat(entity.getId()).isNull();
        assertThat(entity.getPlayer()).isNull();
        assertThat(entity.getQuiz()).isNull();
    }
}