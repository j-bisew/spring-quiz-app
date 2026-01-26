package com.example.quizapp.ranking;

import com.example.quizapp.game.GameResult;
import com.example.quizapp.player.Player;
import com.example.quizapp.quiz.Quiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RankingMapper Tests")
class RankingMapperTest {

    private final RankingMapper rankingMapper = Mappers.getMapper(RankingMapper.class);

    @Test
    @DisplayName("Should map GameResult to RankingDto")
    void shouldMapToRankingDto() {
        // Given
        Player player = Player.builder().nickname("MapperUser").build();
        Quiz quiz = Quiz.builder().title("Mapper Quiz").build();
        LocalDateTime now = LocalDateTime.now();

        GameResult result = GameResult.builder()
                .player(player)
                .quiz(quiz)
                .score(85)
                .maxScore(100)
                .percentageScore(85.0)
                .correctAnswers(17)
                .wrongAnswers(3)
                .totalQuestions(20)
                .timeTakenSeconds(200)
                .completedAt(now)
                .build();
        // Symulujemy metodę getGrade(), która jest używana w ekspresji mappera
        // W prawdziwym obiekcie to jest logika w klasie, tu zakładamy że mapper wywoła gettera/metodę
        // Jeśli getGrade() to metoda w GameResult, to zadziała.

        // When
        RankingDto dto = rankingMapper.toRankingDto(result);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getPlayerNickname()).isEqualTo("MapperUser");
        assertThat(dto.getQuizTitle()).isEqualTo("Mapper Quiz");
        assertThat(dto.getScore()).isEqualTo(85);
        assertThat(dto.getPercentageScore()).isEqualTo(85.0);
        assertThat(dto.getTimeTakenSeconds()).isEqualTo(200);
        assertThat(dto.getCompletedAt()).isEqualTo(now);

        // Sprawdzenie czy pole grade zostało zmapowane (korzystając z metody getGrade() na encji)
        // Zakładając, że GameResult ma metodę getGrade(), MapStruct ją wywoła.
        // Jeśli GameResult to prosty builder w teście, upewnij się, że logika getGrade działa
        // lub pole jest ustawione (zależnie od implementacji GameResult).
        assertThat(dto.getGrade()).isEqualTo(result.getGrade());
    }

    @Test
    @DisplayName("Should map GameResult to RankingPositionDto")
    void shouldMapToRankingPositionDto() {
        // Given
        Player player = Player.builder()
                .id(10L)
                .nickname("PositionUser")
                .build();

        Quiz quiz = Quiz.builder()
                .id(5L)
                .title("Position Quiz")
                .build();

        GameResult result = GameResult.builder()
                .player(player)
                .quiz(quiz)
                .score(50)
                .maxScore(100)
                .timeTakenSeconds(100)
                .build();

        // When
        RankingPositionDto dto = rankingMapper.toRankingPositionDto(result);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getPlayerId()).isEqualTo(10L);
        assertThat(dto.getPlayerNickname()).isEqualTo("PositionUser");
        assertThat(dto.getQuizId()).isEqualTo(5L);
        assertThat(dto.getQuizTitle()).isEqualTo("Position Quiz");
        assertThat(dto.getScore()).isEqualTo(50);

        // Pola ignorowane w mapperze (obliczane osobno w serwisie) powinny być null/default
        assertThat(dto.getPosition()).isNull();
        assertThat(dto.getTotalPlayers()).isNull();
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNulls() {
        // Given
        GameResult result = GameResult.builder()
                .player(Player.builder().build())
                .quiz(Quiz.builder().build())
                .build();

        // When
        RankingDto dto = rankingMapper.toRankingDto(result);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getPlayerNickname()).isNull();
        assertThat(dto.getQuizTitle()).isNull();
        assertThat(dto.getScore()).isZero();
    }
}