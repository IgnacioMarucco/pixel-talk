package com.communityplatform.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Post entity - represents a user post/publication.
 * 
 * Features:
 * - Author reference (userId from user-service)
 * - Title shown in feeds
 * - Text content
 * - Optional media URLs (stored in MinIO)
 * - Soft delete support
 * - Timestamps via BaseEntity
 */
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_deleted_at", columnList = "deleted_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class PostEntity extends BaseEntity {

    /**
     * User ID of the post author (references user-service).
     * Foreign key managed by application logic, not JPA.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Post title (short summary shown in feeds).
     */
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    /**
     * Post content/text.
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Comma-separated list of media URLs.
     * Example: "image1.jpg,image2.png"
     * 
     * In production, consider using @ElementCollection or separate Media table for
     * better normalization.
     */
    @Column(name = "media_urls", columnDefinition = "TEXT")
    private String mediaUrls;

    /**
     * Cached like count for performance.
     * Updated via service layer when likes change.
     */
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    /**
     * Cached comment count for performance.
     * Updated via service layer when comments change.
     */
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;
}
