package com.example.quizapp.quiz;

import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuizMapper {

//    Convert Quiz entity to QuizDto
    @Mapping(target = "questionCount", expression = "java(quiz.getQuestionCount())")
    @Mapping(target = "totalPoints", expression = "java(quiz.getTotalPoints())")
    QuizDto toDto(Quiz quiz);

//    Convert QuizDto to Quiz entity
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Quiz toEntity(QuizDto quizDto);

//    Update Quiz entity from QuizDto
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(QuizDto quizDto, @MappingTarget Quiz quiz);
}