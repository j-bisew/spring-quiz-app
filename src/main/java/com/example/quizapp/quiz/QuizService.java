package com.example.quizapp.quiz;

import com.example.quizapp.common.exception.QuestionNotFoundException;
import com.example.quizapp.common.exception.ResourceNotFoundException;
import com.example.quizapp.question.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for Quiz business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

    /**
     * Get all active quizzes
     */
    public List<QuizDto> getAllActiveQuizzes() {
        log.info("Fetching all active quizzes");
        List<Quiz> quizzes = quizRepository.findByActiveTrue();
        log.debug("Found {} active quizzes", quizzes.size());
        return quizzes.stream()
                .map(quizMapper::toDto)
                .toList();
    }

    /**
     * Get all active quizzes with pagination
     */
    public Page<QuizDto> getAllActiveQuizzes(Pageable pageable) {
        log.info("Fetching active quizzes with pagination: {}", pageable);
        Page<Quiz> quizzes = quizRepository.findByActiveTrue(pageable);
        log.debug("Found {} active quizzes on page {}", quizzes.getNumberOfElements(), pageable.getPageNumber());
        return quizzes.map(quizMapper::toDto);
    }

    /**
     * Get quiz by ID
     */
    public QuizDto getQuizById(Long id) {
        log.info("Fetching quiz with id: {}", id);
        Quiz quiz = quizExists(id);
        log.debug("Found quiz: {}", quiz.getTitle());
        return quizMapper.toDto(quiz);
    }

    /**
     * Get quiz by ID with questions eagerly loaded
     */
    public QuizDto getQuizByIdWithQuestions(Long id) {
        log.info("Fetching quiz with questions, id: {}", id);
        Quiz quiz = quizExists(id);
        log.debug("Found quiz with {} questions: {}", quiz.getQuestions().size(), quiz.getTitle());
        return quizMapper.toDto(quiz);
    }

    /**
     * Create new quiz
     */
    @Transactional
    public QuizDto createQuiz(QuizDto quizDto) {
        log.info("Creating new quiz: {}", quizDto.getTitle());

        // Check if quiz with same title already exists
        if (quizRepository.existsByTitle(quizDto.getTitle())) {
            log.error("Quiz with title '{}' already exists", quizDto.getTitle());
            throw new IllegalArgumentException("Quiz with title '" + quizDto.getTitle() + "' already exists");
        }

        Quiz quiz = quizMapper.toEntity(quizDto);
        quiz.setActive(true);

        Quiz savedQuiz = quizRepository.save(quiz);
        log.info("Quiz created successfully with id: {}", savedQuiz.getId());

        return quizMapper.toDto(savedQuiz);
    }

    /**
     * Update existing quiz
     */
    @Transactional
    public QuizDto updateQuiz(Long id, QuizDto quizDto) {
        log.info("Updating quiz with id: {}", id);

        Quiz existingQuiz = quizExists(id);

        // Check if title is being changed to an existing title
        if (!existingQuiz.getTitle().equals(quizDto.getTitle()) &&
                quizRepository.existsByTitle(quizDto.getTitle())) {
            log.error("Quiz with title '{}' already exists", quizDto.getTitle());
            throw new IllegalArgumentException("Quiz with title '" + quizDto.getTitle() + "' already exists");
        }

        // Update fields
        existingQuiz.setTitle(quizDto.getTitle());
        existingQuiz.setDescription(quizDto.getDescription());
        existingQuiz.setRandomQuestionOrder(quizDto.isRandomQuestionOrder());
        existingQuiz.setRandomAnswerOrder(quizDto.isRandomAnswerOrder());
        existingQuiz.setTimeLimitMinutes(quizDto.getTimeLimitMinutes());
        existingQuiz.setNegativePointsEnabled(quizDto.isNegativePointsEnabled());
        existingQuiz.setBackButtonBlocked(quizDto.isBackButtonBlocked());
        existingQuiz.setActive(quizDto.isActive());

        Quiz updatedQuiz = quizRepository.save(existingQuiz);
        log.info("Quiz updated successfully: {}", updatedQuiz.getTitle());

        return quizMapper.toDto(updatedQuiz);
    }

    /**
     * Delete quiz (soft delete - set active to false)
     */
    @Transactional
    public void deleteQuiz(Long id) {
        log.info("Deleting quiz with id: {}", id);

        Quiz quiz = quizExists(id);

        quiz.setActive(false);
        quizRepository.save(quiz);
        log.info("Quiz soft-deleted successfully: {}", quiz.getTitle());
    }

    /**
     * Permanently delete quiz
     */
    @Transactional
    public void permanentlyDeleteQuiz(Long id) {
        log.info("Permanently deleting quiz with id: {}", id);

        quizExists(id);

        quizRepository.deleteById(id);
        log.info("Quiz permanently deleted with id: {}", id);
    }

    /**
     * Search quizzes by title
     */
    public List<QuizDto> searchQuizzes(String keyword) {
        log.info("Searching quizzes with keyword: {}", keyword);
        List<Quiz> quizzes = quizRepository.searchByTitle(keyword);
        log.debug("Found {} quizzes matching keyword: {}", quizzes.size(), keyword);
        return quizzes.stream()
                .map(quizMapper::toDto)
                .toList();
    }

    /**
     * Search quizzes by title with pagination
     */
    public Page<QuizDto> searchQuizzes(String keyword, Pageable pageable) {
        log.info("Searching quizzes with keyword: {} and pagination", keyword);
        Page<Quiz> quizzes = quizRepository.searchByTitle(keyword, pageable);
        log.debug("Found {} quizzes on page {}", quizzes.getNumberOfElements(), pageable.getPageNumber());
        return quizzes.map(quizMapper::toDto);
    }

    /**
     * Get quizzes by creator
     */
    public List<QuizDto> getQuizzesByCreator(String createdBy) {
        log.info("Fetching quizzes created by: {}", createdBy);
        List<Quiz> quizzes = quizRepository.findByCreatedBy(createdBy);
        log.debug("Found {} quizzes created by: {}", quizzes.size(), createdBy);
        return quizzes.stream()
                .map(quizMapper::toDto)
                .toList();
    }

    /**
     * Count active quizzes
     */
    public long countActiveQuizzes() {
        long count = quizRepository.countByActiveTrue();
        log.debug("Total active quizzes: {}", count);
        return count;
    }

    private Quiz quizExists(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> {
                    log.error("Question not found with id: {}", quizId);
                    return new QuestionNotFoundException(quizId);
                });
    }
}