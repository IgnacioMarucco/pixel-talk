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

import com.communityplatform.content.dto.comment.CommentCreateDto;
import com.communityplatform.content.dto.comment.CommentResponseDto;
import com.communityplatform.content.dto.comment.CommentUpdateDto;
import com.communityplatform.content.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.base-path=/api/v1")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void createCommentReturnsCreated() throws Exception {
        CommentCreateDto request = CommentCreateDto.builder()
                .content("Nice")
                .build();

        CommentResponseDto response = CommentResponseDto.builder()
                .id(1L)
                .content("Nice")
                .build();

        when(commentService.createComment(any(CommentCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/posts/1/comments")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getCommentReturnsOk() throws Exception {
        CommentResponseDto response = CommentResponseDto.builder()
                .id(2L)
                .content("Comment")
                .build();

        when(commentService.getCommentById(2L, 10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/comments/2")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Comment"));
    }

    @Test
    void updateCommentReturnsOk() throws Exception {
        CommentUpdateDto request = CommentUpdateDto.builder()
                .content("Updated")
                .build();

        CommentResponseDto response = CommentResponseDto.builder()
                .id(3L)
                .content("Updated")
                .build();

        when(commentService.updateComment(eq(3L), any(CommentUpdateDto.class), eq(10L))).thenReturn(response);

        mockMvc.perform(put("/api/v1/comments/3")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated"));
    }

    @Test
    void deleteCommentReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/comments/4")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(4L, 10L);
    }

    @Test
    void getCommentsByPostReturnsOk() throws Exception {
        CommentResponseDto response = CommentResponseDto.builder().id(5L).content("One").build();
        when(commentService.getCommentsByPostId(eq(1L), eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts/1/comments")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("One"));
    }

    @Test
    void getTopLevelCommentsReturnsOk() throws Exception {
        CommentResponseDto response = CommentResponseDto.builder().id(6L).content("Top").build();
        when(commentService.getTopLevelComments(eq(1L), eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts/1/comments/top")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Top"));
    }

    @Test
    void getRepliesReturnsOk() throws Exception {
        CommentResponseDto response = CommentResponseDto.builder().id(7L).content("Reply").build();
        when(commentService.getReplies(2L, 10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/comments/2/replies")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Reply"));
    }
}
