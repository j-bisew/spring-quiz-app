package com.example.quizapp.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GameResultJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

//    Simple RowMapper for GameResult ID and score
    private static class GameResultScoreMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Map.of(
                    "id", rs.getLong("id"),
                    "score", rs.getInt("score"),
                    "maxScore", rs.getInt("max_score"),
                    "playerNickname", rs.getString("nickname"),
                    "completedAt", rs.getTimestamp("completed_at").toLocalDateTime()
            );
        }
    }

//    Get player performance history
    public List<Map<String, Object>> getPlayerHistory(Long playerId) {
        log.debug("Getting performance history for player: {}", playerId);

        String sql = """
                SELECT 
                    gr.id,
                    q.title as quiz_title,
                    gr.score,
                    gr.max_score,
                    gr.percentage_score,
                    gr.completed_at,
                    gr.time_taken_seconds,
                    CASE 
                        WHEN gr.percentage_score >= 90 THEN 'A'
                        WHEN gr.percentage_score >= 80 THEN 'B'
                        WHEN gr.percentage_score >= 70 THEN 'C'
                        WHEN gr.percentage_score >= 60 THEN 'D'
                        WHEN gr.percentage_score >= 50 THEN 'E'
                        ELSE 'F'
                    END as grade
                FROM game_results gr
                JOIN quizzes q ON gr.quiz_id = q.id
                WHERE gr.player_id = ? AND gr.is_completed = true
                ORDER BY gr.completed_at DESC
                """;

        return jdbcTemplate.queryForList(sql, playerId);
    }

//    Get players who need to retry (failed quizzes)
    public List<Map<String, Object>> getPlayersNeedingRetry(Long quizId) {
        log.debug("Getting players who failed quiz: {}", quizId);

        String sql = """
                SELECT 
                    p.id,
                    p.nickname,
                    gr.score,
                    gr.percentage_score,
                    gr.completed_at
                FROM game_results gr
                JOIN players p ON gr.player_id = p.id
                WHERE gr.quiz_id = ? 
                AND gr.is_completed = true
                AND gr.percentage_score < 50
                ORDER BY gr.completed_at DESC
                """;

        return jdbcTemplate.queryForList(sql, quizId);
    }

//    Compare player performance with average
    public Map<String, Object> compareWithAverage(Long resultId) {
        log.debug("Comparing result {} with quiz average", resultId);

        String sql = """
                SELECT 
                    gr.score as player_score,
                    gr.percentage_score as player_percentage,
                    ROUND(AVG(gr2.score) OVER (), 2) as quiz_avg_score,
                    ROUND(AVG(gr2.percentage_score) OVER (), 2) as quiz_avg_percentage,
                    gr.score - ROUND(AVG(gr2.score) OVER (), 2) as score_diff,
                    CASE 
                        WHEN gr.score >= AVG(gr2.score) OVER () THEN 'Above Average'
                        ELSE 'Below Average'
                    END as performance
                FROM game_results gr
                JOIN game_results gr2 ON gr.quiz_id = gr2.quiz_id AND gr2.is_completed = true
                WHERE gr.id = ? AND gr.is_completed = true
                LIMIT 1
                """;

        return jdbcTemplate.queryForMap(sql, resultId);
    }

