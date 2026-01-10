package com.example.quizapp.player;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


//    Player nickname (not unique - multiple players can have same nickname)
    @Column(nullable = false, length = 50)
    private String nickname;

//    Optional email for notifications (not required)
    @Column(length = 100)
    private String email;

//    Session identifier for tracking player across games
    @Column(name = "session_id", length = 100)
    private String sessionId;

//    IP address for tracking (optional)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

//    First time player played
    @CreationTimestamp
    @Column(name = "first_played_at", updatable = false)
    private LocalDateTime firstPlayedAt;

//    Last time player played
    @Column(name = "last_played_at")
    private LocalDateTime lastPlayedAt;

//    Total number of games played
    @Column(name = "games_played")
    @Builder.Default
    private int gamesPlayed = 0;

//    Player is active
    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
}