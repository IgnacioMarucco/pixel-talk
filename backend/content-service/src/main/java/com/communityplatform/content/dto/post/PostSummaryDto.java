package com.communityplatform.content.dto.post;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for post summaries in lists.
 * Used for feed views where full details aren't needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDto {

    /**
     * Post ID.
     */
    private Long id;

    /**
     * Author user ID.
     */
    private Long userId;

    /**
     * Author username.
     */
    private String username;

    /**
     * Author profile picture URL.
     */
    private String profilePictureUrl;

    /**
     * Post title.
     */
    private String title;

    /**
     * Like count.
     */
    private Integer likeCount;

    /**
     * Comment count.
     */
    private Integer commentCount;

    /**
     * Whether the current user has liked this post.
     */
    private Boolean likedByCurrentUser;

    /**
     * Post creation timestamp.
     */
    private LocalDateTime createdAt;
}
