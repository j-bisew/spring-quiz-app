package com.example.quizapp.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(RankingController.class)
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;

    @MockitoBean
    private RankingExportService rankingExportService;

    // Zamockowanie UserDetailsService jeśli jest wymagane przez globalny konfig security
    @MockitoBean
    private com.example.quizapp.security.CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /quiz/{id} - Should return top rankings")
    @WithMockUser
    void shouldGetQuizRankings() throws Exception {
        // Given
        RankingDto rankingDto = RankingDto.builder()
                .playerNickname("Champion")
                .score(100)
                .build();

        when(rankingService.getTopRankings(eq(1L), anyInt()))
                .thenReturn(List.of(rankingDto));

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/quiz/1")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].playerNickname").value("Champion"))
                .andExpect(jsonPath("$[0].score").value(100));
    }

    @Test
    @DisplayName("GET /quiz/{id}/full - Should return full leaderboard")
    @WithMockUser
    void shouldGetFullLeaderboard() throws Exception {
        // Given
        when(rankingService.getFullLeaderboard(1L))
                .thenReturn(List.of(new RankingDto(), new RankingDto()));

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/quiz/1/full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @DisplayName("GET /quiz/{quizId}/player/{playerId} - Should return player position")
    @WithMockUser
    void shouldGetPlayerRanking() throws Exception {
        // Given
        RankingPositionDto positionDto = RankingPositionDto.builder()
                .playerId(100L)
                .playerNickname("Player1")
                .position(5)
                .totalPlayers(20)
                .build();

        when(rankingService.getPlayerRanking(1L, 100L)).thenReturn(positionDto);

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/quiz/1/player/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(5))
                .andExpect(jsonPath("$.totalPlayers").value(20));
    }

    @Test
    @DisplayName("GET /global - Should return global rankings")
    @WithMockUser
    void shouldGetGlobalRankings() throws Exception {
        // Given
        GlobalRankingDto globalDto = GlobalRankingDto.builder()
                .playerNickname("GlobalMaster")
                .totalGamesPlayed(50)
                .build();

        when(rankingService.getGlobalRankings(10)).thenReturn(List.of(globalDto));

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerNickname").value("GlobalMaster"));
    }

    @Test
    @DisplayName("GET /quiz/{id}/export/csv - Should download CSV")
    @WithMockUser
    void shouldExportToCsv() throws Exception {
        // Given
        Resource csvResource = new ByteArrayResource("col1,col2".getBytes());
        when(rankingExportService.exportToCsv(1L)).thenReturn(csvResource);

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/quiz/1/export/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"quiz_1_rankings.csv\""));
    }

    @Test
    @DisplayName("GET /quiz/{id}/export/pdf - Should download PDF")
    @WithMockUser
    void shouldExportToPdf() throws Exception {
        // Given
        Resource pdfResource = new ByteArrayResource("PDF-CONTENT".getBytes());
        when(rankingExportService.exportToPdf(1L)).thenReturn(pdfResource);

        // When & Then
        mockMvc.perform(get("/api/v1/rankings/quiz/1/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"quiz_1_rankings.pdf\""));
    }
}