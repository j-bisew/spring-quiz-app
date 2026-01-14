package com.example.quizapp.web;

import com.example.quizapp.game.GameResultRepository;
import com.example.quizapp.player.PlayerRepository;
import com.example.quizapp.question.QuestionRepository;
import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MVC Controller for home page
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final QuizService quizService;
    private final QuestionRepository questionRepository;
    private final PlayerRepository playerRepository;
    private final GameResultRepository gameResultRepository;

    @GetMapping("/")
    public String home(Model model) {
        log.info("GET / - Loading home page");

        // Get platform statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuizzes", quizService.countActiveQuizzes());
        stats.put("totalPlayers", playerRepository.countByActiveTrue());
        stats.put("totalGamesPlayed", gameResultRepository.countByCompletedTrue());
        stats.put("totalQuestions", questionRepository.count());
        model.addAttribute("stats", stats);

        // Get popular quizzes (first 3)
        List<QuizDto> popularQuizzes = quizService.getAllActiveQuizzes();
        if (popularQuizzes.size() > 3) {
            popularQuizzes = popularQuizzes.subList(0, 3);
        }
        model.addAttribute("popularQuizzes", popularQuizzes);

        model.addAttribute("title", "Home");
        return "index";
    }
}