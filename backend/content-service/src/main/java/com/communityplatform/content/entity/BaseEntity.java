package com.communityplatform.content.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base entity class that provides common auditing fields for all entities.
 * 
 * This class implements the following patterns:
 * - Automatic timestamp tracking (created_at, updated_at) using JPA Auditing
 * - Soft delete functionality using deleted_at field
 * - Standard ID field with auto-increment strategy
 * 
 * All entities should extend this class to inherit these common fields.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Helper method to check if this entity is active (not soft deleted).
     * 
     * @return true if the entity is active (deletedAt is null), false otherwise
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * Soft delete this entity by setting the deletedAt timestamp.
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
