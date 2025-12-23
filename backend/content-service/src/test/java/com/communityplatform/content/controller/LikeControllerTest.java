package com.communityplatform.content.controller;

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

import com.communityplatform.content.dto.like.LikeResponseDto;
import com.communityplatform.content.service.LikeService;

@WebMvcTest(LikeController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.base-path=/api/v1")
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void likePostReturnsCreated() throws Exception {
        LikeResponseDto response = LikeResponseDto.builder().id(1L).userId(10L).postId(5L).build();
        when(likeService.likePost(5L, 10L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/posts/5/like")
                        .header("X-User-Id", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void unlikePostReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/5/like")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(likeService).unlikePost(5L, 10L);
    }

    @Test
    void unlikePostAliasReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/posts/5/unlike")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(likeService).unlikePost(5L, 10L);
    }

    @Test
    void hasLikedPostReturnsOk() throws Exception {
        when(likeService.hasLikedPost(5L, 10L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/posts/5/like/check")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void getPostLikesReturnsOk() throws Exception {
        LikeResponseDto response = LikeResponseDto.builder().id(2L).postId(5L).userId(10L).build();
        when(likeService.getPostLikes(5L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/posts/5/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));
    }

    @Test
    void likeCommentReturnsCreated() throws Exception {
        LikeResponseDto response = LikeResponseDto.builder().id(3L).commentId(9L).userId(10L).build();
        when(likeService.likeComment(9L, 10L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/comments/9/like")
                        .header("X-User-Id", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L));
    }

    @Test
    void unlikeCommentReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/comments/9/like")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(likeService).unlikeComment(9L, 10L);
    }

    @Test
    void hasLikedCommentReturnsOk() throws Exception {
        when(likeService.hasLikedComment(9L, 10L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/comments/9/like/check")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    void getCommentLikesReturnsOk() throws Exception {
        LikeResponseDto response = LikeResponseDto.builder().id(4L).commentId(9L).userId(10L).build();
        when(likeService.getCommentLikes(9L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/comments/9/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4L));
    }
}
