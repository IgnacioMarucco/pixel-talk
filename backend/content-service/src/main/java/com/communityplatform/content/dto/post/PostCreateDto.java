package com.communityplatform.content.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new post.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDto {

    /**
     * User ID of the post author.
     * Injected from X-User-Id header by the controller (set after validation).
     */
    private Long userId;

    /**
     * Post title.
     */
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    /**
     * Post content/text.
     */
    @NotBlank(message = "Content cannot be blank")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    private String content;

    /**
     * Optional comma-separated media URLs.
     */
    @Size(max = 1000, message = "Media URLs cannot exceed 1000 characters")
    private String mediaUrls;
}
