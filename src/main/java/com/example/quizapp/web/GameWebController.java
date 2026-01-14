package com.example.quizapp.web;

import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@Slf4j
public class GameWebController {

    private final QuizService quizService;

//    Form for player nickname
    @Data
    public static class PlayerForm {
        @NotBlank(message = "Nickname is required")
        @Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters")
        private String nickname;
    }

    @GetMapping("/start/{quizId}")
    public String showStartPage(@PathVariable Long quizId, Model model) {
        log.info("GET /game/start/{}", quizId);

        QuizDto quiz = quizService.getQuizById(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("playerForm", new PlayerForm());
        model.addAttribute("title", "Start Quiz");
        return "game-start";
    }

    @PostMapping("/start/{quizId}")
    public String startGame(@PathVariable Long quizId,
                            @Validated @ModelAttribute("playerForm") PlayerForm playerForm,
                            Model model) {
        log.info("POST /game/start/{} - nickname: {}", quizId, playerForm.getNickname());
        // TODO: Start game session
        return "redirect:/game/play/" + quizId;
    }

    @GetMapping("/play/{quizId}")
    public String playGame(@PathVariable Long quizId, Model model) {
        log.info("GET /game/play/{}", quizId);
        // TODO: Show quiz questions
        model.addAttribute("title", "Play Quiz");
        return "game-play";
    }

    @PostMapping("/submit/{quizId}")
    public String submitAnswers(@PathVariable Long quizId, Model model) {
        log.info("POST /game/submit/{}", quizId);
        // TODO: Calculate results
        return "redirect:/game/result/1";
    }

    @GetMapping("/result/{resultId}")
    public String showResult(@PathVariable Long resultId, Model model) {
        log.info("GET /game/result/{}", resultId);
        // TODO: Show game results
        model.addAttribute("title", "Quiz Results");
        return "game-result";
    }
}