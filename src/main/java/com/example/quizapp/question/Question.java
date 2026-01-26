package com.example.quizapp.question;

import com.example.quizapp.quiz.Quiz;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//     * Type of question (SINGLE_CHOICE, MULTIPLE_CHOICE, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionType questionType;

//     * The question text
    @Column(nullable = false, length = 1000)
    private String questionText;

//     * Points awarded for correct answer
    @Column(nullable = false)
    private int points;

//     * Negative points for wrong answer (if enabled in quiz settings)
    @Column(name = "negative_points")
    private Integer negativePoints;

//     * Question order/position in quiz
    @Column(name = "question_order")
    private Integer questionOrder;

//     * Time limit for this specific question in seconds (optional)
    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    /**
     * Answer options stored as JSON
     * Format depends on question type:
     *
     * SINGLE_CHOICE / MULTIPLE_CHOICE / DROPDOWN:
     * ["Option 1", "Option 2", "Option 3", "Option 4"]
     *
     * TRUE_FALSE:
     * ["True", "False"]
     *
     * FILL_BLANKS:
     * ["blank1", "blank2", "blank3"]
     *
     * SORTING:
     * ["Item 1", "Item 2", "Item 3", "Item 4"]
     *
     * MATCHING:
     * [{"left": "Country 1", "right": "Capital 1"}, {"left": "Country 2", "right": "Capital 2"}]
     *
     * SHORT_ANSWER:
     * null or [] (no options needed)
     */
    @Column(name = "answer_options", columnDefinition = "TEXT")
    private String answerOptions;

    /**
     * Correct answer(s) stored as JSON
     * Format depends on question type:
     *
     * SINGLE_CHOICE / DROPDOWN / TRUE_FALSE:
     * "0" (index of correct option)
     *
     * MULTIPLE_CHOICE:
     * ["0", "2"] (indexes of correct options)
     *
     * SHORT_ANSWER:
     * "correct answer text"
     *
     * FILL_BLANKS:
     * ["answer1", "answer2", "answer3"]
     *
     * SORTING:
     * ["0", "1", "2", "3"] (correct order as indexes)
     *
     * MATCHING:
     * [{"left": "0", "right": "2"}, {"left": "1", "right": "0"}] (correct pairs)
     */
    @Column(name = "correct_answer", columnDefinition = "TEXT", nullable = false)
    private String correctAnswer;

//     * Explanation shown after answering (optional)
    @Column(length = 1000)
    private String explanation;

//     * Image URL for question (optional)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

//     * Whether this question is active
    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

//     * Relationship with Quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

//     * Helper method to check if answer allows multiple selections
    public boolean allowsMultipleAnswers() {
        return questionType != null && questionType.allowsMultipleAnswers();
    }

//     * Helper method to check if question requires text input
    public boolean requiresTextInput() {
        return questionType != null && questionType.requiresTextInput();
    }

//     * Helper method to check if question requires ordering
    public boolean requiresOrdering() {
        return questionType != null && questionType.requiresOrdering();
    }

//     * Helper method to check if question requires matching
    public boolean requiresMatching() {
        return questionType != null && questionType.requiresMatching();
    }
}