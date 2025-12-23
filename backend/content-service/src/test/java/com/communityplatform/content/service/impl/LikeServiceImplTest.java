package com.communityplatform.content.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.communityplatform.content.UserProfileDto;
import com.communityplatform.content.UserServiceClient;
import com.communityplatform.content.dto.like.LikeResponseDto;
import com.communityplatform.content.entity.CommentEntity;
import com.communityplatform.content.entity.LikeEntity;
import com.communityplatform.content.entity.PostEntity;
import com.communityplatform.content.mapper.LikeMapper;
import com.communityplatform.content.repository.CommentRepository;
import com.communityplatform.content.repository.LikeRepository;
import com.communityplatform.content.repository.PostRepository;
import com.communityplatform.content.service.CommentService;
import com.communityplatform.content.service.PostService;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private PostService postService;

    @Mock
    private CommentService commentService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Test
    void likePostCreatesLike() {
        when(postRepository.findByIdAndActive(1L)).thenReturn(Optional.of(PostEntity.builder().id(1L).build()));
        when(likeRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(false);

        LikeEntity saved = LikeEntity.builder().id(1L).userId(10L).postId(1L).build();
        when(likeRepository.save(any(LikeEntity.class))).thenReturn(saved);

        LikeResponseDto response = LikeResponseDto.builder().id(1L).userId(10L).postId(1L).build();
        when(likeMapper.toResponseDto(saved)).thenReturn(response);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", null, null, null)));

        LikeResponseDto result = likeService.likePost(1L, 10L);

        assertThat(result.getUsername()).isEqualTo("user");
        verify(postService).incrementLikeCount(1L);
    }

    @Test
    void unlikePostDeletesLike() {
        LikeEntity existing = LikeEntity.builder().id(2L).userId(10L).postId(1L).build();
        when(likeRepository.findByUserIdAndPostId(10L, 1L)).thenReturn(Optional.of(existing));

        likeService.unlikePost(1L, 10L);

        verify(likeRepository).delete(existing);
        verify(postService).decrementLikeCount(1L);
    }

    @Test
    void likeCommentCreatesLike() {
        when(commentRepository.findById(2L)).thenReturn(Optional.of(CommentEntity.builder().id(2L).build()));
        when(likeRepository.existsByUserIdAndCommentId(10L, 2L)).thenReturn(false);

        LikeEntity saved = LikeEntity.builder().id(3L).userId(10L).commentId(2L).build();
        when(likeRepository.save(any(LikeEntity.class))).thenReturn(saved);

        LikeResponseDto response = LikeResponseDto.builder().id(3L).userId(10L).commentId(2L).build();
        when(likeMapper.toResponseDto(saved)).thenReturn(response);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", null, null, null)));

        likeService.likeComment(2L, 10L);

        verify(commentService).incrementLikeCount(2L);
    }

    @Test
    void unlikeCommentDeletesLike() {
        LikeEntity existing = LikeEntity.builder().id(4L).userId(10L).commentId(2L).build();
        when(likeRepository.findByUserIdAndCommentId(10L, 2L)).thenReturn(Optional.of(existing));

        likeService.unlikeComment(2L, 10L);

        verify(likeRepository).delete(existing);
        verify(commentService).decrementLikeCount(2L);
    }

    @Test
    void hasLikedPostReturnsValue() {
        when(likeRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(true);

        boolean result = likeService.hasLikedPost(1L, 10L);

        assertThat(result).isTrue();
    }

    @Test
    void hasLikedCommentReturnsValue() {
        when(likeRepository.existsByUserIdAndCommentId(10L, 2L)).thenReturn(false);

        boolean result = likeService.hasLikedComment(2L, 10L);

        assertThat(result).isFalse();
    }

    @Test
    void getPostLikesMapsResults() {
        LikeEntity like = LikeEntity.builder().id(5L).userId(10L).postId(1L).build();
        LikeResponseDto response = LikeResponseDto.builder().id(5L).userId(10L).postId(1L).build();

        when(likeRepository.findByPostId(1L)).thenReturn(List.of(like));
        when(likeMapper.toResponseDto(like)).thenReturn(response);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", null, null, null)));

        List<LikeResponseDto> result = likeService.getPostLikes(1L);

        assertThat(result.get(0).getUsername()).isEqualTo("user");
    }

    @Test
    void getCommentLikesMapsResults() {
        LikeEntity like = LikeEntity.builder().id(6L).userId(10L).commentId(2L).build();
        LikeResponseDto response = LikeResponseDto.builder().id(6L).userId(10L).commentId(2L).build();

        when(likeRepository.findByCommentId(2L)).thenReturn(List.of(like));
        when(likeMapper.toResponseDto(like)).thenReturn(response);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", null, null, null)));

        List<LikeResponseDto> result = likeService.getCommentLikes(2L);

        assertThat(result).hasSize(1);
    }
}
