package com.example.quizapp.web;

import com.example.quizapp.question.QuestionDto;
import com.example.quizapp.question.QuestionService;
import com.example.quizapp.question.QuestionType;
import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/creator")
@RequiredArgsConstructor
@Slf4j
public class CreatorWebController {

    private final QuizService quizService;
    private final QuestionService questionService;

//    Creator dashboard - list user's quizzes
    @GetMapping
    public String creatorDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        log.info("GET /creator - Loading creator dashboard for user: {}", userDetails.getUsername());

        List<QuizDto> myQuizzes = quizService.getQuizzesByCreator(userDetails.getUsername());
        model.addAttribute("quizzes", myQuizzes);

        // NOWE: Obliczanie statystyk w Javie zamiast w HTML
        long activeQuizzes = myQuizzes.stream()
                .filter(QuizDto::isActive) // lub q -> q.isActive()
                .count();

        int totalQuestions = myQuizzes.stream()
                .mapToInt(q -> q.getQuestionCount() != null ? q.getQuestionCount() : 0)
                .sum();

        model.addAttribute("statsActiveQuizzes", activeQuizzes);
        model.addAttribute("statsTotalQuestions", totalQuestions);

        model.addAttribute("title", "Creator Dashboard");

        return "creator/dashboard";
    }

//    Show new quiz form
    @GetMapping("/quiz/new")
    public String showNewQuizForm(Model model) {
        log.info("GET /creator/quiz/new - Showing new quiz form");

        model.addAttribute("quizDto", new QuizDto());
        model.addAttribute("title", "Create New Quiz");
        model.addAttribute("isEdit", false);

        return "creator/quiz-form";
    }

//    Process new quiz creation
    @PostMapping("/quiz/new")
    public String createQuiz(@Valid @ModelAttribute("quizDto") QuizDto quizDto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        log.info("POST /creator/quiz/new - Creating quiz: {}", quizDto.getTitle());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in quiz form: {}", bindingResult.getAllErrors());
            model.addAttribute("title", "Create New Quiz");
            model.addAttribute("isEdit", false);
            return "creator/quiz-form";
        }

        try {
            quizDto.setCreatedBy(userDetails.getUsername());
            quizDto.setActive(true);
            QuizDto createdQuiz = quizService.createQuiz(quizDto);

            redirectAttributes.addFlashAttribute("message", "Quiz '" + createdQuiz.getTitle() + "' created successfully!");
            return "redirect:/creator/quiz/" + createdQuiz.getId() + "/questions";
        } catch (IllegalArgumentException e) {
            log.error("Error creating quiz: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Create New Quiz");
            model.addAttribute("isEdit", false);
            return "creator/quiz-form";
        }
    }

//    Show edit quiz form
    @GetMapping("/quiz/{quizId}/edit")
    public String showEditQuizForm(@PathVariable Long quizId, Model model) {
        log.info("GET /creator/quiz/{}/edit - Showing edit form", quizId);

        QuizDto quiz = quizService.getQuizById(quizId);
        model.addAttribute("quizDto", quiz);
        model.addAttribute("title", "Edit Quiz");
        model.addAttribute("isEdit", true);

        return "creator/quiz-form";
    }

//    Process quiz update
    @PostMapping("/quiz/{quizId}/edit")
    public String updateQuiz(@PathVariable Long quizId,
                             @Valid @ModelAttribute("quizDto") QuizDto quizDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        log.info("POST /creator/quiz/{}/edit - Updating quiz", quizId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Edit Quiz");
            model.addAttribute("isEdit", true);
            return "creator/quiz-form";
        }

        try {
            quizService.updateQuiz(quizId, quizDto);
            redirectAttributes.addFlashAttribute("message", "Quiz updated successfully!");
            return "redirect:/creator/quiz/" + quizId + "/questions";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Edit Quiz");
            model.addAttribute("isEdit", true);
            return "creator/quiz-form";
        }
    }

//    Quiz question management page
    @GetMapping("/quiz/{quizId}/questions")
    public String manageQuestions(@PathVariable Long quizId, Model model) {
        log.info("GET /creator/quiz/{}/questions - Managing questions", quizId);

        QuizDto quiz = quizService.getQuizById(quizId);
        List<QuestionDto> questions = questionService.getQuestionsOrderedByPosition(quizId);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("questionTypes", QuestionType.values());
        model.addAttribute("title", "Manage Questions - " + quiz.getTitle());

        return "creator/questions-list";
    }

