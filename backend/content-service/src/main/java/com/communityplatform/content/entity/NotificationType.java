package com.communityplatform.content.entity;

/**
 * Notification type enum - defines different types of notifications.
 */
public enum NotificationType {
    /**
     * Someone liked your post.
     */
    POST_LIKED,

    /**
     * Someone commented on your post.
     */
    POST_COMMENTED,

    /**
     * Someone liked your comment.
     */
    COMMENT_LIKED,

    /**
     * Someone replied to your comment.
     */
    COMMENT_REPLIED,

    /**
     * Someone started following you.
     */
    NEW_FOLLOWER,

    /**
     * Someone mentioned you in a post or comment.
     */
    MENTIONED,

    /**
     * New message in a conversation.
     */
    NEW_MESSAGE
}
