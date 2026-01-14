package com.example.quizapp.web;

import com.example.quizapp.game.GameResultRepository;
import com.example.quizapp.player.PlayerRepository;
import com.example.quizapp.question.QuestionRepository;
import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizMapper;
import com.example.quizapp.quiz.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final PlayerRepository playerRepository;
    private final GameResultRepository gameResultRepository;
    private final QuizMapper quizMapper;

    @GetMapping("/")
    public String home(Model model) {
        log.info("GET / - Loading home page");

        // Get platform statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuizzes", quizRepository.countByActiveTrue());
        stats.put("totalPlayers", playerRepository.countByActiveTrue());
        stats.put("totalGamesPlayed", gameResultRepository.countByCompletedTrue());
        stats.put("totalQuestions", questionRepository.count());
        model.addAttribute("stats", stats);

        // Get popular quizzes (first 3)
        List<QuizDto> popularQuizzes = quizRepository.findByActiveTrue(PageRequest.of(0, 3))
                .stream()
                .map(quizMapper::toDto)
                .collect(Collectors.toList());
        model.addAttribute("popularQuizzes", popularQuizzes);

        model.addAttribute("title", "Home");
        return "index";
    }
}