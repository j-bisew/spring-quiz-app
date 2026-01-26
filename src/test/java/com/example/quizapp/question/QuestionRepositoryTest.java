package com.example.quizapp.question;

import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("QuestionRepository Tests")
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    private Quiz testQuiz;
    private Question question1;
    private Question question2;
    private Question inactiveQuestion;

    @BeforeEach
    void setUp() {
        // Clear repositories
        questionRepository.deleteAll();
        quizRepository.deleteAll();

        // Create test quiz
        testQuiz = Quiz.builder()
                .title("Java Quiz")
                .description("Test quiz")
                .active(true)
                .createdBy("testuser")
                .build();
        testQuiz = quizRepository.save(testQuiz);

        // Create test questions
        question1 = Question.builder()
                .quiz(testQuiz)
                .questionText("What is Java?")
                .questionType(QuestionType.SINGLE_CHOICE)
                .answerOptions("[\"A programming language\",\"A coffee\",\"An island\"]")
                .correctAnswer("0")
                .points(10)
                .questionOrder(1)
                .active(true)
                .build();

        question2 = Question.builder()
                .quiz(testQuiz)
                .questionText("Is Java compiled?")
                .questionType(QuestionType.TRUE_FALSE)
                .answerOptions("[\"True\",\"False\"]")
                .correctAnswer("True")
                .points(5)
                .questionOrder(2)
                .active(true)
                .build();

        inactiveQuestion = Question.builder()
                .quiz(testQuiz)
                .questionText("Inactive question")
                .questionType(QuestionType.SHORT_ANSWER)
                .correctAnswer("Answer")
                .points(5)
                .questionOrder(3)
                .active(false)
                .build();
    }

    // ==================== CREATE Tests ====================

    @Test
    @DisplayName("Should save question successfully")
    void shouldSaveQuestion() {
        // When
        Question savedQuestion = questionRepository.save(question1);

        // Then
        assertThat(savedQuestion).isNotNull();
        assertThat(savedQuestion.getId()).isNotNull();
        assertThat(savedQuestion.getQuestionText()).isEqualTo("What is Java?");
        assertThat(savedQuestion.getQuiz()).isEqualTo(testQuiz);
    }

    @Test
    @DisplayName("Should save question with all question types")
    void shouldSaveQuestionWithAllTypes() {
        // Given - test all 8 question types
        QuestionType[] types = QuestionType.values();

        // When & Then
        for (int i = 0; i < types.length; i++) {
            Question question = Question.builder()
                    .quiz(testQuiz)
                    .questionText("Question " + i)
                    .questionType(types[i])
                    .correctAnswer("Answer")
                    .points(10)
                    .questionOrder(i + 1)
                    .active(true)
                    .build();

            Question saved = questionRepository.save(question);
            assertThat(saved.getQuestionType()).isEqualTo(types[i]);
        }
    }

    // ==================== READ Tests ====================

    @Test
    @DisplayName("Should find question by ID")
    void shouldFindQuestionById() {
        // Given
        Question savedQuestion = questionRepository.save(question1);

        // When
        Optional<Question> foundQuestion = questionRepository.findById(savedQuestion.getId());

        // Then
        assertThat(foundQuestion).isPresent();
        assertThat(foundQuestion.get().getQuestionText()).isEqualTo("What is Java?");
    }

    @Test
    @DisplayName("Should return empty when question not found")
    void shouldReturnEmptyWhenQuestionNotFound() {
        // When
        Optional<Question> foundQuestion = questionRepository.findById(999L);

        // Then
        assertThat(foundQuestion).isEmpty();
    }

    @Test
    @DisplayName("Should find all questions")
    void shouldFindAllQuestions() {
        // Given
        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(inactiveQuestion);

        // When
        List<Question> allQuestions = questionRepository.findAll();

        // Then
        assertThat(allQuestions).hasSize(3);
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("Should update question text")
    void shouldUpdateQuestionText() {
        // Given
        Question savedQuestion = questionRepository.save(question1);

        // When
        savedQuestion.setQuestionText("Updated: What is Java?");
        Question updatedQuestion = questionRepository.save(savedQuestion);

        // Then
        assertThat(updatedQuestion.getQuestionText()).isEqualTo("Updated: What is Java?");
    }

    @Test
    @DisplayName("Should update question points")
    void shouldUpdateQuestionPoints() {
        // Given
        Question savedQuestion = questionRepository.save(question1);

        // When
        savedQuestion.setPoints(20);
        Question updatedQuestion = questionRepository.save(savedQuestion);

        // Then
        assertThat(updatedQuestion.getPoints()).isEqualTo(20);
    }

    // ==================== DELETE Tests ====================

    @Test
    @DisplayName("Should delete question by ID")
    void shouldDeleteQuestionById() {
        // Given
        Question savedQuestion = questionRepository.save(question1);
        Long questionId = savedQuestion.getId();

        // When
        questionRepository.deleteById(questionId);

        // Then
        Optional<Question> deletedQuestion = questionRepository.findById(questionId);
        assertThat(deletedQuestion).isEmpty();
    }

    @Test
    @DisplayName("Should delete question entity")
    void shouldDeleteQuestionEntity() {
        // Given
        Question savedQuestion = questionRepository.save(question1);
        Long questionId = savedQuestion.getId();

        // When
        questionRepository.delete(savedQuestion);

        // Then
        Optional<Question> deletedQuestion = questionRepository.findById(questionId);
        assertThat(deletedQuestion).isEmpty();
    }

    // ==================== Custom Query Tests ====================

    @Test
    @DisplayName("Should find questions by quiz ID")
    void shouldFindQuestionsByQuizId() {
        // Given
        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(inactiveQuestion);

        // When
        List<Question> questions = questionRepository.findByQuizIdAndActiveTrue(testQuiz.getId());

        // Then
        assertThat(questions).hasSize(2); // Only active questions
    }

    @Test
    @DisplayName("Should find active questions by quiz ID")
    void shouldFindActiveQuestionsByQuizId() {
        // Given
        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(inactiveQuestion);

        // When
        List<Question> activeQuestions = questionRepository.findByQuizIdAndActiveTrue(testQuiz.getId());

        // Then
        assertThat(activeQuestions).hasSize(2);
        assertThat(activeQuestions)
                .extracting(Question::getQuestionText)
                .containsExactlyInAnyOrder("What is Java?", "Is Java compiled?");
    }

    @Test
    @DisplayName("Should find questions ordered by order")
    void shouldFindQuestionsOrderedByOrder() {
        // Given
        questionRepository.save(question2); // order 2
        questionRepository.save(question1); // order 1

        // When
        List<Question> questions = questionRepository.findByQuizIdOrderedByPosition(testQuiz.getId());

        // Then
        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).getQuestionOrder()).isEqualTo(1);
        assertThat(questions.get(1).getQuestionOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find questions by question type")
    void shouldFindQuestionsByQuestionType() {
        // Given
        questionRepository.save(question1); // SINGLE_CHOICE
        questionRepository.save(question2); // TRUE_FALSE

        // When
        List<Question> singleChoiceQuestions = questionRepository.findByQuestionTypeAndActiveTrue(QuestionType.SINGLE_CHOICE);

        // Then
        assertThat(singleChoiceQuestions).hasSize(1);
        assertThat(singleChoiceQuestions.getFirst().getQuestionType()).isEqualTo(QuestionType.SINGLE_CHOICE);
    }

    @Test
    @DisplayName("Should count questions by quiz ID")
    void shouldCountQuestionsByQuizId() {
        // Given
        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(inactiveQuestion);

        // When
        long count = questionRepository.countByQuizIdAndActiveTrue(testQuiz.getId());

        // Then
        assertThat(count).isEqualTo(2); // Only active questions
    }

    @Test
    @DisplayName("Should sum total points for quiz")
    void shouldSumTotalPointsForQuiz() {
        // Given
        questionRepository.save(question1); // 10 points
        questionRepository.save(question2); // 5 points

        // When
        Integer totalPoints = questionRepository.getTotalPointsByQuizId(testQuiz.getId());

        // Then
        assertThat(totalPoints).isEqualTo(15);
    }

    @Test
    @DisplayName("Should return zero when no questions for quiz")
    void shouldReturnZeroWhenNoQuestionsForQuiz() {
        // Given - empty quiz
        Quiz emptyQuiz = Quiz.builder()
                .title("Empty Quiz")
                .active(true)
                .createdBy("testuser")
                .build();
        emptyQuiz = quizRepository.save(emptyQuiz);

        // When
        long count = questionRepository.countByQuizIdAndActiveTrue(emptyQuiz.getId());

        // Then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Should delete all questions by quiz ID")
    void shouldDeleteAllQuestionsByQuizId() {
        // Given
        questionRepository.save(question1);
        questionRepository.save(question2);

        // When
        questionRepository.deleteByQuizId(testQuiz.getId());

        // Then
        List<Question> remainingQuestions = questionRepository.findByQuizIdAndActiveTrue(testQuiz.getId());
        assertThat(remainingQuestions).isEmpty();
    }

    @Test
    @DisplayName("Should maintain question order after updates")
    void shouldMaintainQuestionOrderAfterUpdates() {
        // Given
        Question saved1 = questionRepository.save(question1);
        Question saved2 = questionRepository.save(question2);

        // When - swap orders
        saved1.setQuestionOrder(2);
        saved2.setQuestionOrder(1);
        questionRepository.save(saved1);
        questionRepository.save(saved2);

        // Then
        List<Question> orderedQuestions = questionRepository.findByQuizIdOrderedByPosition(testQuiz.getId());
        assertThat(orderedQuestions.get(0).getQuestionText()).isEqualTo("Is Java compiled?");
        assertThat(orderedQuestions.get(1).getQuestionText()).isEqualTo("What is Java?");
    }

    @Test
    @DisplayName("Should find questions by quiz and type")
    void shouldFindQuestionsByQuizAndType() {
        // Given
        questionRepository.save(question1); // SINGLE_CHOICE
        questionRepository.save(question2); // TRUE_FALSE

        // When
        List<Question> singleChoice = questionRepository.findByQuizIdAndQuestionTypeAndActiveTrue(
                testQuiz.getId(), QuestionType.SINGLE_CHOICE);

        // Then
        assertThat(singleChoice).hasSize(1);
        assertThat(singleChoice.getFirst().getQuestionType()).isEqualTo(QuestionType.SINGLE_CHOICE);
    }

    @Test
    @DisplayName("Should count questions by type")
    void shouldCountQuestionsByType() {
        // Given
        questionRepository.save(question1); // SINGLE_CHOICE
        questionRepository.save(question2); // TRUE_FALSE

        // When
        long count = questionRepository.countByQuizIdAndQuestionTypeAndActiveTrue(
                testQuiz.getId(), QuestionType.SINGLE_CHOICE);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find question by ID and quiz ID")
    void shouldFindQuestionByIdAndQuizId() {
        // Given
        Question saved = questionRepository.save(question1);

        // When
        Optional<Question> found = questionRepository.findByIdAndQuizId(saved.getId(), testQuiz.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getQuestionText()).isEqualTo("What is Java?");
    }

    @Test
    @DisplayName("Should check if question exists in quiz")
    void shouldCheckIfQuestionExistsInQuiz() {
        // Given
        Question saved = questionRepository.save(question1);

        // When
        boolean exists = questionRepository.existsByIdAndQuizId(saved.getId(), testQuiz.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find next question order")
    void shouldFindNextQuestionOrder() {
        // Given
        questionRepository.save(question1); // order 1
        questionRepository.save(question2); // order 2

        // When
        Integer nextOrder = questionRepository.findNextQuestionOrder(testQuiz.getId());

        // Then
        assertThat(nextOrder).isEqualTo(3);
    }

    @Test
    @DisplayName("Should search questions in quiz")
    void shouldSearchQuestionsInQuiz() {
        // Given
        questionRepository.save(question1); // "What is Java?"
        questionRepository.save(question2); // "Is Java compiled?"

        // When
        List<Question> found = questionRepository.searchInQuiz(testQuiz.getId(), "compiled");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getQuestionText()).contains("compiled");
    }
}