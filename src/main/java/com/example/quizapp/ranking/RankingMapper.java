package com.example.quizapp.ranking;

import com.example.quizapp.game.GameResult;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RankingMapper {

//    Convert GameResult to RankingDto
    @Mapping(target = "playerNickname", source = "player.nickname")
    @Mapping(target = "quizTitle", source = "quiz.title")
    @Mapping(target = "grade", expression = "java(gameResult.getGrade())")
    RankingDto toRankingDto(GameResult gameResult);

//    Convert GameResult to RankingPositionDto
    @Mapping(target = "playerId", source = "player.id")
    @Mapping(target = "playerNickname", source = "player.nickname")
    @Mapping(target = "quizId", source = "quiz.id")
    @Mapping(target = "quizTitle", source = "quiz.title")
    @Mapping(target = "position", ignore = true) // Calculated separately
    @Mapping(target = "totalPlayers", ignore = true) // Calculated separately
    RankingPositionDto toRankingPositionDto(GameResult gameResult);
}