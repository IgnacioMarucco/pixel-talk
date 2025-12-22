package com.communityplatform.content.dto.comment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for comment response with full details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {

    /**
     * Comment ID.
     */
    private Long id;

    /**
     * Post ID.
     */
    private Long postId;

    /**
     * Author user ID.
     */
    private Long userId;

    /**
     * Author username.
     */
    private String username;

    /**
     * Author full name.
     */
    private String authorFullName;

    /**
     * Author profile picture URL.
     */
    private String profilePictureUrl;

    /**
     * Comment content.
     */
    private String content;

    /**
     * Parent comment ID (for nested comments).
     */
    private Long parentCommentId;

    /**
     * Like count.
     */
    private Integer likeCount;

    /**
     * Whether current user liked this comment.
     */
    private Boolean likedByCurrentUser;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Update timestamp.
     */
    private LocalDateTime updatedAt;
}
