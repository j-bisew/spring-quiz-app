package com.example.quizapp.quiz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class QuizJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

//    RowMapper for Quiz entity
    private static class QuizRowMapper implements RowMapper<Quiz> {
        @Override
        public Quiz mapRow(ResultSet rs, int rowNum) throws SQLException {
            Quiz quiz = new Quiz();
            quiz.setId(rs.getLong("id"));
            quiz.setTitle(rs.getString("title"));
            quiz.setDescription(rs.getString("description"));
            quiz.setRandomQuestionOrder(rs.getBoolean("random_question_order"));
            quiz.setRandomAnswerOrder(rs.getBoolean("random_answer_order"));
            quiz.setTimeLimitMinutes(rs.getInt("time_limit_minutes"));
            quiz.setNegativePointsEnabled(rs.getBoolean("negative_points_enabled"));
            quiz.setBackButtonBlocked(rs.getBoolean("back_button_blocked"));
            quiz.setActive(rs.getBoolean("active"));
            quiz.setCreatedBy(rs.getString("created_by"));

            // Handle nullable timestamps
            LocalDateTime createdAt = rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime()
                    : null;
            LocalDateTime updatedAt = rs.getTimestamp("updated_at") != null
                    ? rs.getTimestamp("updated_at").toLocalDateTime()
                    : null;
            quiz.setCreatedAt(createdAt);
            quiz.setUpdatedAt(updatedAt);

            return quiz;
        }
    }

//    Find quizzes by title using SQL LIKE
    public List<Quiz> findByTitleContaining(String keyword) {
        log.debug("Finding quizzes by title containing: {}", keyword);

        String sql = "SELECT * FROM quizzes WHERE LOWER(title) LIKE LOWER(?) AND is_active = true ORDER BY created_at DESC";

        String searchPattern = "%" + keyword + "%";

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Quiz.class), searchPattern);
    }

//    Get quiz statistics using complex SQL
    public Map<String, Object> getQuizStatistics(Long quizId) {
        log.debug("Getting statistics for quiz: {}", quizId);

        String sql = """
                SELECT 
                    q.id,
                    q.title,
                    COUNT(DISTINCT gr.id) as total_attempts,
                    COUNT(DISTINCT gr.player_id) as unique_players,
                    ROUND(AVG(gr.score), 2) as average_score,
                    MAX(gr.score) as highest_score,
                    MIN(gr.score) as lowest_score,
                    ROUND(AVG(gr.time_taken_seconds), 2) as average_time,
                    COUNT(CASE WHEN gr.percentage_score >= 50 THEN 1 END) as passed_count,
                    ROUND(100.0 * COUNT(CASE WHEN gr.percentage_score >= 50 THEN 1 END) / 
                          NULLIF(COUNT(*), 0), 2) as pass_rate
                FROM quizzes q
                LEFT JOIN game_results gr ON q.id = gr.quiz_id AND gr.is_completed = true
                WHERE q.id = ?
                GROUP BY q.id, q.title
                """;

        return jdbcTemplate.queryForMap(sql, quizId);
    }

//    Get top performers for a quiz
    public List<Map<String, Object>> getTopPerformers(Long quizId, int limit) {
        log.debug("Getting top {} performers for quiz: {}", limit, quizId);

        String sql = """
                SELECT 
                    p.nickname,
                    gr.score,
                    gr.max_score,
                    gr.percentage_score,
                    gr.time_taken_seconds,
                    gr.completed_at,
                    RANK() OVER (ORDER BY gr.score DESC, gr.time_taken_seconds ASC) as rank
                FROM game_results gr
                JOIN players p ON gr.player_id = p.id
                WHERE gr.quiz_id = ? AND gr.is_completed = true
                ORDER BY gr.score DESC, gr.time_taken_seconds ASC
                LIMIT ?
                """;

        return jdbcTemplate.queryForList(sql, quizId, limit);
    }

