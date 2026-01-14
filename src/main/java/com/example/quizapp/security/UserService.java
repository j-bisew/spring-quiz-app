package com.example.quizapp.security;

import com.example.quizapp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    Get all users
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

//    Get user by ID
    public User getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

//    Get user by username
    public User getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

//    Create new user
    @Transactional
    public User createUser(UserRegistrationDto registrationDto) {
        log.info("Creating new user: {}", registrationDto.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + registrationDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registrationDto.getEmail());
        }

        User user = User.builder()
                .username(registrationDto.getUsername())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .email(registrationDto.getEmail())
                .fullName(registrationDto.getFullName())
                .role(registrationDto.getRole() != null ? registrationDto.getRole() : UserRole.USER)
                .enabled(true)
                .locked(false)
                .build();

        user = userRepository.save(user);
        log.info("User created successfully with id: {}", user.getId());

        return user;
    }

//    Update user
    @Transactional
    public User updateUser(Long id, UserUpdateDto updateDto) {
        log.info("Updating user with id: {}", id);

        User user = getUserById(id);

        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + updateDto.getEmail());
            }
            user.setEmail(updateDto.getEmail());
        }

        if (updateDto.getFullName() != null) {
            user.setFullName(updateDto.getFullName());
        }

        if (updateDto.getRole() != null) {
            user.setRole(updateDto.getRole());
        }

        if (updateDto.getEnabled() != null) {
            user.setEnabled(updateDto.getEnabled());
        }

        if (updateDto.getLocked() != null) {
            user.setLocked(updateDto.getLocked());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: {}", user.getUsername());

        return user;
    }

//    Change password
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user id: {}", userId);

        User user = getUserById(userId);

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getUsername());
    }

//    Delete user
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        User user = getUserById(id);
        userRepository.delete(user);

        log.info("User deleted successfully: {}", user.getUsername());
    }

//    Check if username exists
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

//    Check if email exists
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

//    Get all admins
    public List<User> getAllAdmins() {
        log.info("Fetching all admins");
        return userRepository.findAllAdmins();
    }

//    Count users by role
    public long countUsersByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
}