package com.communityplatform.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Message entity - represents a message in a conversation.
 * 
 * Features:
 * - Belongs to a conversation
 * - Sender user reference
 * - Text content
 * - Read status tracking
 * - Optional media attachment
 * - Timestamps via BaseEntity
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_conversation_id_created", columnList = "conversation_id, created_at"),
        @Index(name = "idx_sender_id", columnList = "sender_user_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class MessageEntity extends BaseEntity {

    /**
     * Conversation ID this message belongs to.
     */
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /**
     * User ID who sent the message (references user-service).
     */
    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    /**
     * Message content/text.
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Optional media attachment URL.
     */
    @Column(name = "media_url")
    private String mediaUrl;

    /**
     * Comma-separated list of user IDs who have read this message.
     * Example: "1,5,10"
     * 
     * Note: In production, consider a separate read_receipts table for better
     * querying.
     * For MVP, this approach is simpler.
     */
    @Column(name = "read_by_user_ids", columnDefinition = "TEXT")
    private String readByUserIds;

    /**
     * Whether the message has been edited.
     */
    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;
}
