package com.example.quizapp.security;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    // UserDetailsService jest potrzebny do kontekstu bezpieczeństwa w WebMvcTest
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /register - Should register user")
    @WithMockUser
    void shouldRegisterUser() throws Exception {
        // Given
        UserRegistrationDto request = UserRegistrationDto.builder()
                .username("newuser")
                .password("password123")
                .email("new@example.com")
                .build();

        User user = User.builder().id(1L).username("newuser").build();
        UserDto responseDto = UserDto.builder().id(1L).username("newuser").build();

        when(userService.createUser(any(UserRegistrationDto.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("POST /register - Should return 400 on invalid input")
    @WithMockUser
    void shouldReturn400OnInvalidRegisterInput() throws Exception {
        // Given - empty username
        UserRegistrationDto request = UserRegistrationDto.builder()
                .username("")
                .password("pass")
                .email("invalid-email")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /check-username - Should return availability")
    @WithMockUser
    void shouldCheckUsername() throws Exception {
        // Given
        when(userService.usernameExists("testuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/users/check-username")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    // Test dla GET /me wymagałby wstrzyknięcia CustomUserDetails.
    // Ponieważ @WithMockUser wstawia standardowego Usera, pominiemy ten specyficzny test
    // w podstawowym zestawie lub musielibyśmy stworzyć własną adnotację @WithCustomUserDetails.
}