package com.example.quizapp.security;

import com.example.quizapp.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(UserRole.USER)
                .enabled(true)
                .build();

        registrationDto = UserRegistrationDto.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .fullName("New User")
                .build();
    }

    // ==================== CREATE USER Tests ====================

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() {
        // Given
        when(userRepository.existsByUsername(registrationDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(10L);
            return savedUser;
        });

        // When
        User createdUser = userService.createUser(registrationDto);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(10L);
        assertThat(createdUser.getUsername()).isEqualTo("newuser");
        assertThat(createdUser.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowWhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername(registrationDto.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(registrationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowWhenEmailExists() {
        // Given
        when(userRepository.existsByUsername(registrationDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(registrationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== UPDATE USER Tests ====================

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUser() {
        // Given
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .fullName("Updated Name")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User updatedUser = userService.updateUser(1L, updateDto);

        // Then
        assertThat(updatedUser.getFullName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw exception when updating to existing email")
    void shouldThrowWhenUpdatingToExistingEmail() {
        // Given
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== CHANGE PASSWORD Tests ====================

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePassword() {
        // Given
        String oldPass = "oldPass";
        String newPass = "newPass";

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPass, "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPass)).thenReturn("newEncodedPassword");

        // When
        userService.changePassword(1L, oldPass, newPass);

        // Then
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw exception when old password incorrect")
    void shouldThrowWhenOldPasswordIncorrect() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(1L, "wrongPass", "newPass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Old password is incorrect");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== GET & DELETE Tests ====================

    @Test
    @DisplayName("Should get user by username")
    void shouldGetUserByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserByUsername("testuser");

        // Then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("Should throw when user not found by ID")
    void shouldThrowWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).delete(user);
    }
}