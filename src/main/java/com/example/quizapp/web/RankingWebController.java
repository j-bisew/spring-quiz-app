package com.example.quizapp.web;

import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizService;
import com.example.quizapp.ranking.GlobalRankingDto;
import com.example.quizapp.ranking.RankingDto;
import com.example.quizapp.ranking.RankingService;
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
@RequestMapping("/rankings")
@RequiredArgsConstructor
@Slf4j
public class RankingWebController {

    private final RankingService rankingService;
    private final QuizService quizService;

//    Show global rankings page
    @GetMapping
    public String showGlobalRankings(@RequestParam(defaultValue = "20") Integer limit, Model model) {
        log.info("GET /rankings - Showing global rankings, limit: {}", limit);

        List<GlobalRankingDto> rankings = rankingService.getGlobalRankings(limit);
        List<QuizDto> quizzes = quizService.getAllActiveQuizzes();

        model.addAttribute("rankings", rankings);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("title", "Global Rankings");

        return "rankings/global";
    }

//    Show quiz-specific leaderboard
    @GetMapping("/quiz/{quizId}")
    public String showQuizLeaderboard(@PathVariable Long quizId,
                                      @RequestParam(defaultValue = "50") Integer limit,
                                      Model model) {
        log.info("GET /rankings/quiz/{} - Showing quiz leaderboard, limit: {}", quizId, limit);

        QuizDto quiz = quizService.getQuizById(quizId);
        List<RankingDto> rankings = rankingService.getTopRankings(quizId, limit);

        model.addAttribute("quiz", quiz);
        model.addAttribute("rankings", rankings);
        model.addAttribute("title", "Leaderboard - " + quiz.getTitle());

        return "rankings/quiz-leaderboard";
    }

//    Alias for quiz leaderboard
    @GetMapping("/{quizId}")
    public String showQuizLeaderboardAlias(@PathVariable Long quizId,
                                           @RequestParam(defaultValue = "50") Integer limit,
                                           Model model) {
        return showQuizLeaderboard(quizId, limit, model);
    }
}