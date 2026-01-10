package com.example.quizapp.game;

import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameResultMapper {

//    Convert GameResult entity to GameResultDto
    @Mapping(target = "playerId", source = "player.id")
    @Mapping(target = "playerNickname", source = "player.nickname")
    @Mapping(target = "quizId", source = "quiz.id")
    @Mapping(target = "quizTitle", source = "quiz.title")
    @Mapping(target = "grade", expression = "java(gameResult.getGrade())")
    @Mapping(target = "passed", expression = "java(gameResult.isPassed())")
    @Mapping(target = "detailedAnswers", ignore = true)
    GameResultDto toDto(GameResult gameResult);

//    Convert GameResultDto to GameResult entity
    @Mapping(target = "player", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "answersJson", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    GameResult toEntity(GameResultDto gameResultDto);
}