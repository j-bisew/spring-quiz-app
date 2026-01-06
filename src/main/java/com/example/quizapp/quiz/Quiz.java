package com.example.quizapp.quiz;

import com.example.quizapp.question.Question;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    // settings
    @Column(name = "random_question_order")
    private boolean randomQuestionOrder;

    @Column(name = "random_answer_order")
    private boolean randomAnswerOrder;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "negative_points_enabled")
    private boolean negativePointsEnabled;

    @Column(name = "back_button_blocked")
    private boolean backButtonBlocked;

    // metadata
    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // questions relation
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    // helpers
    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null);
    }

    public int getTotalPoints() {
        return questions.stream()
                .mapToInt(Question::getPoints)
                .sum();
    }

    public int getQuestionCount() {
        return questions.size();
    }
}