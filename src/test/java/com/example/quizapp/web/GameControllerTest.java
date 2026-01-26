package com.example.quizapp.web;

import com.example.quizapp.game.*;
import com.example.quizapp.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /start - Should start game and return 200")
    @WithMockUser
    void shouldStartGame() throws Exception {
        StartGameRequest request = new StartGameRequest(1L, "Player1", null, null);
        StartGameResponse response = StartGameResponse.builder().sessionId("sess-1").build();

        when(gameService.startGame(any(StartGameRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/game/start")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-1"));
    }

    @Test
    @DisplayName("POST /start - Should return 400 when input invalid")
    @WithMockUser
    void shouldReturn400OnInvalidInput() throws Exception {
        StartGameRequest request = new StartGameRequest(1L, "", null, null); // Pusty nick

        mockMvc.perform(post("/api/v1/game/start")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /submit - Should submit answers and return result")
    @WithMockUser
    void shouldSubmitAnswers() throws Exception {
        SubmitAnswersRequest request = SubmitAnswersRequest.builder()
                .sessionId("sess-1")
                .quizId(1L)
                .playerId(1L)
                .answers(List.of(new SubmitAnswersRequest.AnswerSubmission(1L, "Answer")))
                .build();
        GameResultDto resultDto = GameResultDto.builder().score(10).passed(true).build();

        when(gameService.submitAnswers(any(SubmitAnswersRequest.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/v1/game/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(10))
                .andExpect(jsonPath("$.passed").value(true));
    }

    @Test
    @DisplayName("GET /result/{id} - Should return game result")
    @WithMockUser
    void shouldGetGameResult() throws Exception {
        GameResultDto resultDto = GameResultDto.builder().id(100L).playerNickname("Winner").build();

        when(gameService.getGameResult(100L)).thenReturn(resultDto);

        mockMvc.perform(get("/api/v1/game/result/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerNickname").value("Winner"));
    }

    @Test
    @DisplayName("GET /result/{id} - Should return 404 when result not found")
    @WithMockUser
    void shouldReturn404WhenResultNotFound() throws Exception {
        when(gameService.getGameResult(999L))
                .thenThrow(new com.example.quizapp.common.exception.ResourceNotFoundException("Result not found"));

        mockMvc.perform(get("/api/v1/game/result/999"))
                .andExpect(status().isNotFound());
    }
}