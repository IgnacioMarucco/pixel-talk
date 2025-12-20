package com.communityplatform.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Content Service Application - Handles posts, comments, likes, media,
 * notifications, and chat.
 * 
 * Features:
 * - Posts and comments management
 * - Likes and reactions
 * - Media upload/download (MinIO)
 * - Real-time notifications (WebSocket)
 * - Chat conversations and messages
 * - Redis caching for performance
 */
@SpringBootApplication
@EnableJpaAuditing
public class ContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }

}
