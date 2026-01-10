-- Sample data initialization
-- Quiz Application - Test Data

-- Insert test users (password is 'password' encrypted with BCrypt)
INSERT INTO users (username, password, email, full_name, role, is_enabled) VALUES
                                                                               ('admin', '$2a$10$7PtcjEnWb/ZkgyXyxY0C3OqnHFHmaN/YwVKN4e.WkTbEEQU0xLFrm', 'admin@quiz.com', 'Admin User', 'ADMIN', true),
                                                                               ('creator1', '$2a$10$7PtcjEnWb/ZkgyXyxY0C3OqnHFHmaN/YwVKN4e.WkTbEEQU0xLFrm', 'creator1@quiz.com', 'Quiz Creator 1', 'USER', true),
                                                                               ('creator2', '$2a$10$7PtcjEnWb/ZkgyXyxY0C3OqnHFHmaN/YwVKN4e.WkTbEEQU0xLFrm', 'creator2@quiz.com', 'Quiz Creator 2', 'USER', true);

-- Insert sample quizzes
INSERT INTO quizzes (title, description, random_question_order, random_answer_order, time_limit_minutes, negative_points_enabled, back_button_blocked, is_active, created_by) VALUES
                                                                                                                                                                                  ('Java Programming Basics', 'Test your knowledge of Java fundamentals', true, false, 30, true, false, true, 'creator1'),
                                                                                                                                                                                  ('Spring Framework Quiz', 'Advanced Spring Framework concepts', false, true, 45, false, true, true, 'creator1'),
                                                                                                                                                                                  ('General Knowledge', 'Mixed questions about various topics', true, true, 20, false, false, true, 'creator2'),
                                                                                                                                                                                  ('Mathematics Challenge', 'Test your math skills', false, false, 60, true, false, true, 'creator2');

-- Insert questions for Quiz 1: Java Programming Basics

-- Question 1: SINGLE_CHOICE
INSERT INTO questions (quiz_id, question_type, question_text, points, negative_points, question_order, answer_options, correct_answer, explanation) VALUES
    (1, 'SINGLE_CHOICE', 'What is the default value of a boolean variable in Java?', 10, 2, 1,
     '["true", "false", "null", "0"]',
     '1',
     'In Java, the default value of a boolean variable is false.');

-- Question 2: MULTIPLE_CHOICE
INSERT INTO questions (quiz_id, question_type, question_text, points, negative_points, question_order, answer_options, correct_answer, explanation) VALUES
    (1, 'MULTIPLE_CHOICE', 'Which of the following are valid Java primitive types? (Select all that apply)', 15, 3, 2,
     '["int", "String", "boolean", "char", "Integer"]',
     '[0, 2, 3]',
     'int, boolean, and char are primitive types. String and Integer are reference types.');

-- Question 3: TRUE_FALSE
INSERT INTO questions (quiz_id, question_type, question_text, points, negative_points, question_order, answer_options, correct_answer, explanation) VALUES
    (1, 'TRUE_FALSE', 'Java is a platform-independent programming language.', 5, 1, 3,
     '["True", "False"]',
     '0',
     'Java is platform-independent because it runs on the JVM.');

-- Question 4: SHORT_ANSWER
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, correct_answer, explanation) VALUES
    (1, 'SHORT_ANSWER', 'What keyword is used to create a subclass in Java?', 10, 4,
     'extends',
     'The "extends" keyword is used for class inheritance in Java.');

-- Question 5: FILL_BLANKS
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer, explanation) VALUES
    (1, 'FILL_BLANKS', 'Java was created by _____ at _____ in _____.', 15, 5,
     '["creator", "company", "year"]',
     '["James Gosling", "Sun Microsystems", "1995"]',
     'Java was created by James Gosling at Sun Microsystems in 1995.');

-- Insert questions for Quiz 2: Spring Framework Quiz

-- Question 1: SINGLE_CHOICE
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer, explanation) VALUES
    (2, 'SINGLE_CHOICE', 'What annotation is used to mark a class as a Spring component?', 10, 1,
     '["@Bean", "@Component", "@Service", "@Configuration"]',
     '1',
     '@Component is the generic stereotype annotation for any Spring-managed component.');

-- Question 2: MULTIPLE_CHOICE
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer, explanation) VALUES
    (2, 'MULTIPLE_CHOICE', 'Which are valid Spring stereotype annotations?', 15, 2,
     '["@Component", "@Controller", "@Service", "@Repository", "@Bean"]',
     '[0, 1, 2, 3]',
     '@Component, @Controller, @Service, and @Repository are stereotype annotations. @Bean is used in configuration classes.');

-- Question 3: SORTING
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer, explanation) VALUES
    (2, 'SORTING', 'Arrange these Spring Boot application lifecycle events in order:', 20, 3,
     '["Application Started", "Application Ready", "Context Refreshed", "Context Initialized"]',
     '[3, 2, 0, 1]',
     'The correct order is: Context Initialized → Context Refreshed → Application Started → Application Ready');

