package com.example.quizapp.question;

import com.example.quizapp.common.exception.ResourceNotFoundException;
import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service for Question business logic with type-specific validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuestionMapper questionMapper;
    private final ObjectMapper objectMapper;

    /**
     * Get all questions for a quiz
     */
    public List<QuestionDto> getQuestionsByQuizId(Long quizId) {
        log.info("Fetching questions for quiz id: {}", quizId);
        List<Question> questions = questionRepository.findByQuizIdAndActiveTrue(quizId);
        log.debug("Found {} questions for quiz {}", questions.size(), quizId);
        return questions.stream()
                .map(questionMapper::toDto)
                .toList();
    }

    /**
     * Get questions ordered by position
     */
    public List<QuestionDto> getQuestionsOrderedByPosition(Long quizId) {
        log.info("Fetching ordered questions for quiz id: {}", quizId);
        List<Question> questions = questionRepository.findByQuizIdOrderedByPosition(quizId);
        return questions.stream()
                .map(questionMapper::toDto)
                .toList();
    }

    /**
     * Get question by ID
     */
    public QuestionDto getQuestionById(Long id) {
        log.info("Fetching question with id: {}", id);
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Question not found with id: {}", id);
                    return new ResourceNotFoundException("Question not found with id: " + id);
                });
        return questionMapper.toDto(question);
    }

    /**
     * Create new question with type-specific validation
     */
    @Transactional
    public QuestionDto createQuestion(QuestionDto questionDto) {
        log.info("Creating new question for quiz id: {}", questionDto.getQuizId());

        // Verify quiz exists
        Quiz quiz = quizRepository.findById(questionDto.getQuizId())
                .orElseThrow(() -> {
                    log.error("Quiz not found with id: {}", questionDto.getQuizId());
                    return new ResourceNotFoundException("Quiz not found with id: " + questionDto.getQuizId());
                });

        // Validate question based on type
        validateQuestionByType(questionDto);

        // Set question order if not provided
        if (questionDto.getQuestionOrder() == null) {
            Integer nextOrder = questionRepository.findNextQuestionOrder(questionDto.getQuizId());
            questionDto.setQuestionOrder(nextOrder);
        }

        Question question = questionMapper.toEntity(questionDto);
        question.setQuiz(quiz);
        question.setActive(true);

        Question savedQuestion = questionRepository.save(question);
        log.info("Question created successfully with id: {}", savedQuestion.getId());

        return questionMapper.toDto(savedQuestion);
    }

    /**
     * Update existing question
     */
    @Transactional
    public QuestionDto updateQuestion(Long id, QuestionDto questionDto) {
        log.info("Updating question with id: {}", id);

        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Question not found with id: {}", id);
                    return new ResourceNotFoundException("Question not found with id: " + id);
                });

        // Validate question based on type
        validateQuestionByType(questionDto);

        // Update fields
        existingQuestion.setQuestionType(questionDto.getQuestionType());
        existingQuestion.setQuestionText(questionDto.getQuestionText());
        existingQuestion.setPoints(questionDto.getPoints());
        existingQuestion.setNegativePoints(questionDto.getNegativePoints());
        existingQuestion.setQuestionOrder(questionDto.getQuestionOrder());
        existingQuestion.setTimeLimitSeconds(questionDto.getTimeLimitSeconds());
        existingQuestion.setAnswerOptions(questionDto.getAnswerOptions());
        existingQuestion.setCorrectAnswer(questionDto.getCorrectAnswer());
        existingQuestion.setExplanation(questionDto.getExplanation());
        existingQuestion.setImageUrl(questionDto.getImageUrl());

        Question updatedQuestion = questionRepository.save(existingQuestion);
        log.info("Question updated successfully: {}", updatedQuestion.getId());

        return questionMapper.toDto(updatedQuestion);
    }

    /**
     * Delete question (soft delete)
     */
    @Transactional
    public void deleteQuestion(Long id) {
        log.info("Deleting question with id: {}", id);

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Question not found with id: {}", id);
                    return new ResourceNotFoundException("Question not found with id: " + id);
                });

        question.setActive(false);
        questionRepository.save(question);
        log.info("Question soft-deleted successfully: {}", question.getId());
    }

    /**
     * Permanently delete question
     */
    @Transactional
    public void permanentlyDeleteQuestion(Long id) {
        log.info("Permanently deleting question with id: {}", id);

        if (!questionRepository.existsById(id)) {
            log.error("Question not found with id: {}", id);
            throw new ResourceNotFoundException("Question not found with id: " + id);
        }

        questionRepository.deleteById(id);
        log.info("Question permanently deleted with id: {}", id);
    }

    /**
     * Count questions in a quiz
     */
    public long countQuestions(Long quizId) {
        long count = questionRepository.countByQuizIdAndActiveTrue(quizId);
        log.debug("Quiz {} has {} questions", quizId, count);
        return count;
    }

    /**
     * Get total points for a quiz
     */
    public int getTotalPoints(Long quizId) {
        Integer total = questionRepository.getTotalPointsByQuizId(quizId);
        return total != null ? total : 0;
    }

    /**
     * Validate answer for a question
     * Returns true if answer is correct
     */
    public boolean validateAnswer(Long questionId, String userAnswer) {
        log.info("Validating answer for question id: {}", questionId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        return validateAnswerByType(question, userAnswer);
    }

    /**
     * Type-specific validation for question creation/update
     */
    private void validateQuestionByType(QuestionDto questionDto) {
        QuestionType type = questionDto.getQuestionType();
        String answerOptions = questionDto.getAnswerOptions();
        String correctAnswer = questionDto.getCorrectAnswer();

        log.debug("Validating question type: {}", type);

        try {
            switch (type) {
                case SINGLE_CHOICE, DROPDOWN -> validateSingleChoiceQuestion(answerOptions, correctAnswer);
                case MULTIPLE_CHOICE -> validateMultipleChoiceQuestion(answerOptions, correctAnswer);
                case TRUE_FALSE -> validateTrueFalseQuestion(answerOptions, correctAnswer);
                case SHORT_ANSWER -> validateShortAnswerQuestion(correctAnswer);
                case FILL_BLANKS -> validateFillBlanksQuestion(answerOptions, correctAnswer);
                case SORTING -> validateSortingQuestion(answerOptions, correctAnswer);
                case MATCHING -> validateMatchingQuestion(answerOptions, correctAnswer);
                default -> throw new IllegalArgumentException("Unknown question type: " + type);
            }
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON format in question data", e);
            throw new IllegalArgumentException("Invalid JSON format in question data: " + e.getMessage());
        }
    }

    /**
     * Validate Single Choice / Dropdown question
     */
    private void validateSingleChoiceQuestion(String answerOptions, String correctAnswer) throws JsonProcessingException {
        List<String> options = objectMapper.readValue(answerOptions, new TypeReference<>() {});

        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("Single choice question must have at least 2 options");
        }

        int correctIndex = Integer.parseInt(correctAnswer);
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("Correct answer index is out of range");
        }
    }

    /**
     * Validate Multiple Choice question
     */
    private void validateMultipleChoiceQuestion(String answerOptions, String correctAnswer) throws JsonProcessingException {
        List<String> options = objectMapper.readValue(answerOptions, new TypeReference<>() {});
        List<Integer> correctIndexes = objectMapper.readValue(correctAnswer, new TypeReference<>() {});

        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("Multiple choice question must have at least 2 options");
        }

        if (correctIndexes == null || correctIndexes.isEmpty()) {
            throw new IllegalArgumentException("Multiple choice question must have at least one correct answer");
        }

        for (int index : correctIndexes) {
            if (index < 0 || index >= options.size()) {
                throw new IllegalArgumentException("Correct answer index is out of range");
            }
        }
    }

    /**
     * Validate True/False question
     */
    private void validateTrueFalseQuestion(String answerOptions, String correctAnswer) throws JsonProcessingException {
        List<String> options = objectMapper.readValue(answerOptions, new TypeReference<>() {});

        if (options == null || options.size() != 2) {
            throw new IllegalArgumentException("True/False question must have exactly 2 options");
        }

        int correctIndex = Integer.parseInt(correctAnswer);
        if (correctIndex < 0 || correctIndex > 1) {
            throw new IllegalArgumentException("Correct answer must be 0 (True) or 1 (False)");
        }
    }

    /**
     * Validate Short Answer question
     */
    private void validateShortAnswerQuestion(String correctAnswer) {
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            throw new IllegalArgumentException("Short answer question must have a correct answer");
        }
    }

    /**
     * Validate Fill Blanks question
     */
    private void validateFillBlanksQuestion(String answerOptions, String correctAnswer) throws JsonProcessingException {
        List<String> blanks = objectMapper.readValue(answerOptions, new TypeReference<>() {});
        List<String> answers = objectMapper.readValue(correctAnswer, new TypeReference<>() {});

        if (blanks == null || blanks.isEmpty()) {
            throw new IllegalArgumentException("Fill blanks question must have at least one blank");
        }

        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Fill blanks question must have answers for all blanks");
        }

        if (blanks.size() != answers.size()) {
            throw new IllegalArgumentException("Number of blanks must match number of answers");
        }
    }

    /**
     * Validate Sorting question
     */
    private void validateSortingQuestion(String answerOptions, String correctAnswer) throws JsonProcessingException {
        List<String> items = objectMapper.readValue(answerOptions, new TypeReference<>() {});
        List<Integer> correctOrder = objectMapper.readValue(correctAnswer, new TypeReference<>() {});

        if (items == null || items.size() < 2) {
            throw new IllegalArgumentException("Sorting question must have at least 2 items");
        }

        if (correctOrder == null || correctOrder.size() != items.size()) {
            throw new IllegalArgumentException("Correct order must include all items");
        }
    }

    /**
     * Validate Matching question
     */
    private void validateMatchingQuestion(String answerOptions, String correctAnswer) throws JsonProcessingException {
        List<Map<String, String>> pairs = objectMapper.readValue(answerOptions, new TypeReference<>() {});
        List<Map<String, String>> correctPairs = objectMapper.readValue(correctAnswer, new TypeReference<>() {});

        if (pairs == null || pairs.size() < 2) {
            throw new IllegalArgumentException("Matching question must have at least 2 pairs");
        }

        if (correctPairs == null || correctPairs.isEmpty()) {
            throw new IllegalArgumentException("Matching question must have correct pair mappings");
        }
    }

    /**
     * Type-specific answer validation
     */
    private boolean validateAnswerByType(Question question, String userAnswer) {
        try {
            return switch (question.getQuestionType()) {
                case SINGLE_CHOICE, DROPDOWN, TRUE_FALSE ->
                        validateSingleChoiceAnswer(question.getCorrectAnswer(), userAnswer);
                case MULTIPLE_CHOICE ->
                        validateMultipleChoiceAnswer(question.getCorrectAnswer(), userAnswer);
                case SHORT_ANSWER ->
                        validateShortAnswerAnswer(question.getCorrectAnswer(), userAnswer);
                case FILL_BLANKS ->
                        validateFillBlanksAnswer(question.getCorrectAnswer(), userAnswer);
                case SORTING ->
                        validateSortingAnswer(question.getCorrectAnswer(), userAnswer);
                case MATCHING ->
                        validateMatchingAnswer(question.getCorrectAnswer(), userAnswer);
            };
        } catch (Exception e) {
            log.error("Error validating answer", e);
            return false;
        }
    }

    private boolean validateSingleChoiceAnswer(String correctAnswer, String userAnswer) {
        return correctAnswer.equals(userAnswer);
    }

    private boolean validateMultipleChoiceAnswer(String correctAnswer, String userAnswer) throws JsonProcessingException {
        List<Integer> correct = objectMapper.readValue(correctAnswer, new TypeReference<>() {});
        List<Integer> user = objectMapper.readValue(userAnswer, new TypeReference<>() {});
        return correct.size() == user.size() && correct.containsAll(user);
    }

    private boolean validateShortAnswerAnswer(String correctAnswer, String userAnswer) {
        return correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
    }

    private boolean validateFillBlanksAnswer(String correctAnswer, String userAnswer) throws JsonProcessingException {
        List<String> correct = objectMapper.readValue(correctAnswer, new TypeReference<>() {});
        List<String> user = objectMapper.readValue(userAnswer, new TypeReference<>() {});

        if (correct.size() != user.size()) return false;

        for (int i = 0; i < correct.size(); i++) {
            if (!correct.get(i).trim().equalsIgnoreCase(user.get(i).trim())) {
                return false;
            }
        }
        return true;
    }

    private boolean validateSortingAnswer(String correctAnswer, String userAnswer) throws JsonProcessingException {
        List<Integer> correct = objectMapper.readValue(correctAnswer, new TypeReference<>() {});
        List<Integer> user = objectMapper.readValue(userAnswer, new TypeReference<>() {});
        return correct.equals(user);
    }

    private boolean validateMatchingAnswer(String correctAnswer, String userAnswer) throws JsonProcessingException {
        List<Map<String, String>> correct = objectMapper.readValue(correctAnswer, new TypeReference<>() {});
        List<Map<String, String>> user = objectMapper.readValue(userAnswer, new TypeReference<>() {});

        if (correct.size() != user.size()) return false;

        return correct.containsAll(user) && user.containsAll(correct);
    }
}