package com.communityplatform.content.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.communityplatform.content.dto.media.MediaPresignedConfirmRequestDto;
import com.communityplatform.content.dto.media.MediaPresignedDownloadResponseDto;
import com.communityplatform.content.dto.media.MediaPresignedUploadRequestDto;
import com.communityplatform.content.dto.media.MediaPresignedUploadResponseDto;
import com.communityplatform.content.dto.media.MediaResponseDto;
import com.communityplatform.content.dto.media.MediaUploadResponseDto;
import com.communityplatform.content.service.MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MediaController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "api.base-path=/api/v1")
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MediaService mediaService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void uploadMediaReturnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                MediaType.IMAGE_PNG_VALUE,
                "data".getBytes(StandardCharsets.UTF_8));

        MediaUploadResponseDto response = MediaUploadResponseDto.builder()
                .id(1L)
                .url("http://example.com/file")
                .build();

        when(mediaService.uploadMedia(any(), eq(10L))).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/media")
                        .file(file)
                        .header("X-User-Id", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createPresignedUploadReturnsOk() throws Exception {
        MediaPresignedUploadRequestDto request = MediaPresignedUploadRequestDto.builder()
                .originalFilename("file.png")
                .build();

        MediaPresignedUploadResponseDto response = MediaPresignedUploadResponseDto.builder()
                .uploadUrl("http://upload")
                .objectKey("key")
                .build();

        when(mediaService.createPresignedUpload(any(MediaPresignedUploadRequestDto.class), eq(10L)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/media/presigned/upload")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("http://upload"));
    }

    @Test
    void confirmPresignedUploadReturnsCreated() throws Exception {
        MediaPresignedConfirmRequestDto request = MediaPresignedConfirmRequestDto.builder()
                .storedFilename("stored.png")
                .originalFilename("file.png")
                .fileSize(10L)
                .build();

        MediaUploadResponseDto response = MediaUploadResponseDto.builder()
                .id(2L)
                .url("http://example.com/stored.png")
                .build();

        when(mediaService.confirmPresignedUpload(any(MediaPresignedConfirmRequestDto.class), eq(10L)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/media/presigned/confirm")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    void getMediaByIdReturnsOk() throws Exception {
        MediaResponseDto response = MediaResponseDto.builder()
                .id(3L)
                .url("http://example.com/media")
                .build();

        when(mediaService.getMediaById(3L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/media/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://example.com/media"));
    }

    @Test
    void createPresignedDownloadReturnsOk() throws Exception {
        MediaPresignedDownloadResponseDto response = MediaPresignedDownloadResponseDto.builder()
                .downloadUrl("http://download")
                .build();

        when(mediaService.createPresignedDownload(4L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/media/4/presigned-download"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value("http://download"));
    }

    @Test
    void getMediaByUserReturnsOk() throws Exception {
        MediaResponseDto response = MediaResponseDto.builder()
                .id(5L)
                .url("http://example.com/user")
                .build();

        when(mediaService.getMediaByUserId(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/media/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5L));
    }

    @Test
    void deleteMediaReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/media/6")
                        .header("X-User-Id", "10"))
                .andExpect(status().isNoContent());

        verify(mediaService).deleteMedia(6L, 10L);
    }
}
