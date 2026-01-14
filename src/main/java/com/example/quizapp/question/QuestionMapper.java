package com.example.quizapp.question;

import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper {

//    Convert Question entity to QuestionDto
    @Mapping(target = "quizId", source = "quiz.id")
    QuestionDto toDto(Question question);

//    Convert QuestionDto to Question entity
    @Mapping(target = "quiz", ignore = true)
    @Mapping(target = "id", ignore = true)
    Question toEntity(QuestionDto questionDto);

//    Update Question entity from QuestionDto
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    void updateEntityFromDto(QuestionDto questionDto, @MappingTarget Question question);
}