package com.example.quizapp.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

//    Find all questions for a specific quiz
    List<Question> findByQuizIdAndActiveTrue(Long quizId);

//    Find all questions for a specific quiz with pagination
    Page<Question> findByQuizIdAndActiveTrue(Long quizId, Pageable pageable);

//    Find questions by quiz ID ordered by questionOrder
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND q.active = true ORDER BY q.questionOrder ASC")
    List<Question> findByQuizIdOrderedByPosition(@Param("quizId") Long quizId);

//    Find questions by type
    List<Question> findByQuestionTypeAndActiveTrue(QuestionType questionType);

//    Find questions by quiz and type
    List<Question> findByQuizIdAndQuestionTypeAndActiveTrue(Long quizId, QuestionType questionType);

//    Count questions in a quiz
    long countByQuizIdAndActiveTrue(Long quizId);

//    Count questions by type in a quiz
    long countByQuizIdAndQuestionTypeAndActiveTrue(Long quizId, QuestionType questionType);

//    Find questions with time limit
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND q.timeLimitSeconds IS NOT NULL AND q.active = true")
    List<Question> findQuestionsWithTimeLimit(@Param("quizId") Long quizId);

//    Get total points for a quiz
    @Query("SELECT SUM(q.points) FROM Question q WHERE q.quiz.id = :quizId AND q.active = true")
    Integer getTotalPointsByQuizId(@Param("quizId") Long quizId);

//    Find question by ID and quiz ID (for security)
    Optional<Question> findByIdAndQuizId(Long id, Long quizId);

//    Delete all questions for a quiz
    void deleteByQuizId(Long quizId);

//    Check if question exists in quiz
    boolean existsByIdAndQuizId(Long id, Long quizId);

//    Find next question order number for quiz
    @Query("SELECT COALESCE(MAX(q.questionOrder), 0) + 1 FROM Question q WHERE q.quiz.id = :quizId")
    Integer findNextQuestionOrder(@Param("quizId") Long quizId);

//    Search questions by text in a quiz
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%')) AND q.active = true")
    List<Question> searchInQuiz(@Param("quizId") Long quizId, @Param("keyword") String keyword);
}