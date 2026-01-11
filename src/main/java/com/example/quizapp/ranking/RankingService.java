package com.example.quizapp.ranking;

import com.example.quizapp.common.exception.QuizNotFoundException;
import com.example.quizapp.common.exception.ResourceNotFoundException;
import com.example.quizapp.game.GameResult;
import com.example.quizapp.game.GameResultRepository;
import com.example.quizapp.quiz.Quiz;
import com.example.quizapp.quiz.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.SequencedCollection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RankingService {
    private final GameResultRepository gameResultRepository;
    private final QuizRepository quizRepository;

/*
    Get top rankings for a quiz (leaderboard)
     @param quizId Quiz ID
     @param limit Maximum number of results (default 10)
     @return List of ranking entries
*/
    public List<RankingDto> getTopRankings(Long quizId, Integer limit) {
        log.info("Getting top {} rankings for quiz: {}", limit, quizId);

        Quiz quiz = quizExists(quizId);

        int resultLimit = limit != null && limit > 0 ? limit : 10;
        Pageable pageable = PageRequest.of(0, resultLimit);

        List<GameResult> topResults = gameResultRepository.findTopScoresByQuizId(quizId, pageable);

        log.debug("Found {} top results for quiz {}", topResults.size(), quizId);

        return topResults.stream()
                .map(result -> mapToRankingDto(result, quiz))
                .collect(Collectors.toList());
    }

//    Get full leaderboard for a quiz (all results)
    public List<RankingDto> getFullLeaderboard(Long quizId) {
        log.info("Getting full leaderboard for quiz: {}", quizId);

        Quiz quiz = quizExists(quizId);

        List<GameResult> results = getRankings(quizId);

        log.debug("Found {} total results for quiz {}", results.size(), quizId);

        return results.stream()
                .map(result -> mapToRankingDto(result, quiz))
                .collect(Collectors.toList());
    }

//    Get player's ranking position for a specific quiz
    public RankingPositionDto getPlayerRanking(Long quizId, Long playerId) {
        log.info("Getting ranking position for player {} in quiz {}", playerId, quizId);

        Quiz quiz = quizExists(quizId);

        // Get all results for the quiz
        List<GameResult> allResults = getRankings(quizId);

        // Find player's results
        List<GameResult> playerResults = allResults.stream()
                .filter(r -> r.getPlayer().getId().equals(playerId))
                .collect(Collectors.toList());

        if (playerResults.isEmpty()) {
            throw new ResourceNotFoundException("No results found for player " + playerId + " in quiz " + quizId);
        }

        // Get best result
        GameResult bestResult = playerResults.get(0);

        // Find position in overall ranking
        int position = 0;
        for (int i = 0; i < allResults.size(); i++) {
            if (allResults.get(i).getId().equals(bestResult.getId())) {
                position = i + 1;
                break;
            }
        }

        return RankingPositionDto.builder()
                .playerId(playerId)
                .playerNickname(bestResult.getPlayer().getNickname())
                .quizId(quizId)
                .quizTitle(quiz.getTitle())
                .position(position)
                .totalPlayers(allResults.size())
                .score(bestResult.getScore())
                .maxScore(bestResult.getMaxScore())
                .percentageScore(bestResult.getPercentageScore())
                .timeTakenSeconds(bestResult.getTimeTakenSeconds())
                .completedAt(bestResult.getCompletedAt())
                .build();
    }

//    Get global rankings (top players across all quizzes)
    public List<GlobalRankingDto> getGlobalRankings(Integer limit) {
        log.info("Getting global rankings, limit: {}", limit);

        int resultLimit = limit != null && limit > 0 ? limit : 10;
        Pageable pageable = PageRequest.of(0, resultLimit);

        // Get recent results and group by player
        List<GameResult> recentResults = gameResultRepository.findRecentResults(pageable);

        // This is a simplified version - in production, you'd want more sophisticated logic
        return recentResults.stream()
                .map(this::mapToGlobalRankingDto)
                .collect(Collectors.toList());
    }

//    Map GameResult to RankingDto
    private RankingDto mapToRankingDto(GameResult result, Quiz quiz) {
        return RankingDto.builder()
                .playerNickname(result.getPlayer().getNickname())
                .score(result.getScore())
                .maxScore(result.getMaxScore())
                .percentageScore(result.getPercentageScore())
                .correctAnswers(result.getCorrectAnswers())
                .wrongAnswers(result.getWrongAnswers())
                .totalQuestions(result.getTotalQuestions())
                .timeTakenSeconds(result.getTimeTakenSeconds())
                .grade(result.getGrade())
                .completedAt(result.getCompletedAt())
                .quizTitle(quiz.getTitle())
                .build();
    }

//    Map GameResult to GlobalRankingDto
    private GlobalRankingDto mapToGlobalRankingDto(GameResult result) {
        return GlobalRankingDto.builder()
                .playerNickname(result.getPlayer().getNickname())
                .totalGamesPlayed(result.getPlayer().getGamesPlayed())
                .lastScore(result.getScore())
                .lastQuizTitle(result.getQuiz().getTitle())
                .lastPlayedAt(result.getCompletedAt())
                .build();
    }

//    Helpers
    private List<GameResult> getRankings(long quizId) {
        List<GameResult> results = gameResultRepository.findByQuizIdAndCompletedTrue(quizId);

        // Sort by score DESC, then by time ASC
        results.sort((r1, r2) -> {
            int scoreCompare = Integer.compare(r2.getScore(), r1.getScore());
            if (scoreCompare != 0) return scoreCompare;

            Integer time1 = r1.getTimeTakenSeconds() != null ? r1.getTimeTakenSeconds() : Integer.MAX_VALUE;
            Integer time2 = r2.getTimeTakenSeconds() != null ? r2.getTimeTakenSeconds() : Integer.MAX_VALUE;
            return Integer.compare(time1, time2);
        });

        return results;
    }

    private Quiz quizExists(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizNotFoundException(quizId));
    }
}