//    Get quiz difficulty analysis
    public List<Map<String, Object>> getQuestionDifficultyAnalysis(Long quizId) {
        log.debug("Analyzing question difficulty for quiz: {}", quizId);

        String sql = """
                SELECT 
                    q.id as question_id,
                    q.question_text,
                    q.question_type,
                    q.points,
                    COUNT(CASE WHEN jsonb_array_element(gr.answers_json::jsonb, q.question_order - 1)->>'isCorrect' = 'true' 
                               THEN 1 END) as correct_answers,
                    COUNT(*) as total_answers,
                    ROUND(100.0 * COUNT(CASE WHEN jsonb_array_element(gr.answers_json::jsonb, q.question_order - 1)->>'isCorrect' = 'true' 
                                             THEN 1 END) / COUNT(*), 2) as success_rate
                FROM questions q
                LEFT JOIN game_results gr ON q.quiz_id = gr.quiz_id AND gr.completed = true
                WHERE q.quiz_id = ? AND q.active = true
                GROUP BY q.id, q.question_text, q.question_type, q.points, q.question_order
                ORDER BY success_rate ASC
                """;

        return jdbcTemplate.queryForList(sql, quizId);
    }

//    Bulk update quiz active status
    public int bulkUpdateActiveStatus(List<Long> quizIds, boolean active) {
        String placeholders = quizIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "UPDATE quizzes SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id IN (" + placeholders + ")";

        List<Object> params = new ArrayList<>();
        params.add(active);
        params.addAll(quizIds);

        return jdbcTemplate.update(sql, params.toArray());
    }


    //    Delete old incomplete game results (cleanup)
    public int deleteOldIncompleteResults(int daysOld) {
        log.info("Deleting incomplete game results older than {} days", daysOld);

        String sql = """
                DELETE FROM game_results 
                WHERE is_completed = false 
                AND started_at < DATEADD('DAY', -?, CURRENT_TIMESTAMP)
                """;

        return jdbcTemplate.update(sql, daysOld);
    }

//    Get quiz activity by date range
    public List<Map<String, Object>> getQuizActivityByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting quiz activity from {} to {}", startDate, endDate);

        String sql = """
                SELECT
                    CAST(gr.completed_at AS DATE) AS com_date,
                    COUNT(DISTINCT gr.quiz_id) AS quizzes_played,
                    COUNT(*) AS total_games,
                    COUNT(DISTINCT gr.player_id) AS unique_players,
                    ROUND(AVG(gr.score), 2) AS avg_score
                FROM game_results gr
                WHERE gr.is_completed = true
                  AND gr.completed_at BETWEEN ? AND ?
                GROUP BY CAST(gr.completed_at AS DATE)
                ORDER BY com_date DESC;
                """;

        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

//    Find quizzes with no questions
    public List<Long> findQuizzesWithoutQuestions() {
        log.debug("Finding quizzes without questions");

        String sql = """
                SELECT q.id 
                FROM quizzes q
                LEFT JOIN questions qt ON q.id = qt.quiz_id
                WHERE qt.id IS NULL
                """;

        return jdbcTemplate.queryForList(sql, Long.class);
    }

//    Count total questions across all quizzes
    public int countTotalQuestions() {
        String sql = "SELECT COUNT(*) FROM questions WHERE is_active = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

//    Get average quiz completion time
    public Double getAverageCompletionTime(Long quizId) {
        String sql = """
                SELECT AVG(time_taken_seconds) 
                FROM game_results 
                WHERE quiz_id = ? AND is_completed = true
                """;

        return jdbcTemplate.queryForObject(sql, Double.class, quizId);
    }

//    Execute custom SQL query (for admin purposes)
    public List<Map<String, Object>> executeCustomQuery(String sql) {
        log.warn("Executing custom SQL query: {}", sql);
        return jdbcTemplate.queryForList(sql);
    }
}