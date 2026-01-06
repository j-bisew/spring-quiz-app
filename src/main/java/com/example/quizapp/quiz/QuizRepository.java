package com.example.quizapp.quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    /**
     * Find all active quizzes
     */
    List<Quiz> findByActiveTrue();

    /**
     * Find all active quizzes with pagination
     */
    Page<Quiz> findByActiveTrue(Pageable pageable);

    /**
     * Find quiz by id and fetch questions eagerly
     */
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    /**
     * Find quizzes by creator
     */
    List<Quiz> findByCreatedBy(String createdBy);

    /**
     * Find quizzes by creator with pagination
     */
    Page<Quiz> findByCreatedBy(String createdBy, Pageable pageable);

    /**
     * Search quizzes by title (case-insensitive)
     */
    @Query("SELECT q FROM Quiz q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND q.active = true")
    List<Quiz> searchByTitle(@Param("keyword") String keyword);

    /**
     * Search quizzes by title with pagination
     */
    @Query("SELECT q FROM Quiz q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND q.active = true")
    Page<Quiz> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Count active quizzes
     */
    long countByActiveTrue();

    /**
     * Find quizzes with time limit
     */
    @Query("SELECT q FROM Quiz q WHERE q.timeLimitMinutes IS NOT NULL AND q.active = true")
    List<Quiz> findQuizzesWithTimeLimit();

    /**
     * Find quizzes by settings
     */
    List<Quiz> findByRandomQuestionOrderAndActiveTrue(boolean randomQuestionOrder);

    /**
     * Check if quiz exists by title
     */
    boolean existsByTitle(String title);
}