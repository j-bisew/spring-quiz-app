package com.example.quizapp.game;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {

//    Find all results for a specific quiz
    List<GameResult> findByQuizIdAndCompletedTrue(Long quizId);

//    Find all results for a specific quiz with pagination
    Page<GameResult> findByQuizIdAndCompletedTrue(Long quizId, Pageable pageable);

//    Find all results for a specific player
    List<GameResult> findByPlayerIdAndCompletedTrue(Long playerId);

//    Find results by quiz and player
    List<GameResult> findByQuizIdAndPlayerIdAndCompletedTrue(Long quizId, Long playerId);

//    Find top scores for a quiz (leaderboard)
    @Query("SELECT gr FROM GameResult gr WHERE gr.quiz.id = :quizId AND gr.completed = true ORDER BY gr.score DESC, gr.timeTakenSeconds ASC")
    List<GameResult> findTopScoresByQuizId(@Param("quizId") Long quizId, Pageable pageable);

//    Find result by session ID
    Optional<GameResult> findBySessionId(String sessionId);

//    Count results for a quiz
    long countByQuizIdAndCompletedTrue(Long quizId);

//    Count results for a player
    long countByPlayerIdAndCompletedTrue(Long playerId);

//    Get average score for a quiz
    @Query("SELECT AVG(gr.score) FROM GameResult gr WHERE gr.quiz.id = :quizId AND gr.completed = true")
    Double getAverageScoreByQuizId(@Param("quizId") Long quizId);

//    Get the highest score for a quiz
    @Query("SELECT MAX(gr.score) FROM GameResult gr WHERE gr.quiz.id = :quizId AND gr.completed = true")
    Integer getHighestScoreByQuizId(@Param("quizId") Long quizId);

//    Get the lowest score for a quiz
    @Query("SELECT MIN(gr.score) FROM GameResult gr WHERE gr.quiz.id = :quizId AND gr.completed = true")
    Integer getLowestScoreByQuizId(@Param("quizId") Long quizId);

//    Find recent results
    @Query("SELECT gr FROM GameResult gr WHERE gr.completed = true ORDER BY gr.completedAt DESC")
    List<GameResult> findRecentResults(Pageable pageable);

//    Find results by date range
    @Query("SELECT gr FROM GameResult gr WHERE gr.completedAt BETWEEN :startDate AND :endDate AND gr.completed = true")
    List<GameResult> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

//    Count passed attempts for a quiz
    @Query("SELECT COUNT(gr) FROM GameResult gr WHERE gr.quiz.id = :quizId AND gr.completed = true AND gr.percentageScore >= 50.0")
    long countPassedAttempts(@Param("quizId") Long quizId);

//    Get player's best score for a quiz
    @Query("SELECT MAX(gr.score) FROM GameResult gr WHERE gr.quiz.id = :quizId AND gr.player.id = :playerId AND gr.completed = true")
    Integer getPlayerBestScore(@Param("quizId") Long quizId, @Param("playerId") Long playerId);
}