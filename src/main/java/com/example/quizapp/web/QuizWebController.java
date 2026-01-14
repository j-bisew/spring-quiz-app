package com.example.quizapp.web;

import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/quizzes")
@RequiredArgsConstructor
@Slf4j
public class QuizWebController {

    private final QuizService quizService;

    @GetMapping
    public String listQuizzes(@RequestParam(required = false) String search, Model model) {
        log.info("GET /quizzes - search: {}", search);

        List<QuizDto> quizzes;
        if (search != null && !search.isEmpty()) {
            quizzes = quizService.searchQuizzes(search);
            model.addAttribute("search", search);
        } else {
            quizzes = quizService.getAllActiveQuizzes();
        }

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("title", "Quizzes");
        return "quizzes";
    }

    @GetMapping("/{id}")
    public String viewQuiz(@PathVariable Long id, Model model) {
        log.info("GET /quizzes/{}", id);

        QuizDto quiz = quizService.getQuizById(id);
        model.addAttribute("quiz", quiz);
        model.addAttribute("title", quiz.getTitle());
        return "quiz-detail";
    }
}