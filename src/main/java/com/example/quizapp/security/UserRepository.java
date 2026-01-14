package com.example.quizapp.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//    Find user by username
    Optional<User> findByUsername(String username);

//    Find user by email
    Optional<User> findByEmail(String email);

//    Check if username exists
    boolean existsByUsername(String username);

//    Check if email exists
    boolean existsByEmail(String email);

//    Find all enabled users
    List<User> findByEnabledTrue();

//    Find users by role
    List<User> findByRole(UserRole role);

//    Count users by role
    long countByRole(UserRole role);

//    Find all admins
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.enabled = true")
    List<User> findAllAdmins();
}