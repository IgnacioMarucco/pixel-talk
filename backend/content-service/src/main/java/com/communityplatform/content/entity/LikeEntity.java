package com.communityplatform.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Like entity - represents a like on a post or comment.
 * 
 * Features:
 * - User can like a post OR a comment
 * - Unique constraint prevents duplicate likes
 * - Supports both post likes and comment likes
 * - Timestamps via BaseEntity
 */
@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_post", columnNames = { "user_id", "post_id" }),
        @UniqueConstraint(name = "uk_user_comment", columnNames = { "user_id", "comment_id" })
}, indexes = {
        @Index(name = "idx_post_id", columnList = "post_id"),
        @Index(name = "idx_comment_id", columnList = "comment_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class LikeEntity extends BaseEntity {

    /**
     * User ID who gave the like (references user-service).
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Post ID being liked (nullable, mutually exclusive with commentId).
     */
    @Column(name = "post_id")
    private Long postId;

    /**
     * Comment ID being liked (nullable, mutually exclusive with postId).
     */
    @Column(name = "comment_id")
    private Long commentId;

    /**
     * Validates that either postId or commentId is set, but not both.
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        if ((postId == null && commentId == null) || (postId != null && commentId != null)) {
            throw new IllegalStateException("Like must reference either a post or a comment, but not both");
        }
    }
}
