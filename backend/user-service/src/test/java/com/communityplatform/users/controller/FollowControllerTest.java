package com.communityplatform.users.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.communityplatform.users.dto.follow.FollowCountDto;
import com.communityplatform.users.dto.follow.FollowResponseDto;
import com.communityplatform.users.dto.user.UserSummaryDto;
import com.communityplatform.users.repository.UserRepository;
import com.communityplatform.users.service.FollowService;

@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.base-path=/api/v1")
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void followUserReturnsNoContent() throws Exception {
        when(userRepository.existsById(10L)).thenReturn(true);

        mockMvc.perform(post("/api/v1/users/5/follow")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(followService).followUser(10L, 5L);
    }

    @Test
    void unfollowUserReturnsNoContent() throws Exception {
        when(userRepository.existsById(10L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/users/5/follow")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(followService).unfollowUser(10L, 5L);
    }

    @Test
    void getFollowersReturnsOk() throws Exception {
        List<UserSummaryDto> response = List.of(
                UserSummaryDto.builder().id(1L).username("u1").build());

        when(followService.getFollowers(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/5/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("u1"));
    }

    @Test
    void getFollowingReturnsOk() throws Exception {
        List<UserSummaryDto> response = List.of(
                UserSummaryDto.builder().id(2L).username("u2").build());

        when(followService.getFollowing(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/5/following"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("u2"));
    }

    @Test
    void isFollowingReturnsOk() throws Exception {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        FollowResponseDto response = FollowResponseDto.builder().following(true).build();
        when(followService.isFollowing(10L, 5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/5/follow/check")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true));
    }

    @Test
    void getFollowersCountReturnsOk() throws Exception {
        when(followService.getFollowersCount(5L)).thenReturn(FollowCountDto.builder().count(3).build());

        mockMvc.perform(get("/api/v1/users/5/followers/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void getFollowingCountReturnsOk() throws Exception {
        when(followService.getFollowingCount(5L)).thenReturn(FollowCountDto.builder().count(4).build());

        mockMvc.perform(get("/api/v1/users/5/following/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(4));
    }
}
