package com.communityplatform.content.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.communityplatform.content.dto.post.PostCreateDto;
import com.communityplatform.content.dto.post.PostResponseDto;
import com.communityplatform.content.dto.post.PostSummaryDto;
import com.communityplatform.content.dto.post.PostUpdateDto;
import com.communityplatform.content.entity.PostEntity;
import com.communityplatform.content.exception.PostNotFoundException;
import com.communityplatform.content.exception.UnauthorizedOperationException;
import com.communityplatform.content.mapper.PostMapper;
import com.communityplatform.content.repository.PostRepository;
import com.communityplatform.content.service.PostService;
import com.communityplatform.content.UserProfileDto;
import com.communityplatform.content.UserServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of PostService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserServiceClient userServiceClient;
    private final com.communityplatform.content.repository.LikeRepository likeRepository;

    @Override
    public PostResponseDto createPost(PostCreateDto dto) {
        log.debug("Creating post for user: {}", dto.getUserId());

        PostEntity entity = postMapper.toEntity(dto);
        entity.setLikeCount(0);
        entity.setCommentCount(0);

        PostEntity saved = postRepository.save(entity);
        log.info("Post created with id: {}", saved.getId());

        PostResponseDto response = postMapper.toResponseDto(saved);
        enrichPostAuthor(response);
        response.setLikedByCurrentUser(false);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long postId, Long currentUserId) {
        log.debug("Getting post by id: {}", postId);

        PostEntity entity = postRepository.findByIdAndActive(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        PostResponseDto dto = postMapper.toResponseDto(entity);
        enrichPostAuthor(dto);
        
        // Set liked by current user
        if (currentUserId != null) {
            boolean liked = likeRepository.existsByUserIdAndPostId(currentUserId, postId);
            dto.setLikedByCurrentUser(liked);
        }
        
        return dto;
    }

    @Override
    public PostResponseDto updatePost(Long postId, PostUpdateDto dto, Long currentUserId) {
        log.debug("Updating post: {}", postId);

        PostEntity entity = postRepository.findByIdAndActive(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Check authorization
        if (!entity.getUserId().equals(currentUserId)) {
            throw new UnauthorizedOperationException("You can only edit your own posts");
        }

        // Update only provided fields
        postMapper.updateEntityFromDto(dto, entity);

        PostEntity updated = postRepository.save(entity);
        log.info("Post updated: {}", postId);

        PostResponseDto response = postMapper.toResponseDto(updated);
        enrichPostAuthor(response);
        
        // Set liked by current user
        if (currentUserId != null) {
            boolean liked = likeRepository.existsByUserIdAndPostId(currentUserId, postId);
            response.setLikedByCurrentUser(liked);
        }
        
        return response;
    }

    @Override
    public void deletePost(Long postId, Long currentUserId) {
        log.debug("Deleting post: {}", postId);

        PostEntity entity = postRepository.findByIdAndActive(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // Check authorization
        if (!entity.getUserId().equals(currentUserId)) {
            throw new UnauthorizedOperationException("You can only delete your own posts");
        }

        entity.softDelete();
        postRepository.save(entity);
        log.info("Post soft deleted: {}", postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getAllPosts(Long currentUserId, Pageable pageable) {
        log.debug("Getting all posts, page: {}", pageable.getPageNumber());
        return postRepository.findAllActive(pageable)
                .map(postMapper::toSummaryDto)
                .map(dto -> enrichPostSummaryWithLike(dto, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getPostsByUserId(Long userId, Long currentUserId, Pageable pageable) {
        log.debug("Getting posts for user: {}", userId);
        return postRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(postMapper::toSummaryDto)
                .map(dto -> enrichPostSummaryWithLike(dto, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getFollowingPosts(Long userId, Pageable pageable) {
        log.debug("Getting following posts for user: {}", userId);
        List<Long> followingIds = userServiceClient.getFollowingIds(userId);
        if (followingIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return postRepository.findByUserIdInAndDeletedAtIsNull(followingIds, pageable)
                .map(postMapper::toSummaryDto)
                .map(dto -> enrichPostSummaryWithLike(dto, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getFeedPosts(Long userId, Pageable pageable) {
        log.debug("Getting feed posts for user: {}", userId);
        List<Long> followingIds = userServiceClient.getFollowingIds(userId);
        if (userId != null) {
            LinkedHashSet<Long> merged = new LinkedHashSet<>(followingIds);
            merged.add(userId);
            followingIds = new ArrayList<>(merged);
        }
        if (followingIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return postRepository.findByUserIdInAndDeletedAtIsNull(followingIds, pageable)
                .map(postMapper::toSummaryDto)
                .map(dto -> enrichPostSummaryWithLike(dto, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostSummaryDto> searchPosts(String searchTerm, Long currentUserId, Pageable pageable) {
        log.debug("Searching posts with term: {}", searchTerm);
        return postRepository.searchByContent(searchTerm, pageable)
                .map(postMapper::toSummaryDto)
                .map(dto -> enrichPostSummaryWithLike(dto, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostSummaryDto> getTrendingPosts(Long currentUserId, Pageable pageable) {
        log.debug("Getting trending posts");
        return postRepository.findTrendingPosts(pageable)
                .map(postMapper::toSummaryDto)
                .map(dto -> enrichPostSummaryWithLike(dto, currentUserId));
    }

    @Override
    public void incrementLikeCount(Long postId) {
        PostEntity entity = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        entity.setLikeCount(entity.getLikeCount() + 1);
        postRepository.save(entity);
        log.debug("Incremented like count for post: {}", postId);
    }

    @Override
    public void decrementLikeCount(Long postId) {
        PostEntity entity = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        entity.setLikeCount(Math.max(0, entity.getLikeCount() - 1));
        postRepository.save(entity);
        log.debug("Decremented like count for post: {}", postId);
    }

    @Override
    public void incrementCommentCount(Long postId) {
        PostEntity entity = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        entity.setCommentCount(entity.getCommentCount() + 1);
        postRepository.save(entity);
        log.debug("Incremented comment count for post: {}", postId);
    }

    @Override
    public void decrementCommentCount(Long postId) {
        PostEntity entity = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        entity.setCommentCount(Math.max(0, entity.getCommentCount() - 1));
        postRepository.save(entity);
        log.debug("Decremented comment count for post: {}", postId);
    }

    private void enrichPostAuthor(PostResponseDto dto) {
        if (dto == null || dto.getUserId() == null) {
            return;
        }
        userServiceClient.getUserById(dto.getUserId())
                .ifPresent(profile -> {
                    dto.setUsername(profile.getUsername());
                    dto.setAuthorFullName(profile.toFullName());
                    dto.setProfilePictureUrl(profile.getProfilePictureUrl());
                });
    }

    private PostSummaryDto enrichPostSummary(PostSummaryDto dto) {
        if (dto == null || dto.getUserId() == null) {
            return dto;
        }
        userServiceClient.getUserById(dto.getUserId())
                .ifPresent(profile -> {
                    dto.setUsername(profile.getUsername());
                    dto.setProfilePictureUrl(profile.getProfilePictureUrl());
                });
        return dto;
    }
    
    private PostSummaryDto enrichPostSummaryWithLike(PostSummaryDto dto, Long currentUserId) {
        enrichPostSummary(dto);
        if (dto != null && currentUserId != null) {
            boolean liked = likeRepository.existsByUserIdAndPostId(currentUserId, dto.getId());
            dto.setLikedByCurrentUser(liked);
        }
        return dto;
    }
}
