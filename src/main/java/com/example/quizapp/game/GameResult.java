package com.example.quizapp.game;

import com.example.quizapp.player.Player;
import com.example.quizapp.quiz.Quiz;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    Player who played the quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

//    Quiz that was played
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

//    Total score achieved
    @Column(nullable = false)
    private int score;

//    Maximum possible score
    @Column(name = "max_score", nullable = false)
    private int maxScore;

//    Number of correct answers
    @Column(name = "correct_answers")
    private int correctAnswers;

//    Number of wrong answers
    @Column(name = "wrong_answers")
    private int wrongAnswers;

//    Total number of questions
    @Column(name = "total_questions")
    private int totalQuestions;

//    Time taken to complete quiz in seconds
    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

//    Percentage score (calculated)
    @Column(name = "percentage_score")
    private Double percentageScore;

//    Detailed answers stored as JSON
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;

//    Game session identifier
    @Column(name = "session_id", length = 100)
    private String sessionId;

//    When the game was started
    @Column(name = "started_at")
    private LocalDateTime startedAt;

//    When the game was completed
    @CreationTimestamp
    @Column(name = "completed_at", updatable = false)
    private LocalDateTime completedAt;

//    Whether the game was completed or abandoned
    @Column(name = "is_completed")
    @Builder.Default
    private boolean completed = true;

//    IP address of player (for tracking)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

//    Calculate percentage score
    @PrePersist
    @PreUpdate
    public void calculatePercentage() {
        if (maxScore > 0) {
            this.percentageScore = (double) score / maxScore * 100;
        } else {
            this.percentageScore = 0.0;
        }
    }

//    Check if player passed (>= 50%)
    public boolean isPassed() {
        return percentageScore != null && percentageScore >= 50.0;
    }

//    Get grade based on percentage
    public String getGrade() {
        if (percentageScore == null) return "N/A";
        if (percentageScore >= 90) return "A";
        if (percentageScore >= 80) return "B";
        if (percentageScore >= 70) return "C";
        if (percentageScore >= 60) return "D";
        if (percentageScore >= 50) return "E";
        return "F";
    }
}