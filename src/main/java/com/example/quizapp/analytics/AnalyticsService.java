package com.example.quizapp.analytics;

import com.example.quizapp.game.GameResultJdbcRepository;
import com.example.quizapp.quiz.QuizJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final QuizJdbcRepository quizJdbcRepository;
    private final GameResultJdbcRepository gameResultJdbcRepository;

//    Get comprehensive quiz analytics
    public Map<String, Object> getQuizAnalytics(Long quizId) {
        log.info("Getting analytics for quiz: {}", quizId);

        Map<String, Object> analytics = new HashMap<>();

        // Basic statistics
        Map<String, Object> stats = quizJdbcRepository.getQuizStatistics(quizId);
        analytics.put("statistics", stats);

        // Top performers
        List<Map<String, Object>> topPerformers = quizJdbcRepository.getTopPerformers(quizId, 10);
        analytics.put("topPerformers", topPerformers);

        // Question difficulty
        List<Map<String, Object>> difficulty = quizJdbcRepository.getQuestionDifficultyAnalysis(quizId);
        analytics.put("questionDifficulty", difficulty);

        // Players needing retry
        List<Map<String, Object>> needRetry = gameResultJdbcRepository.getPlayersNeedingRetry(quizId);
        analytics.put("playersNeedingRetry", needRetry);

        // Average completion time
        Double avgTime = quizJdbcRepository.getAverageCompletionTime(quizId);
        analytics.put("averageCompletionTime", avgTime);

        return analytics;
    }

//    Get player performance report
    public Map<String, Object> getPlayerReport(Long playerId) {
        log.info("Getting performance report for player: {}", playerId);

        Map<String, Object> report = new HashMap<>();

        // Performance history
        List<Map<String, Object>> history = gameResultJdbcRepository.getPlayerHistory(playerId);
        report.put("history", history);

        // Calculate statistics
        if (!history.isEmpty()) {
            double avgPercentage = history.stream()
                    .mapToDouble(h -> ((Number) h.get("percentage_score")).doubleValue())
                    .average()
                    .orElse(0.0);

            long passedCount = history.stream()
                    .filter(h -> ((Number) h.get("percentage_score")).doubleValue() >= 50)
                    .count();

            report.put("totalQuizzes", history.size());
            report.put("averagePercentage", Math.round(avgPercentage * 100.0) / 100.0);
            report.put("passedQuizzes", passedCount);
            report.put("passRate", Math.round((passedCount * 100.0 / history.size()) * 100.0) / 100.0);
        }

        return report;
    }

//    Get platform-wide analytics
    public Map<String, Object> getPlatformAnalytics() {
        log.info("Getting platform-wide analytics");

        Map<String, Object> analytics = new HashMap<>();

        // Active players
        List<Map<String, Object>> activePlayers = gameResultJdbcRepository.getActivePlayersStatistics(3);
        analytics.put("activePlayers", activePlayers);

        // Completion rate by difficulty
        List<Map<String, Object>> difficultyAnalysis = gameResultJdbcRepository.getCompletionRateByDifficulty();
        analytics.put("difficultyAnalysis", difficultyAnalysis);

        // Hourly activity
        List<Map<String, Object>> hourlyActivity = gameResultJdbcRepository.getHourlyActivity();
        analytics.put("hourlyActivity", hourlyActivity);

        // Completion trend
        List<Map<String, Object>> trend = gameResultJdbcRepository.getCompletionTrend();
        analytics.put("completionTrend", trend);

        // Quiz activity last 30 days
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        List<Map<String, Object>> activity = quizJdbcRepository.getQuizActivityByDateRange(startDate, endDate);
        analytics.put("last30DaysActivity", activity);

        // Total questions count
        int totalQuestions = quizJdbcRepository.countTotalQuestions();
        analytics.put("totalQuestions", totalQuestions);

        // Quizzes without questions
        List<Long> emptyQuizzes = quizJdbcRepository.findQuizzesWithoutQuestions();
        analytics.put("quizzesWithoutQuestions", emptyQuizzes);

        return analytics;
    }

//    Compare result with quiz average
    public Map<String, Object> compareResultWithAverage(Long resultId) {
        log.info("Comparing result {} with average", resultId);
        return gameResultJdbcRepository.compareWithAverage(resultId);
    }

//    Cleanup old incomplete results
    @Transactional
    public int cleanupOldIncompleteResults(int daysOld) {
        log.info("Cleaning up incomplete results older than {} days", daysOld);
        return quizJdbcRepository.deleteOldIncompleteResults(daysOld);
    }

//    Bulk activate/deactivate quizzes
    @Transactional
    public int bulkUpdateQuizStatus(List<Long> quizIds, boolean active) {
        log.info("Bulk updating {} quizzes to active={}", quizIds.size(), active);
        return quizJdbcRepository.bulkUpdateActiveStatus(quizIds, active);
    }

//    Search quizzes by keyword (using SQL LIKE)
    public List<com.example.quizapp.quiz.Quiz> searchQuizzes(String keyword) {
        log.info("Searching quizzes with keyword: {}", keyword);
        return quizJdbcRepository.findByTitleContaining(keyword);
    }
}