package com.communityplatform.content.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.communityplatform.content.UserProfileDto;
import com.communityplatform.content.UserServiceClient;
import com.communityplatform.content.dto.comment.CommentCreateDto;
import com.communityplatform.content.dto.comment.CommentResponseDto;
import com.communityplatform.content.dto.comment.CommentUpdateDto;
import com.communityplatform.content.entity.CommentEntity;
import com.communityplatform.content.entity.PostEntity;
import com.communityplatform.content.exception.UnauthorizedOperationException;
import com.communityplatform.content.mapper.CommentMapper;
import com.communityplatform.content.repository.CommentRepository;
import com.communityplatform.content.repository.LikeRepository;
import com.communityplatform.content.repository.PostRepository;
import com.communityplatform.content.service.PostService;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private PostService postService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void createCommentIncrementsPostCount() {
        CommentCreateDto request = CommentCreateDto.builder()
                .postId(1L)
                .userId(10L)
                .content("Nice")
                .build();

        CommentEntity entity = CommentEntity.builder()
                .postId(1L)
                .userId(10L)
                .content("Nice")
                .build();

        CommentEntity saved = CommentEntity.builder()
                .id(1L)
                .postId(1L)
                .userId(10L)
                .content("Nice")
                .build();

        CommentResponseDto response = CommentResponseDto.builder()
                .id(1L)
                .postId(1L)
                .userId(10L)
                .content("Nice")
                .build();

        when(postRepository.findByIdAndActive(1L)).thenReturn(Optional.of(PostEntity.builder().id(1L).build()));
        when(commentMapper.toEntity(request)).thenReturn(entity);
        when(commentRepository.save(entity)).thenReturn(saved);
        when(commentMapper.toResponseDto(saved)).thenReturn(response);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", "User", "One", "pic")));

        CommentResponseDto result = commentService.createComment(request);

        assertThat(result.getUsername()).isEqualTo("user");
        verify(postService).incrementCommentCount(1L);
    }

    @Test
    void getCommentByIdSetsLikedFlag() {
        CommentEntity entity = CommentEntity.builder()
                .id(2L)
                .postId(1L)
                .userId(10L)
                .content("Comment")
                .build();

        CommentResponseDto response = CommentResponseDto.builder()
                .id(2L)
                .postId(1L)
                .userId(10L)
                .content("Comment")
                .build();

        when(commentRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(commentMapper.toResponseDto(entity)).thenReturn(response);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", null, null, null)));
        when(likeRepository.existsByUserIdAndCommentId(5L, 2L)).thenReturn(true);

        CommentResponseDto result = commentService.getCommentById(2L, 5L);

        assertThat(result.getLikedByCurrentUser()).isTrue();
        assertThat(result.getUsername()).isEqualTo("user");
    }

    @Test
    void updateCommentRequiresOwnership() {
        CommentEntity entity = CommentEntity.builder()
                .id(3L)
                .postId(1L)
                .userId(10L)
                .content("Old")
                .build();

        when(commentRepository.findById(3L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> commentService.updateComment(3L, CommentUpdateDto.builder().content("New").build(), 11L))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void deleteCommentSoftDeletes() {
        CommentEntity entity = CommentEntity.builder()
                .id(4L)
                .postId(1L)
                .userId(10L)
                .content("Comment")
                .build();

        when(commentRepository.findById(4L)).thenReturn(Optional.of(entity));

        commentService.deleteComment(4L, 10L);

        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
        verify(postService).decrementCommentCount(1L);
    }

    @Test
    void getCommentsByPostMapsResults() {
        CommentEntity entity = CommentEntity.builder().id(5L).postId(1L).userId(10L).content("One").build();
        CommentResponseDto response = CommentResponseDto.builder().id(5L).postId(1L).userId(10L).content("One").build();

        when(commentRepository.findByPostIdAndActive(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(commentMapper.toResponseDto(entity)).thenReturn(response);
        when(userServiceClient.getUserById(10L)).thenReturn(Optional.empty());

        var page = commentService.getCommentsByPostId(1L, null, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void getTopLevelCommentsMapsResults() {
        CommentEntity entity = CommentEntity.builder().id(6L).postId(1L).userId(10L).content("Top").build();
        CommentResponseDto response = CommentResponseDto.builder().id(6L).postId(1L).userId(10L).content("Top").build();

        when(commentRepository.findTopLevelComments(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(commentMapper.toResponseDto(entity)).thenReturn(response);
        when(userServiceClient.getUserById(10L)).thenReturn(Optional.empty());

        var page = commentService.getTopLevelComments(1L, null, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void getRepliesMapsResults() {
        CommentEntity entity = CommentEntity.builder().id(7L).postId(1L).userId(10L).content("Reply").build();
        CommentResponseDto response = CommentResponseDto.builder().id(7L).postId(1L).userId(10L).content("Reply").build();

        when(commentRepository.findRepliesByParentId(2L)).thenReturn(List.of(entity));
        when(commentMapper.toResponseDto(entity)).thenReturn(response);
        when(userServiceClient.getUserById(10L)).thenReturn(Optional.empty());

        List<CommentResponseDto> result = commentService.getReplies(2L, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void incrementAndDecrementLikeCount() {
        CommentEntity entity = CommentEntity.builder()
                .id(8L)
                .likeCount(1)
                .build();

        when(commentRepository.findById(8L)).thenReturn(Optional.of(entity));

        commentService.incrementLikeCount(8L);
        commentService.decrementLikeCount(8L);

        verify(commentRepository, org.mockito.Mockito.times(2)).save(entity);
    }
}
