package com.communityplatform.content.controller;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.communityplatform.content.dto.post.PostCreateDto;
import com.communityplatform.content.dto.post.PostResponseDto;
import com.communityplatform.content.dto.post.PostSummaryDto;
import com.communityplatform.content.dto.post.PostUpdateDto;
import com.communityplatform.content.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.base-path=/api/v1")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createPostReturnsCreated() throws Exception {
        PostCreateDto request = PostCreateDto.builder()
                .title("Title")
                .content("Content")
                .build();

        PostResponseDto response = PostResponseDto.builder()
                .id(1L)
                .title("Title")
                .content("Content")
                .userId(10L)
                .build();

        when(postService.createPost(any(PostCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/posts")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getPostReturnsOk() throws Exception {
        PostResponseDto response = PostResponseDto.builder()
                .id(2L)
                .title("Title")
                .content("Content")
                .build();

        when(postService.getPostById(2L, 10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/posts/2")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void updatePostReturnsOk() throws Exception {
        PostUpdateDto request = PostUpdateDto.builder()
                .title("Updated")
                .content("Content")
                .build();

        PostResponseDto response = PostResponseDto.builder()
                .id(3L)
                .title("Updated")
                .build();

        when(postService.updatePost(eq(3L), any(PostUpdateDto.class), eq(10L))).thenReturn(response);

        mockMvc.perform(put("/api/v1/posts/3")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deletePostReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/4")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(4L, 10L);
    }

    @Test
    void getAllPostsReturnsOk() throws Exception {
        PostSummaryDto summary = PostSummaryDto.builder().id(5L).title("Title").build();
        when(postService.getAllPosts(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    void getFollowingPostsReturnsOk() throws Exception {
        PostSummaryDto summary = PostSummaryDto.builder().id(6L).title("Followed").build();
        when(postService.getFollowingPosts(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts/following")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Followed"));
    }

    @Test
    void getFeedPostsReturnsOk() throws Exception {
        PostSummaryDto summary = PostSummaryDto.builder().id(7L).title("Feed").build();
        when(postService.getFeedPosts(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts/feed")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Feed"));
    }

    @Test
    void getTrendingPostsReturnsOk() throws Exception {
        PostSummaryDto summary = PostSummaryDto.builder().id(8L).title("Trending").build();
        when(postService.getTrendingPosts(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts/trending")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Trending"));
    }

    @Test
    void searchPostsReturnsOk() throws Exception {
        PostSummaryDto summary = PostSummaryDto.builder().id(9L).title("Found").build();
        when(postService.searchPosts(eq("term"), eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts/search")
                        .param("q", "term")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Found"));
    }

    @Test
    void getPostsByUserReturnsOk() throws Exception {
        PostSummaryDto summary = PostSummaryDto.builder().id(10L).title("UserPost").build();
        when(postService.getPostsByUserId(eq(2L), eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts/user/2")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("UserPost"));
    }
}