//    Show new question form
    @GetMapping("/quiz/{quizId}/question/new")
    public String showNewQuestionForm(@PathVariable Long quizId,
                                      @RequestParam(required = false) String type,
                                      Model model) {
        log.info("GET /creator/quiz/{}/question/new - Showing new question form, type: {}", quizId, type);

        QuizDto quiz = quizService.getQuizById(quizId);
        QuestionDto questionDto = new QuestionDto();
        questionDto.setQuizId(quizId);
        questionDto.setPoints(10);

        if (type != null) {
            try {
                questionDto.setQuestionType(QuestionType.valueOf(type));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid question type: {}", type);
            }
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("questionDto", questionDto);
        model.addAttribute("questionTypes", QuestionType.values());
        model.addAttribute("title", "Add Question");
        model.addAttribute("isEdit", false);

        return "creator/question-form";
    }

//    Process new question creation
    @PostMapping("/quiz/{quizId}/question/new")
    public String createQuestion(@PathVariable Long quizId,
                                 @Valid @ModelAttribute("questionDto") QuestionDto questionDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        log.info("POST /creator/quiz/{}/question/new - Creating question", quizId);

        questionDto.setQuizId(quizId);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            QuizDto quiz = quizService.getQuizById(quizId);
            model.addAttribute("quiz", quiz);
            model.addAttribute("questionTypes", QuestionType.values());
            model.addAttribute("title", "Add Question");
            model.addAttribute("isEdit", false);
            return "creator/question-form";
        }

        try {
            questionService.createQuestion(questionDto);
            redirectAttributes.addFlashAttribute("message", "Question added successfully!");
            return "redirect:/creator/quiz/" + quizId + "/questions";
        } catch (IllegalArgumentException e) {
            log.error("Error creating question: {}", e.getMessage());
            QuizDto quiz = quizService.getQuizById(quizId);
            model.addAttribute("quiz", quiz);
            model.addAttribute("questionTypes", QuestionType.values());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Add Question");
            model.addAttribute("isEdit", false);
            return "creator/question-form";
        }
    }

//    Show edit question form
    @GetMapping("/quiz/{quizId}/question/{questionId}/edit")
    public String showEditQuestionForm(@PathVariable Long quizId,
                                       @PathVariable Long questionId,
                                       Model model) {
        log.info("GET /creator/quiz/{}/question/{}/edit - Showing edit form", quizId, questionId);

        QuizDto quiz = quizService.getQuizById(quizId);
        QuestionDto question = questionService.getQuestionById(questionId);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questionDto", question);
        model.addAttribute("questionTypes", QuestionType.values());
        model.addAttribute("title", "Edit Question");
        model.addAttribute("isEdit", true);

        return "creator/question-form";
    }

//    Process question update
    @PostMapping("/quiz/{quizId}/question/{questionId}/edit")
    public String updateQuestion(@PathVariable Long quizId,
                                 @PathVariable Long questionId,
                                 @Valid @ModelAttribute("questionDto") QuestionDto questionDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        log.info("POST /creator/quiz/{}/question/{}/edit - Updating question", quizId, questionId);

        questionDto.setQuizId(quizId);

        if (bindingResult.hasErrors()) {
            QuizDto quiz = quizService.getQuizById(quizId);
            model.addAttribute("quiz", quiz);
            model.addAttribute("questionTypes", QuestionType.values());
            model.addAttribute("title", "Edit Question");
            model.addAttribute("isEdit", true);
            return "creator/question-form";
        }

        try {
            questionService.updateQuestion(questionId, questionDto);
            redirectAttributes.addFlashAttribute("message", "Question updated successfully!");
            return "redirect:/creator/quiz/" + quizId + "/questions";
        } catch (IllegalArgumentException e) {
            QuizDto quiz = quizService.getQuizById(quizId);
            model.addAttribute("quiz", quiz);
            model.addAttribute("questionTypes", QuestionType.values());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Edit Question");
            model.addAttribute("isEdit", true);
            return "creator/question-form";
        }
    }

//    Delete question
    @PostMapping("/quiz/{quizId}/question/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId,
                                 @PathVariable Long questionId,
                                 RedirectAttributes redirectAttributes) {
        log.info("POST /creator/quiz/{}/question/{}/delete - Deleting question", quizId, questionId);

        questionService.deleteQuestion(questionId);
        redirectAttributes.addFlashAttribute("message", "Question deleted successfully!");

        return "redirect:/creator/quiz/" + quizId + "/questions";
    }

//    Delete quiz
    @PostMapping("/quiz/{quizId}/delete")
    public String deleteQuiz(@PathVariable Long quizId, RedirectAttributes redirectAttributes) {
        log.info("POST /creator/quiz/{}/delete - Deleting quiz", quizId);

        quizService.deleteQuiz(quizId);
        redirectAttributes.addFlashAttribute("message", "Quiz deleted successfully!");

        return "redirect:/creator";
    }

//    Preview quiz
    @GetMapping("/quiz/{quizId}/preview")
    public String previewQuiz(@PathVariable Long quizId, Model model) {
        log.info("GET /creator/quiz/{}/preview - Previewing quiz", quizId);

        QuizDto quiz = quizService.getQuizById(quizId);
        List<QuestionDto> questions = questionService.getQuestionsOrderedByPosition(quizId);

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("title", "Preview - " + quiz.getTitle());

        return "creator/quiz-preview";
    }
}