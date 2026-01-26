package com.example.quizapp.question;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestionService questionService;

    // Ponieważ w kontrolerze nie ma SecurityConfig, musimy zamockować UserDetailsService,
    // jeśli jest używany w globalnej konfiguracji, lub po prostu użyć @WithMockUser.
    @MockitoBean
    private com.example.quizapp.security.CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/questions/quiz/{quizId} - Should return questions list")
    @WithMockUser
    void shouldGetQuestionsByQuizId() throws Exception {
        // Given
        QuestionDto dto = QuestionDto.builder().id(1L).questionText("Test?").build();
        when(questionService.getQuestionsByQuizId(10L)).thenReturn(List.of(dto));

        // When & Then
        mockMvc.perform(get("/api/v1/questions/quiz/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].questionText").value("Test?"));
    }

    @Test
    @DisplayName("GET /api/v1/questions/{id} - Should return question")
    @WithMockUser
    void shouldGetQuestionById() throws Exception {
        // Given
        QuestionDto dto = QuestionDto.builder().id(1L).questionText("Single question").build();
        when(questionService.getQuestionById(1L)).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/v1/questions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionText").value("Single question"));
    }

    @Test
    @DisplayName("POST /api/v1/questions - Should create question")
    @WithMockUser
    void shouldCreateQuestion() throws Exception {
        // Given
        QuestionDto requestDto = QuestionDto.builder()
                .quizId(1L)
                .questionText("New Question")
                .questionType(QuestionType.SINGLE_CHOICE)
                .points(10)
                .answerOptions("[]") // uproszczenie dla testu kontrolera
                .correctAnswer("0")
                .build();

        QuestionDto responseDto = QuestionDto.builder()
                .id(100L)
                .questionText("New Question")
                .build();

        when(questionService.createQuestion(any(QuestionDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/questions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    @DisplayName("POST /api/v1/questions/{id}/validate - Should validate answer")
    @WithMockUser
    void shouldValidateAnswer() throws Exception {
        // Given
        AnswerValidationRequest request = new AnswerValidationRequest("my answer");
        when(questionService.validateAnswer(eq(1L), eq("my answer"))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/questions/1/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/questions/{id} - Should delete question")
    @WithMockUser
    void shouldDeleteQuestion() throws Exception {
        // Given
        doNothing().when(questionService).deleteQuestion(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/questions/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}