package com.example.quizapp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("UserMapper Tests")
class UserMapperTest {

    // Pobieramy instancję mappera (MapStruct generuje implementację UserMapperImpl)
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("Should map User entity to UserDto")
    void shouldMapToDto() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("secret")
                .fullName("Test User")
                .role(UserRole.USER)
                .enabled(true)
                .locked(false)
                .build();

        // When
        UserDto dto = userMapper.toDto(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getFullName()).isEqualTo("Test User");
        assertThat(dto.getRole()).isEqualTo(UserRole.USER);

        // Hasło nie powinno być mapowane do DTO
        // (W UserDto nie ma pola password, więc to jest automatycznie spełnione przez strukturę klasy)
    }

    @Test
    @DisplayName("Should map UserDto to User entity")
    void shouldMapToEntity() {
        // Given
        UserDto dto = UserDto.builder()
                .username("dtoUser")
                .email("dto@example.com")
                .fullName("Dto User")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        // When
        User user = userMapper.toEntity(dto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("dtoUser");
        assertThat(user.getEmail()).isEqualTo("dto@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);

        // Pola ignorowane w mapperze
        assertThat(user.getPassword()).isNull();
        assertThat(user.getId()).isNull();
    }

    @Test
    @DisplayName("Should update User entity from UserUpdateDto")
    void shouldUpdateEntityFromDto() {
        // Given
        User existingUser = User.builder()
                .id(1L)
                .username("original")
                .email("original@example.com")
                .fullName("Original Name")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("updated@example.com")
                .fullName("Updated Name")
                .role(UserRole.ADMIN)
                .enabled(false)
                .locked(true)
                .build();

        // When
        userMapper.updateEntityFromDto(updateDto, existingUser);

        // Then
        // Pola zaktualizowane
        assertThat(existingUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(existingUser.getFullName()).isEqualTo("Updated Name");
        assertThat(existingUser.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(existingUser.isEnabled()).isFalse();
        assertThat(existingUser.isLocked()).isTrue();

        // Pola, które nie powinny się zmienić (ignorowane w mapperze lub brak w DTO)
        assertThat(existingUser.getId()).isEqualTo(1L);
        assertThat(existingUser.getUsername()).isEqualTo("original");
    }

    @Test
    @DisplayName("Should ignore null values during update if configured (optional check)")
    void shouldIgnoreNullsDuringUpdate() {
        // Uwaga: Domyślnie MapStruct nadpisuje nullami, chyba że ustawisz nullValuePropertyMappingStrategy = IGNORE.
        // W Twoim kodzie jest ReportingPolicy.IGNORE, ale nie ma strategii dla nulli.
        // Jeśli chcesz sprawdzić domyślne zachowanie (nadpisywanie):

        // Given
        User user = User.builder().fullName("Old Name").build();
        UserUpdateDto updateDto = UserUpdateDto.builder().fullName(null).build(); // Null w DTO

        // When
        userMapper.updateEntityFromDto(updateDto, user);

        // Then (przy domyślnej konfiguracji MapStruct)
        assertThat(user.getFullName()).isNull();
    }
}