//    Get completion rate by quiz difficulty
    public List<Map<String, Object>> getCompletionRateByDifficulty() {
        log.debug("Analyzing completion rate by quiz difficulty");

        String sql = """
            SELECT
                CASE
                    WHEN q.time_limit_minutes <= 10 THEN 'Easy'
                    WHEN q.time_limit_minutes <= 30 THEN 'Medium'
                    ELSE 'Hard'
                END as difficulty,
                COUNT(*) as total_attempts,
                COUNT(CASE WHEN gr.percentage_score >= 50 THEN 1 END) as passed,
                ROUND(100.0 * COUNT(CASE WHEN gr.percentage_score >= 50 THEN 1 END) / COUNT(*), 2) as pass_rate
            FROM game_results gr
            JOIN quizzes q ON gr.quiz_id = q.id
            WHERE gr.is_completed = true
            GROUP BY
                CASE
                    WHEN q.time_limit_minutes <= 10 THEN 'Easy'
                    WHEN q.time_limit_minutes <= 30 THEN 'Medium'
                    ELSE 'Hard'
                END
            ORDER BY
                CASE
                    WHEN q.time_limit_minutes <= 10 THEN 'Easy'
                    WHEN q.time_limit_minutes <= 30 THEN 'Medium'
                    ELSE 'Hard'
                END
            """;

        return jdbcTemplate.queryForList(sql);
    }

//    Find players who have attempted multiple quizzes
    public List<Map<String, Object>> getActivePlayersStatistics(int minAttempts) {
        log.debug("Getting active players with at least {} attempts", minAttempts);

        String sql = """
                SELECT 
                    p.id,
                    p.nickname,
                    COUNT(DISTINCT gr.quiz_id) as quizzes_attempted,
                    COUNT(gr.id) as total_games,
                    ROUND(AVG(gr.percentage_score), 2) as avg_percentage,
                    MAX(gr.score) as best_score,
                    p.last_played_at
                FROM players p
                JOIN game_results gr ON p.id = gr.player_id AND gr.is_completed = true
                GROUP BY p.id, p.nickname, p.last_played_at
                HAVING COUNT(gr.id) >= ?
                ORDER BY total_games DESC, avg_percentage DESC
                """;

        return jdbcTemplate.queryForList(sql, minAttempts);
    }

//    Get hourly game activity
    public List<Map<String, Object>> getHourlyActivity() {
        log.debug("Getting hourly game activity statistics");

        String sql = """
                SELECT 
                    EXTRACT(HOUR FROM completed_at) as completed_hour,
                    COUNT(*) as games_count,
                    ROUND(AVG(score), 2) as avg_score
                FROM game_results
                WHERE is_completed = true
                AND completed_at >= DATEADD('DAY', -7, CURRENT_DATE) 
                GROUP BY EXTRACT(HOUR FROM completed_at)
                ORDER BY completed_hour
                """;

        return jdbcTemplate.queryForList(sql);
    }

//    Batch insert mock data (for testing)
    public int batchInsertMockResults(List<Map<String, Object>> results) {
        log.info("Batch inserting {} mock results", results.size());

        String sql = """
                INSERT INTO game_results 
                (player_id, quiz_id, score, max_score, percentage_score, completed, started_at, completed_at)
                VALUES (?, ?, ?, ?, ?, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;

        int[][] updateCounts = jdbcTemplate.batchUpdate(sql, results, results.size(),
                (ps, result) -> {
                    ps.setLong(1, (Long) result.get("playerId"));
                    ps.setLong(2, (Long) result.get("quizId"));
                    ps.setInt(3, (Integer) result.get("score"));
                    ps.setInt(4, (Integer) result.get("maxScore"));
                    ps.setDouble(5, (Double) result.get("percentageScore"));
                });

        // Sum all batch results
        int totalInserted = 0;
        for (int[] batch : updateCounts) {
            for (int count : batch) {
                totalInserted += count;
            }
        }
        return totalInserted;
    }

//    Get quiz completion trend (last 30 days)
    public List<Map<String, Object>> getCompletionTrend() {
        String sql = """
                SELECT 
                    CAST(completed_at AS DATE) as date,
                    COUNT(*) as completions,
                    COUNT(CASE WHEN percentage_score >= 50 THEN 1 END) as passed
                FROM game_results
                WHERE is_completed = true
                AND completed_at >= DATEADD('DAY', -30, CURRENT_DATE)
                GROUP BY CAST(completed_at AS DATE)
                ORDER BY date
                """;

        return jdbcTemplate.queryForList(sql);
    }
}