-- Question 4: MATCHING
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer, explanation) VALUES
    (2, 'MATCHING', 'Match the Spring annotation with its purpose:', 20, 4,
     '[{"left":"@Autowired","right":"Dependency Injection"}, {"left":"@RequestMapping","right":"URL Mapping"}, {"left":"@Transactional","right":"Transaction Management"}]',
     '[{"left":"0","right":"0"}, {"left":"1","right":"1"}, {"left":"2","right":"2"}]',
     'Each annotation serves a specific purpose in Spring applications.');

-- Insert questions for Quiz 3: General Knowledge

-- Question 1: SINGLE_CHOICE
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer) VALUES
    (3, 'SINGLE_CHOICE', 'What is the capital of France?', 5, 1,
     '["London", "Paris", "Berlin", "Madrid"]',
     '1');

-- Question 2: TRUE_FALSE
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer) VALUES
    (3, 'TRUE_FALSE', 'The Earth is flat.', 5, 2,
     '["True", "False"]',
     '1');

-- Question 3: DROPDOWN
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer) VALUES
    (3, 'DROPDOWN', 'Select the largest ocean:', 10, 3,
     '["Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean"]',
     '3');

-- Insert questions for Quiz 4: Mathematics Challenge

-- Question 1: SHORT_ANSWER
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, correct_answer, explanation) VALUES
    (4, 'SHORT_ANSWER', 'What is 7 x 8?', 5, 1,
     '56',
     '7 multiplied by 8 equals 56.');

-- Question 2: SINGLE_CHOICE
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer) VALUES
    (4, 'SINGLE_CHOICE', 'What is the square root of 144?', 10, 2,
     '["10", "11", "12", "13"]',
     '2');

-- Question 3: MULTIPLE_CHOICE
INSERT INTO questions (quiz_id, question_type, question_text, points, question_order, answer_options, correct_answer) VALUES
    (4, 'MULTIPLE_CHOICE', 'Which of these are prime numbers?', 15, 3,
     '["2", "4", "7", "9", "11"]',
     '[0, 2, 4]');

-- Insert sample players
INSERT INTO players (nickname, session_id, games_played, last_played_at) VALUES
                                                                             ('JohnDoe', 'session-001', 5, CURRENT_TIMESTAMP - INTERVAL '1 day'),
                                                                             ('JaneSmith', 'session-002', 3, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
                                                                             ('BobJones', 'session-003', 8, CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
                                                                             ('AliceWilliams', 'session-004', 2, CURRENT_TIMESTAMP - INTERVAL '1 week');

-- Insert sample game results
INSERT INTO game_results (player_id, quiz_id, score, max_score, correct_answers, wrong_answers, total_questions, time_taken_seconds, percentage_score, session_id, started_at, completed_at, is_completed) VALUES
                                                                                                                                                                                                               (1, 1, 45, 55, 4, 1, 5, 420, 81.82, 'game-session-001', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '7 minutes', true),
                                                                                                                                                                                                               (1, 2, 50, 65, 3, 1, 4, 1200, 76.92, 'game-session-002', CURRENT_TIMESTAMP - INTERVAL '12 hours', CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '20 minutes', true),
                                                                                                                                                                                                               (2, 1, 55, 55, 5, 0, 5, 360, 100.00, 'game-session-003', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours' + INTERVAL '6 minutes', true),
                                                                                                                                                                                                               (2, 3, 15, 20, 2, 1, 3, 240, 75.00, 'game-session-004', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour' + INTERVAL '4 minutes', true),
                                                                                                                                                                                                               (3, 1, 40, 55, 3, 2, 5, 480, 72.73, 'game-session-005', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '30 minutes' + INTERVAL '8 minutes', true),
                                                                                                                                                                                                               (3, 2, 45, 65, 2, 2, 4, 1500, 69.23, 'game-session-006', CURRENT_TIMESTAMP - INTERVAL '20 minutes', CURRENT_TIMESTAMP - INTERVAL '20 minutes' + INTERVAL '25 minutes', true),
                                                                                                                                                                                                               (4, 3, 20, 20, 3, 0, 3, 180, 100.00, 'game-session-007', CURRENT_TIMESTAMP - INTERVAL '1 week', CURRENT_TIMESTAMP - INTERVAL '1 week' + INTERVAL '3 minutes', true);

-- Note: In production, you would want to populate answers_json field with actual answer details
-- For testing purposes, we're leaving it null as it's generated during actual gameplay

COMMENT ON TABLE users IS 'Sample users - default password for all is "password"';
COMMENT ON TABLE quizzes IS '4 sample quizzes covering different topics';
COMMENT ON TABLE questions IS 'Sample questions demonstrating all 8 question types';
COMMENT ON TABLE players IS 'Sample anonymous players';
COMMENT ON TABLE game_results IS 'Sample game results for testing leaderboards';