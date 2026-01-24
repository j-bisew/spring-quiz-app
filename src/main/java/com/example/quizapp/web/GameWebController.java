package com.example.quizapp.web;

import com.example.quizapp.game.*;
import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/game")
@RequiredArgsConstructor
@Slf4j
public class GameWebController {

    private final QuizService quizService;
    private final GameService gameService;

    // Prosty formularz dla Nicku
    @Data
    public static class PlayerForm {
        private String nickname;
    }

    // Klasa pomocnicza do zbierania odpowiedzi z formularza Thymeleaf
    @Data
    public static class GameForm {
        private String sessionId;
        private Long quizId;
        private Long playerId;
        // Lista odpowiedzi (indeksowana w HTML)
        private List<AnswerSubmissionWrapper> answers = new ArrayList<>();
    }

    @Data
    public static class AnswerSubmissionWrapper {
        private Long questionId;
        private String userAnswer; // Dla checkboxów to może być JSON string lub tablica
    }

    @GetMapping("/start/{quizId}")
    public String showStartPage(@PathVariable Long quizId, Model model) {
        QuizDto quiz = quizService.getQuizById(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("playerForm", new PlayerForm());
        return "game-start"; // Ten plik już masz
    }

    @PostMapping("/start/{quizId}")
    public String startGame(@PathVariable Long quizId,
                            @ModelAttribute("playerForm") PlayerForm playerForm,
                            HttpSession session) {

        // Wywołujemy logikę biznesową startu gry
        StartGameRequest request = StartGameRequest.builder()
                .quizId(quizId)
                .playerNickname(playerForm.getNickname())
                .build();

        StartGameResponse response = gameService.startGame(request);

        // Zapisujemy kluczowe dane w sesji HTTP (żeby nie przesyłać ich w URL)
        session.setAttribute("gameSession", response);

        return "redirect:/game/play/" + quizId;
    }

    @GetMapping("/play/{quizId}")
    public String playGame(@PathVariable Long quizId, HttpSession session, Model model) {
        StartGameResponse gameSession = (StartGameResponse) session.getAttribute("gameSession");

        if (gameSession == null || !gameSession.getQuizId().equals(quizId)) {
            return "redirect:/game/start/" + quizId;
        }

        // Przygotowujemy formularz odpowiedzi
        GameForm gameForm = new GameForm();
        gameForm.setSessionId(gameSession.getSessionId());
        gameForm.setQuizId(quizId);
        gameForm.setPlayerId(gameSession.getPlayerId());

        // Inicjalizujemy listę odpowiedzi pustymi obiektami, żeby Thymeleaf mógł je zbindować
        for (var q : gameSession.getQuestions()) {
            var answer = new AnswerSubmissionWrapper();
            answer.setQuestionId(q.getId());
            gameForm.getAnswers().add(answer);
        }

        model.addAttribute("questions", gameSession.getQuestions());
        model.addAttribute("gameForm", gameForm);
        model.addAttribute("quizTitle", gameSession.getQuizTitle());

        // Jeśli quiz ma limit czasu, przekaż go do widoku
        model.addAttribute("timeLimit", gameSession.getTimeLimitMinutes());

        return "game-play"; // Ten plik musimy stworzyć
    }

    @PostMapping("/submit")
    public String submitAnswers(@ModelAttribute GameForm gameForm, HttpSession session) {
        // Mapowanie formularza na obiekt biznesowy
        List<SubmitAnswersRequest.AnswerSubmission> submissions = gameForm.getAnswers().stream()
                .filter(a -> a.getUserAnswer() != null) // Filtruj puste
                .map(a -> new SubmitAnswersRequest.AnswerSubmission(a.getQuestionId(), a.getUserAnswer()))
                .toList();

        SubmitAnswersRequest request = SubmitAnswersRequest.builder()
                .sessionId(gameForm.getSessionId())
                .quizId(gameForm.getQuizId())
                .playerId(gameForm.getPlayerId())
                .answers(submissions)
                .build();

        GameResultDto result = gameService.submitAnswers(request);

        // Czyścimy sesję gry
        session.removeAttribute("gameSession");

        return "redirect:/game/result/" + result.getId();
    }

    @GetMapping("/result/{resultId}")
    public String showResult(@PathVariable Long resultId, Model model) {
        GameResultDto result = gameService.getGameResult(resultId);
        model.addAttribute("result", result);
        return "game-result"; // Ten plik musimy stworzyć
    }
}