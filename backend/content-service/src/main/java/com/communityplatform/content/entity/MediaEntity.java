package com.communityplatform.content.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Media entity - represents uploaded media files (images, videos).
 * 
 * Features:
 * - Metadata for files stored in MinIO
 * - File type, size, and original filename
 * - Reference to uploader user
 * - MinIO object path/key
 * - Timestamps via BaseEntity
 */
@Entity
@Table(name = "media", indexes = {
        @Index(name = "idx_uploader_id", columnList = "uploader_user_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class MediaEntity extends BaseEntity {

    /**
     * Original filename uploaded by user.
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * Stored filename/key in MinIO (usually a UUID-based name).
     */
    @Column(name = "stored_filename", nullable = false, unique = true)
    private String storedFilename;

    /**
     * File MIME type (e.g., image/jpeg, video/mp4).
     */
    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    /**
     * File size in bytes.
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * MinIO bucket name where file is stored.
     */
    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    /**
     * User ID who uploaded the media (references user-service).
     */
    @Column(name = "uploader_user_id", nullable = false)
    private Long uploaderUserId;

    /**
     * Full MinIO URL for accessing the file.
     * Example: http://localhost:9000/content-media/uuid-filename.jpg
     */
    @Column(name = "url", nullable = false)
    private String url;
}
