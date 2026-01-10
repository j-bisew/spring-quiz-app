package com.example.quizapp.player;

import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayerMapper {

//    Convert Player entity to PlayerDto
    PlayerDto toDto(Player player);

//    Convert PlayerDto to Player entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstPlayedAt", ignore = true)
    @Mapping(target = "lastPlayedAt", ignore = true)
    Player toEntity(PlayerDto playerDto);

//    Update Player entity from PlayerDto
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstPlayedAt", ignore = true)
    void updateEntityFromDto(PlayerDto playerDto, @MappingTarget Player player);
}