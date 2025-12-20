package com.communityplatform.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Comment entity - represents a comment on a post.
 * 
 * Features:
 * - Belongs to a post
 * - Author reference (userId from user-service)
 * - Text content
 * - Optional parent comment for nested/threaded comments
 * - Timestamps via BaseEntity
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_post_id", columnList = "post_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_parent_id", columnList = "parent_comment_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class CommentEntity extends BaseEntity {

    /**
     * Post ID this comment belongs to.
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * User ID of the comment author (references user-service).
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Comment content/text.
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Parent comment ID for nested comments (optional).
     * Null for top-level comments.
     */
    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    /**
     * Cached like count for this comment.
     */
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
}
