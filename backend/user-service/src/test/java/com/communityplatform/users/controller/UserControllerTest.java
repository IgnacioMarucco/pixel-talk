package com.communityplatform.users.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.communityplatform.users.dto.user.ChangePasswordDto;
import com.communityplatform.users.dto.user.UserCreateDto;
import com.communityplatform.users.dto.user.UserResponseDto;
import com.communityplatform.users.dto.user.UserUpdateDto;
import com.communityplatform.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.base-path=/api/v1")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createUserReturnsCreated() throws Exception {
        UserCreateDto request = UserCreateDto.builder()
                .username("user1")
                .email("user1@example.com")
                .password("Password1!")
                .firstName("User")
                .lastName("One")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .firstName("User")
                .lastName("One")
                .build();

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserByIdReturnsOk() throws Exception {
        UserResponseDto response = UserResponseDto.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .build();

        when(userService.getUserById(2L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"));
    }

    @Test
    void getAllUsersReturnsOk() throws Exception {
        List<UserResponseDto> response = List.of(
                UserResponseDto.builder().id(1L).username("u1").email("u1@example.com").build(),
                UserResponseDto.builder().id(2L).username("u2").email("u2@example.com").build());

        when(userService.getAllUsers()).thenReturn(response);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("u1"));
    }

    @Test
    void updateUserReturnsOk() throws Exception {
        UserUpdateDto request = UserUpdateDto.builder()
                .firstName("Updated")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .firstName("Updated")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void deleteUserReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/3"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(3L);
    }

    @Test
    void changePasswordReturnsNoContent() throws Exception {
        ChangePasswordDto request = ChangePasswordDto.builder()
                .currentPassword("Password1!")
                .newPassword("Password2!")
                .build();

        mockMvc.perform(post("/api/v1/users/4/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq(4L), any(ChangePasswordDto.class));
    }

    @Test
    void getCurrentUserReturnsOk() throws Exception {
        UserResponseDto response = UserResponseDto.builder()
                .id(5L)
                .username("current")
                .email("current@example.com")
                .build();

        when(userService.getCurrentUser("current")).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-Username", "current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("current"));
    }

    @Test
    void updateCurrentUserReturnsOk() throws Exception {
        UserUpdateDto request = UserUpdateDto.builder()
                .firstName("New")
                .build();

        UserResponseDto response = UserResponseDto.builder()
                .id(6L)
                .username("current")
                .email("current@example.com")
                .firstName("New")
                .build();

        when(userService.updateCurrentUser(eq("current"), any(UserUpdateDto.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me")
                        .header("X-Username", "current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    void getUserByUsernameReturnsOk() throws Exception {
        UserResponseDto response = UserResponseDto.builder()
                .id(7L)
                .username("user7")
                .email("user7@example.com")
                .build();

        when(userService.getUserByUsername("user7")).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/username/user7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user7@example.com"));
    }
}
