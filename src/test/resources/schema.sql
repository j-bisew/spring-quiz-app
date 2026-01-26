-- Database schema initialization
-- Quiz Application - PostgreSQL Schema

-- Drop tables if exist (for development)
DROP TABLE IF EXISTS game_results CASCADE;
DROP TABLE IF EXISTS questions CASCADE;
DROP TABLE IF EXISTS quizzes CASCADE;
DROP TABLE IF EXISTS players CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       full_name VARCHAR(100),
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       is_enabled BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quizzes table
CREATE TABLE quizzes (
                         id BIGSERIAL PRIMARY KEY,
                         title VARCHAR(200) NOT NULL,
                         description VARCHAR(1000),
                         random_question_order BOOLEAN DEFAULT FALSE,
                         random_answer_order BOOLEAN DEFAULT FALSE,
                         time_limit_minutes INTEGER,
                         negative_points_enabled BOOLEAN DEFAULT FALSE,
                         back_button_blocked BOOLEAN DEFAULT FALSE,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_by VARCHAR(50),
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Questions table
CREATE TABLE questions (
                           id BIGSERIAL PRIMARY KEY,
                           quiz_id BIGINT NOT NULL,
                           question_type VARCHAR(50) NOT NULL,
                           question_text VARCHAR(1000) NOT NULL,
                           points INTEGER NOT NULL,
                           negative_points INTEGER,
                           question_order INTEGER,
                           time_limit_seconds INTEGER,
                           answer_options TEXT,
                           correct_answer TEXT NOT NULL,
                           explanation VARCHAR(1000),
                           image_url VARCHAR(500),
                           is_active BOOLEAN DEFAULT TRUE,
                           FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Players table (anonymous players)
CREATE TABLE players (
                         id BIGSERIAL PRIMARY KEY,
                         nickname VARCHAR(50) NOT NULL,
                         email VARCHAR(100),
                         session_id VARCHAR(100),
                         ip_address VARCHAR(45),
                         first_played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         last_played_at TIMESTAMP,
                         games_played INTEGER DEFAULT 0,
                         is_active BOOLEAN DEFAULT TRUE
);

-- Game results table
CREATE TABLE game_results (
                              id BIGSERIAL PRIMARY KEY,
                              player_id BIGINT NOT NULL,
                              quiz_id BIGINT NOT NULL,
                              score INTEGER NOT NULL,
                              max_score INTEGER NOT NULL,
                              correct_answers INTEGER,
                              wrong_answers INTEGER,
                              total_questions INTEGER,
                              time_taken_seconds INTEGER,
                              percentage_score DOUBLE PRECISION,
                              answers_json TEXT,
                              session_id VARCHAR(100),
                              started_at TIMESTAMP,
                              completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              is_completed BOOLEAN DEFAULT TRUE,
                              ip_address VARCHAR(45),
                              FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
                              FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Indexes for better query performance
CREATE INDEX idx_quizzes_active ON quizzes(is_active);
CREATE INDEX idx_quizzes_created_by ON quizzes(created_by);
CREATE INDEX idx_questions_quiz_id ON questions(quiz_id);
CREATE INDEX idx_questions_active ON questions(is_active);
CREATE INDEX idx_questions_type ON questions(question_type);
CREATE INDEX idx_players_session_id ON players(session_id);
CREATE INDEX idx_players_nickname ON players(nickname);
CREATE INDEX idx_game_results_quiz_id ON game_results(quiz_id);
CREATE INDEX idx_game_results_player_id ON game_results(player_id);
CREATE INDEX idx_game_results_session_id ON game_results(session_id);
CREATE INDEX idx_game_results_completed ON game_results(is_completed);
CREATE INDEX idx_game_results_score ON game_results(score DESC);
CREATE INDEX idx_game_results_completed_at ON game_results(completed_at DESC);

-- Comments for documentation
COMMENT ON TABLE users IS 'Quiz creators and administrators';
COMMENT ON TABLE quizzes IS 'Quiz definitions with settings';
COMMENT ON TABLE questions IS 'Questions for quizzes with 8 different types';
COMMENT ON TABLE players IS 'Anonymous players (no registration required)';
COMMENT ON TABLE game_results IS 'Results of completed quiz attempts';

COMMENT ON COLUMN questions.question_type IS 'SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER, DROPDOWN, FILL_BLANKS, SORTING, MATCHING';
COMMENT ON COLUMN questions.answer_options IS 'JSON array of answer options (format depends on question type)';
COMMENT ON COLUMN questions.correct_answer IS 'JSON representation of correct answer (format depends on question type)';
COMMENT ON COLUMN game_results.answers_json IS 'JSON array of detailed answers with correctness and points';