package com.example.quizapp.security;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

//User entity for authentication and authorization
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    Username for login (unique)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

//    BCrypt encoded password
    @Column(nullable = false)
    private String password;

//    Email address (unique)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

//    Full name of the user
    @Column(name = "full_name", length = 100)
    private String fullName;

//    User role (USER, ADMIN)
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;

//    Account enabled flag
    @Column(name = "is_enabled")
    @Builder.Default
    private boolean enabled = true;

//    Account locked flag
    @Column(name = "is_locked")
    @Builder.Default
    private boolean locked = false;

//    Account creation timestamp
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

//    Last update timestamp
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}