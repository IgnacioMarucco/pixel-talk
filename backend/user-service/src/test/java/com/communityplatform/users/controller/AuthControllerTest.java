package com.communityplatform.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.communityplatform.users.dto.auth.AuthResponseDto;
import com.communityplatform.users.dto.auth.LoginRequestDto;
import com.communityplatform.users.dto.auth.RefreshTokenRequestDto;
import com.communityplatform.users.dto.auth.RegisterRequestDto;
import com.communityplatform.users.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.base-path=/api/v1")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void registerReturnsCreated() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("user1")
                .email("user1@example.com")
                .password("Password1!")
                .firstName("User")
                .lastName("One")
                .build();

        AuthResponseDto response = AuthResponseDto.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .userId(1L)
                .username("user1")
                .email("user1@example.com")
                .roles(Set.of("ROLE_USER"))
                .expiresIn(3600L)
                .build();

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void loginReturnsOk() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .usernameOrEmail("user1")
                .password("Password1!")
                .build();

        AuthResponseDto response = AuthResponseDto.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .userId(1L)
                .username("user1")
                .email("user1@example.com")
                .roles(Set.of("ROLE_USER"))
                .expiresIn(3600L)
                .build();

        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void refreshReturnsOk() throws Exception {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("refresh")
                .build();

        AuthResponseDto response = AuthResponseDto.builder()
                .accessToken("access")
                .refreshToken("refresh2")
                .userId(1L)
                .username("user1")
                .email("user1@example.com")
                .roles(Set.of("ROLE_USER"))
                .expiresIn(3600L)
                .build();

        when(authService.refreshToken(any(RefreshTokenRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("refresh2"));
    }

    @Test
    void logoutReturnsNoContent() throws Exception {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("refresh")
                .build();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).logout("refresh");
    }
}
