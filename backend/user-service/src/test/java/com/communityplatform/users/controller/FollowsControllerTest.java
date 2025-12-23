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

import com.communityplatform.users.dto.follow.FollowResponseDto;
import com.communityplatform.users.dto.user.UserSummaryDto;
import com.communityplatform.users.service.FollowService;

@WebMvcTest(FollowsController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.base-path=/api/v1")
class FollowsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FollowService followService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void followUserReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/follows/2")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(followService).followUser(10L, 2L);
    }

    @Test
    void unfollowUserReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/follows/2")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(followService).unfollowUser(10L, 2L);
    }

    @Test
    void getFollowersReturnsOk() throws Exception {
        List<UserSummaryDto> response = List.of(
                UserSummaryDto.builder().id(1L).username("u1").build());

        when(followService.getFollowers(2L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/follows/2/followers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("u1"));
    }

    @Test
    void getCurrentUserFollowingReturnsOk() throws Exception {
        List<UserSummaryDto> response = List.of(
                UserSummaryDto.builder().id(3L).username("u3").build());

        when(followService.getFollowing(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/follows/me/following")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("u3"));
    }

    @Test
    void isFollowingReturnsOk() throws Exception {
        when(followService.isFollowing(anyLong(), anyLong()))
                .thenReturn(FollowResponseDto.builder().following(true).build());

        mockMvc.perform(get("/api/v1/follows/2/is-following")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}
