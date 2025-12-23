package com.communityplatform.content.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.communityplatform.content.dto.post.PostCreateDto;
import com.communityplatform.content.dto.post.PostResponseDto;
import com.communityplatform.content.dto.post.PostSummaryDto;
import com.communityplatform.content.dto.post.PostUpdateDto;
import com.communityplatform.content.entity.PostEntity;
import com.communityplatform.content.exception.UnauthorizedOperationException;
import com.communityplatform.content.mapper.PostMapper;
import com.communityplatform.content.repository.LikeRepository;
import com.communityplatform.content.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void createPostEnrichesAuthor() {
        PostCreateDto request = PostCreateDto.builder()
                .userId(10L)
                .title("Title")
                .content("Content")
                .build();

        PostEntity entity = PostEntity.builder()
                .userId(10L)
                .title("Title")
                .content("Content")
                .build();

        PostEntity saved = PostEntity.builder()
                .id(1L)
                .userId(10L)
                .title("Title")
                .content("Content")
                .build();

        PostResponseDto response = PostResponseDto.builder()
                .id(1L)
                .userId(10L)
                .title("Title")
                .content("Content")
                .build();

        when(postMapper.toEntity(request)).thenReturn(entity);
        when(postRepository.save(entity)).thenReturn(saved);
        when(postMapper.toResponseDto(saved)).thenReturn(response);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", "User", "One", "pic")));

        PostResponseDto result = postService.createPost(request);

        assertThat(result.getUsername()).isEqualTo("user");
        assertThat(result.getLikedByCurrentUser()).isFalse();
    }

    @Test
    void getPostByIdSetsLikedFlag() {
        PostEntity entity = PostEntity.builder()
                .id(2L)
                .userId(10L)
                .title("Title")
                .build();

        PostResponseDto response = PostResponseDto.builder()
                .id(2L)
                .userId(10L)
                .title("Title")
                .build();

        when(postRepository.findByIdAndActive(2L)).thenReturn(Optional.of(entity));
        when(postMapper.toResponseDto(entity)).thenReturn(response);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", null, null, null)));
        when(likeRepository.existsByUserIdAndPostId(5L, 2L)).thenReturn(true);

        PostResponseDto result = postService.getPostById(2L, 5L);

        assertThat(result.getLikedByCurrentUser()).isTrue();
        assertThat(result.getUsername()).isEqualTo("user");
    }

    @Test
    void updatePostRequiresOwnership() {
        PostEntity entity = PostEntity.builder().id(3L).userId(10L).title("Old").build();
        when(postRepository.findByIdAndActive(3L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> postService.updatePost(3L, PostUpdateDto.builder().title("New").build(), 11L))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void deletePostSoftDeletes() {
        PostEntity entity = PostEntity.builder()
                .id(4L)
                .userId(10L)
                .title("Title")
                .build();

        when(postRepository.findByIdAndActive(4L)).thenReturn(Optional.of(entity));

        postService.deletePost(4L, 10L);

        ArgumentCaptor<PostEntity> captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void getAllPostsEnrichesSummaries() {
        PostEntity entity = PostEntity.builder().id(5L).userId(10L).title("Title").build();
        PostSummaryDto summary = PostSummaryDto.builder().id(5L).userId(10L).title("Title").build();

        when(postRepository.findAllActive(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(postMapper.toSummaryDto(entity)).thenReturn(summary);
        when(userServiceClient.getUserById(10L))
                .thenReturn(Optional.of(new UserProfileDto(10L, "user", null, null, "pic")));
        when(likeRepository.existsByUserIdAndPostId(1L, 5L)).thenReturn(true);

        var page = postService.getAllPosts(1L, PageRequest.of(0, 20));

        assertThat(page.getContent().get(0).getUsername()).isEqualTo("user");
        assertThat(page.getContent().get(0).getLikedByCurrentUser()).isTrue();
    }

    @Test
    void getFollowingPostsReturnsEmptyWhenNoFollowing() {
        when(userServiceClient.getFollowingIds(1L)).thenReturn(List.of());

        var page = postService.getFollowingPosts(1L, PageRequest.of(0, 20));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void getFeedPostsMergesSelf() {
        PostEntity entity = PostEntity.builder().id(6L).userId(1L).title("Feed").build();
        PostSummaryDto summary = PostSummaryDto.builder().id(6L).userId(1L).title("Feed").build();

        when(userServiceClient.getFollowingIds(1L)).thenReturn(List.of(2L));
        when(postRepository.findByUserIdInAndDeletedAtIsNull(eq(List.of(2L, 1L)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(postMapper.toSummaryDto(entity)).thenReturn(summary);
        when(userServiceClient.getUserById(1L))
                .thenReturn(Optional.of(new UserProfileDto(1L, "user", null, null, null)));
        when(likeRepository.existsByUserIdAndPostId(1L, 6L)).thenReturn(false);

        var page = postService.getFeedPosts(1L, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void searchPostsMapsResults() {
        PostEntity entity = PostEntity.builder().id(7L).userId(2L).title("Title").build();
        PostSummaryDto summary = PostSummaryDto.builder().id(7L).userId(2L).title("Title").build();

        when(postRepository.searchByTitleOrContent(eq("term"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(postMapper.toSummaryDto(entity)).thenReturn(summary);
        when(userServiceClient.getUserById(2L)).thenReturn(Optional.empty());

        var page = postService.searchPosts("term", null, PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void updatePostReturnsResponse() {
        PostEntity entity = PostEntity.builder().id(8L).userId(10L).title("Old").build();
        PostResponseDto response = PostResponseDto.builder().id(8L).userId(10L).title("New").build();

        when(postRepository.findByIdAndActive(8L)).thenReturn(Optional.of(entity));
        when(postRepository.save(entity)).thenReturn(entity);
        when(postMapper.toResponseDto(entity)).thenReturn(response);
        when(userServiceClient.getUserById(10L)).thenReturn(Optional.of(new UserProfileDto(10L, "user", null, null, null)));
        when(likeRepository.existsByUserIdAndPostId(10L, 8L)).thenReturn(false);

        PostResponseDto result = postService.updatePost(8L, PostUpdateDto.builder().title("New").build(), 10L);

        assertThat(result.getTitle()).isEqualTo("New");
    }

    @Test
    void incrementAndDecrementCounts() {
        PostEntity entity = PostEntity.builder()
                .id(9L)
                .likeCount(1)
                .commentCount(1)
                .build();

        when(postRepository.findById(9L)).thenReturn(Optional.of(entity));

        postService.incrementLikeCount(9L);
        postService.decrementLikeCount(9L);
        postService.incrementCommentCount(9L);
        postService.decrementCommentCount(9L);

        verify(postRepository, org.mockito.Mockito.times(4)).save(entity);
    }
}
