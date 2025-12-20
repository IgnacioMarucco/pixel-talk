package com.communityplatform.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Notification entity - represents a notification for a user.
 * 
 * Features:
 * - Type of notification (enum)
 * - Read/unread status
 * - Reference to the entity that triggered the notification
 * - Target user ID
 * - Timestamps via BaseEntity
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id_read", columnList = "user_id, is_read"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class NotificationEntity extends BaseEntity {

    /**
     * User ID who receives this notification (references user-service).
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Type of notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    /**
     * ID of the user who triggered this notification (e.g., who liked, commented).
     */
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    /**
     * Reference to the entity that triggered this notification.
     * Could be postId, commentId, etc. depending on the type.
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Optional message or additional context.
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Whether the notification has been read.
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
}
