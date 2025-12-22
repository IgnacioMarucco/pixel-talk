package com.communityplatform.content.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.communityplatform.content.dto.post.PostCreateDto;
import com.communityplatform.content.dto.post.PostResponseDto;
import com.communityplatform.content.dto.post.PostSummaryDto;
import com.communityplatform.content.dto.post.PostUpdateDto;

/**
 * Service interface for Post operations.
 */
public interface PostService {

    /**
     * Create a new post.
     */
    PostResponseDto createPost(PostCreateDto dto);

    /**
     * Get post by ID.
     */
    PostResponseDto getPostById(Long postId, Long currentUserId);

    /**
     * Update post.
     */
    PostResponseDto updatePost(Long postId, PostUpdateDto dto, Long currentUserId);

    /**
     * Delete post (soft delete).
     */
    void deletePost(Long postId, Long currentUserId);

    /**
     * Get all posts (feed).
     */
    Page<PostSummaryDto> getAllPosts(Long currentUserId, Pageable pageable);

    /**
     * Get posts from users the current user follows (excluding own posts).
     */
    Page<PostSummaryDto> getFollowingPosts(Long userId, Pageable pageable);

    /**
     * Get feed posts for current user (own posts + following).
     */
    Page<PostSummaryDto> getFeedPosts(Long userId, Pageable pageable);

    /**
     * Get posts by user.
     */
    Page<PostSummaryDto> getPostsByUserId(Long userId, Long currentUserId, Pageable pageable);

    /**
     * Search posts by content.
     */
    Page<PostSummaryDto> searchPosts(String searchTerm, Long currentUserId, Pageable pageable);

    /**
     * Get trending posts.
     */
    Page<PostSummaryDto> getTrendingPosts(Long currentUserId, Pageable pageable);

    /**
     * Increment like count.
     */
    void incrementLikeCount(Long postId);

    /**
     * Decrement like count.
     */
    void decrementLikeCount(Long postId);

    /**
     * Increment comment count.
     */
    void incrementCommentCount(Long postId);

    /**
     * Decrement comment count.
     */
    void decrementCommentCount(Long postId);
}
