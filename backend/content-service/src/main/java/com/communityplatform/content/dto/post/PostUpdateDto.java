package com.communityplatform.content.dto.post;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing post.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDto {

    /**
     * Updated post title.
     */
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    /**
     * Updated post content.
     */
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    private String content;

    /**
     * Updated media URLs.
     */
    @Size(max = 1000, message = "Media URLs cannot exceed 1000 characters")
    private String mediaUrls;
}
