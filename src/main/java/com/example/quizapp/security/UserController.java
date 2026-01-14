package com.example.quizapp.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

//REST Controller for user management
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user authentication and management")
@SecurityRequirement(name = "basicAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

//    Register new user
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody @Parameter(description = "User registration data")
            UserRegistrationDto registrationDto) {
        log.info("POST /api/v1/users/register - Registering new user: {}", registrationDto.getUsername());

        User user = userService.createUser(registrationDto);
        UserDto userDto = userMapper.toDto(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

//    Get current user profile
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns current authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/v1/users/me - Getting current user");

        User user = userService.getUserById(userDetails.getId());
        UserDto userDto = userMapper.toDto(user);

        return ResponseEntity.ok(userDto);
    }

//    Get all users (Admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Returns all users (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("GET /api/v1/users - Getting all users");

        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }

//    Get user by ID (Admin only)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Returns user by ID (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> getUserById(
            @PathVariable @Parameter(description = "User ID") Long id) {
        log.info("GET /api/v1/users/{} - Getting user by id", id);

        User user = userService.getUserById(id);
        UserDto userDto = userMapper.toDto(user);

        return ResponseEntity.ok(userDto);
    }

//    Update user (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Updates user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> updateUser(
            @PathVariable @Parameter(description = "User ID") Long id,
            @Valid @RequestBody @Parameter(description = "User update data") UserUpdateDto updateDto) {
        log.info("PUT /api/v1/users/{} - Updating user", id);

        User user = userService.updateUser(id, updateDto);
        UserDto userDto = userMapper.toDto(user);

        return ResponseEntity.ok(userDto);
    }

//    Delete user (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes user (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Parameter(description = "User ID") Long id) {
        log.info("DELETE /api/v1/users/{} - Deleting user", id);

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

//    Check if username exists
    @GetMapping("/check-username")
    @Operation(summary = "Check username availability", description = "Checks if username is already taken")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Boolean> checkUsername(
            @RequestParam @Parameter(description = "Username to check") String username) {
        log.info("GET /api/v1/users/check-username?username={}", username);

        boolean exists = userService.usernameExists(username);

        return ResponseEntity.ok(exists);
    }

//    Check if email exists
    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", description = "Checks if email is already registered")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Boolean> checkEmail(
            @RequestParam @Parameter(description = "Email to check") String email) {
        log.info("GET /api/v1/users/check-email?email={}", email);

        boolean exists = userService.emailExists(email);

        return ResponseEntity.ok(exists);
    }
}