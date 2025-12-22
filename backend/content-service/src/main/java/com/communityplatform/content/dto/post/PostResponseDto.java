package com.communityplatform.content.dto.post;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for post response with full details.
 * Includes author info, content, counts, and timestamps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {

    /**
     * Post ID.
     */
    private Long id;

    /**
     * Author user ID.
     */
    private Long userId;

    /**
     * Author username (fetched from user-service).
     * In MVP, this might be null and fetched client-side.
     */
    private String username;

    /**
     * Author profile picture URL.
     */
    private String profilePictureUrl;

    /**
     * Author full name (optional).
     */
    private String authorFullName;

    /**
     * Post title.
     */
    private String title;

    /**
     * Post content.
     */
    private String content;

    /**
     * Comma-separated media URLs.
     */
    private String mediaUrls;

    /**
     * Number of likes on this post.
     */
    private Integer likeCount;

    /**
     * Number of comments on this post.
     */
    private Integer commentCount;

    /**
     * Whether the current user has liked this post.
     * Set by service layer based on request context.
     */
    private Boolean likedByCurrentUser;

    /**
     * Post creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updatedAt;
}
