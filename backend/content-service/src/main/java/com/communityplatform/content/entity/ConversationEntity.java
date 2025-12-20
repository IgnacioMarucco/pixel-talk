package com.communityplatform.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Conversation entity - represents a chat conversation between users.
 * 
 * Features:
 * - Supports 1-on-1 and group conversations
 * - Stores participant user IDs
 * - Tracks last message info for performance
 * - Timestamps via BaseEntity
 */
@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_participant_ids", columnList = "participant_ids"),
        @Index(name = "idx_last_message_at", columnList = "last_message_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class ConversationEntity extends BaseEntity {

    /**
     * Comma-separated list of participant user IDs.
     * Example: "1,5,10" for a conversation between users 1, 5, and 10.
     * 
     * Note: In production, consider using a separate participant table for better
     * querying.
     * For MVP, this approach is simpler.
     */
    @Column(name = "participant_ids", nullable = false)
    private String participantIds;

    /**
     * Optional conversation name (for group chats).
     */
    @Column(name = "name")
    private String name;

    /**
     * Whether this is a group conversation (3+ participants).
     */
    @Column(name = "is_group", nullable = false)
    private Boolean isGroup = false;

    /**
     * Cached last message content for preview.
     */
    @Column(name = "last_message_content", columnDefinition = "TEXT")
    private String lastMessageContent;

    /**
     * Cached timestamp of last message.
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * User ID who sent the last message.
     */
    @Column(name = "last_message_sender_id")
    private Long lastMessageSenderId;
}
