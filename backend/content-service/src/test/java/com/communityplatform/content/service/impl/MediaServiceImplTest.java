package com.communityplatform.content.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.communityplatform.content.dto.media.MediaPresignedConfirmRequestDto;
import com.communityplatform.content.dto.media.MediaPresignedDownloadResponseDto;
import com.communityplatform.content.dto.media.MediaPresignedUploadRequestDto;
import com.communityplatform.content.dto.media.MediaPresignedUploadResponseDto;
import com.communityplatform.content.dto.media.MediaResponseDto;
import com.communityplatform.content.dto.media.MediaUploadResponseDto;
import com.communityplatform.content.entity.MediaEntity;
import com.communityplatform.content.mapper.MediaMapper;
import com.communityplatform.content.repository.MediaRepository;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaMapper mediaMapper;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioClient presignedMinioClient;

    private MediaServiceImpl mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaServiceImpl(mediaRepository, mediaMapper, minioClient, presignedMinioClient);
        ReflectionTestUtils.setField(mediaService, "bucketName", "media");
        ReflectionTestUtils.setField(mediaService, "publicUrl", "http://localhost:9000");
        ReflectionTestUtils.setField(mediaService, "presignedExpirySeconds", 300);
    }

    @Test
    void uploadMediaStoresFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "data".getBytes(StandardCharsets.UTF_8));

        MediaEntity saved = MediaEntity.builder()
                .id(1L)
                .originalFilename("image.png")
                .storedFilename("stored.png")
                .mimeType("image/png")
                .fileSize(4L)
                .bucketName("media")
                .uploaderUserId(10L)
                .url("http://localhost:9000/media/stored.png")
                .build();

        MediaUploadResponseDto response = MediaUploadResponseDto.builder()
                .id(1L)
                .url(saved.getUrl())
                .build();

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);
        when(mediaRepository.save(any(MediaEntity.class))).thenReturn(saved);
        when(mediaMapper.toUploadResponseDto(saved)).thenReturn(response);

        MediaUploadResponseDto result = mediaService.uploadMedia(file, 10L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getMediaByIdReturnsResponse() {
        MediaEntity entity = MediaEntity.builder()
                .id(2L)
                .url("http://localhost:9000/media/2.png")
                .build();

        MediaResponseDto response = MediaResponseDto.builder()
                .id(2L)
                .url(entity.getUrl())
                .build();

        when(mediaRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(mediaMapper.toResponseDto(entity)).thenReturn(response);

        MediaResponseDto result = mediaService.getMediaById(2L);

        assertThat(result.getUrl()).isEqualTo(entity.getUrl());
    }

    @Test
    void getMediaByUserReturnsPage() {
        MediaEntity entity = MediaEntity.builder().id(3L).uploaderUserId(10L).build();
        MediaResponseDto response = MediaResponseDto.builder().id(3L).build();

        when(mediaRepository.findByUploaderUserId(eq(10L), any()))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(mediaMapper.toResponseDto(entity)).thenReturn(response);

        var page = mediaService.getMediaByUserId(10L, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void createPresignedUploadReturnsUrl() throws Exception {
        MediaPresignedUploadRequestDto request = MediaPresignedUploadRequestDto.builder()
                .originalFilename("image.png")
                .build();

        when(presignedMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://upload");

        MediaPresignedUploadResponseDto result = mediaService.createPresignedUpload(request, 10L);

        assertThat(result.getUploadUrl()).isEqualTo("http://upload");
    }

    @Test
    void confirmPresignedUploadReturnsExisting() {
        MediaPresignedConfirmRequestDto request = MediaPresignedConfirmRequestDto.builder()
                .storedFilename("stored.png")
                .originalFilename("image.png")
                .fileSize(4L)
                .build();

        MediaEntity existing = MediaEntity.builder()
                .id(4L)
                .storedFilename("stored.png")
                .originalFilename("image.png")
                .uploaderUserId(10L)
                .build();

        MediaUploadResponseDto response = MediaUploadResponseDto.builder()
                .id(4L)
                .storedFilename("stored.png")
                .build();

        when(mediaRepository.findByStoredFilename("stored.png")).thenReturn(Optional.of(existing));
        when(mediaMapper.toUploadResponseDto(existing)).thenReturn(response);

        MediaUploadResponseDto result = mediaService.confirmPresignedUpload(request, 10L);

        assertThat(result.getId()).isEqualTo(4L);
    }

    @Test
    void confirmPresignedUploadStoresNew() throws Exception {
        MediaPresignedConfirmRequestDto request = MediaPresignedConfirmRequestDto.builder()
                .storedFilename("stored.png")
                .originalFilename("image.png")
                .fileSize(4L)
                .build();

        when(mediaRepository.findByStoredFilename("stored.png")).thenReturn(Optional.empty());

        StatObjectResponse stat = org.mockito.Mockito.mock(StatObjectResponse.class);
        when(stat.size()).thenReturn(4L);
        when(stat.contentType()).thenReturn("image/png");
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(stat);

        MediaEntity saved = MediaEntity.builder()
                .id(5L)
                .storedFilename("stored.png")
                .originalFilename("image.png")
                .mimeType("image/png")
                .fileSize(4L)
                .bucketName("media")
                .uploaderUserId(10L)
                .url("http://localhost:9000/media/stored.png")
                .build();

        MediaUploadResponseDto response = MediaUploadResponseDto.builder()
                .id(5L)
                .storedFilename("stored.png")
                .build();

        when(mediaRepository.save(any(MediaEntity.class))).thenReturn(saved);
        when(mediaMapper.toUploadResponseDto(saved)).thenReturn(response);

        MediaUploadResponseDto result = mediaService.confirmPresignedUpload(request, 10L);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void createPresignedDownloadReturnsUrl() throws Exception {
        MediaEntity entity = MediaEntity.builder()
                .id(6L)
                .bucketName("media")
                .storedFilename("stored.png")
                .build();

        when(mediaRepository.findById(6L)).thenReturn(Optional.of(entity));
        when(presignedMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://download");

        MediaPresignedDownloadResponseDto result = mediaService.createPresignedDownload(6L);

        assertThat(result.getDownloadUrl()).isEqualTo("http://download");
    }

    @Test
    void deleteMediaRemovesObject() throws Exception {
        MediaEntity entity = MediaEntity.builder()
                .id(7L)
                .bucketName("media")
                .storedFilename("stored.png")
                .uploaderUserId(10L)
                .build();

        when(mediaRepository.findById(7L)).thenReturn(Optional.of(entity));

        mediaService.deleteMedia(7L, 10L);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
        verify(mediaRepository).delete(entity);
    }
}
