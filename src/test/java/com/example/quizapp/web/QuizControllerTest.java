package com.example.quizapp.web;

import com.example.quizapp.quiz.QuizController;
import com.example.quizapp.quiz.QuizDto;
import com.example.quizapp.quiz.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuizService quizService;

    @MockitoBean
    private com.example.quizapp.security.CustomUserDetailsService userDetailsService;

    // ==================== GET / SEARCH Tests ====================

    @Test
    @DisplayName("GET /api/v1/quizzes/paginated - Should return all quizzes paginated")
    @WithMockUser
    void shouldGetAllQuizzesPaginated() throws Exception {
        // Given
        QuizDto quizDto = QuizDto.builder().id(1L).title("Quiz 1").build();
        Page<QuizDto> page = new PageImpl<>(List.of(quizDto));

        // POPRAWKA: Metoda w serwisie to getAllActiveQuizzes(Pageable)
        when(quizService.getAllActiveQuizzes(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/quizzes/paginated")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Quiz 1"));
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/search/paginated - Should search quizzes paginated")
    @WithMockUser
    void shouldSearchQuizzesPaginated() throws Exception {
        // Given
        QuizDto quizDto = QuizDto.builder().id(1L).title("Java Basics").build();
        Page<QuizDto> page = new PageImpl<>(List.of(quizDto));

        when(quizService.searchQuizzes(eq("Java"), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/quizzes/search/paginated")
                        .param("keyword", "Java")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Java Basics"));
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/creator/{username} - Should get quizzes by creator")
    @WithMockUser
    void shouldGetQuizzesByCreator() throws Exception {
        // Given
        QuizDto quizDto = QuizDto.builder().id(1L).createdBy("user1").build();
        when(quizService.getQuizzesByCreator("user1")).thenReturn(List.of(quizDto));

        // When & Then
        mockMvc.perform(get("/api/v1/quizzes/creator/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].createdBy").value("user1"));
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/{id}/with-questions - Should get quiz with questions")
    @WithMockUser
    void shouldGetQuizByIdWithQuestions() throws Exception {
        // Given
        QuizDto quizDto = QuizDto.builder().id(1L).title("Full Quiz").build();

        // POPRAWKA: Metoda w serwisie to getQuizByIdWithQuestions
        when(quizService.getQuizByIdWithQuestions(1L)).thenReturn(quizDto);

        // When & Then
        mockMvc.perform(get("/api/v1/quizzes/1/with-questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Full Quiz"));
    }

    // ==================== UPDATE Tests ====================

    @Test
    @DisplayName("PUT /api/v1/quizzes/{id} - Should update quiz")
    @WithMockUser
    void shouldUpdateQuiz() throws Exception {
        // Given
        QuizDto requestDto = QuizDto.builder().title("Updated Title").build();
        QuizDto responseDto = QuizDto.builder().id(1L).title("Updated Title").build();

        when(quizService.updateQuiz(eq(1L), any(QuizDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(put("/api/v1/quizzes/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    // ==================== DELETE / OTHER Tests ====================

    @Test
    @DisplayName("DELETE /api/v1/quizzes/{id}/permanent - Should permanently delete quiz")
    @WithMockUser(roles = "ADMIN")
    void shouldPermanentlyDeleteQuiz() throws Exception {
        doNothing().when(quizService).permanentlyDeleteQuiz(1L);

        mockMvc.perform(delete("/api/v1/quizzes/1/permanent")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/count - Should count active quizzes")
    @WithMockUser
    void shouldCountActiveQuizzes() throws Exception {
        when(quizService.countActiveQuizzes()).thenReturn(5L);

        mockMvc.perform(get("/api/v1/quizzes/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/quizzes/{id} - Should return quiz basic info")
    @WithMockUser
    void shouldGetQuizById() throws Exception {
        QuizDto quizDto = QuizDto.builder().id(10L).title("Basic Info").build();
        when(quizService.getQuizById(10L)).thenReturn(quizDto);

        mockMvc.perform(get("/api/v1/quizzes/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Basic Info"));
    }
}