package com.communityplatform.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.communityplatform.content.entity.PostEntity;

/**
 * Repository for Post entity operations.
 */
@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    /**
     * Find all active (not deleted) posts by user ID.
     *
     * @param userId   User ID
     * @param pageable Pagination info
     * @return Page of posts
     */
    @Query("SELECT p FROM PostEntity p WHERE p.userId = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<PostEntity> findByUserIdAndDeletedAtIsNull(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find all active posts by a list of user IDs.
     *
     * @param userIds  User IDs
     * @param pageable Pagination info
     * @return Page of posts
     */
    @Query("SELECT p FROM PostEntity p WHERE p.userId IN :userIds AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<PostEntity> findByUserIdInAndDeletedAtIsNull(@Param("userIds") List<Long> userIds, Pageable pageable);

    /**
     * Find all active posts ordered by creation date.
     *
     * @param pageable Pagination info
     * @return Page of posts
     */
    @Query("SELECT p FROM PostEntity p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<PostEntity> findAllActive(Pageable pageable);

    /**
     * Search posts by content (case-insensitive).
     *
     * @param searchTerm Search term
     * @param pageable   Pagination info
     * @return Page of matching posts
     */
    @Query("SELECT p FROM PostEntity p WHERE p.deletedAt IS NULL AND LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY p.createdAt DESC")
    Page<PostEntity> searchByContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find single post by ID if active.
     *
     * @param id Post ID
     * @return Optional post
     */
    @Query("SELECT p FROM PostEntity p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<PostEntity> findByIdAndActive(@Param("id") Long id);

    /**
     * Find trending posts (high engagement) in last N days.
     * Ordered by like count + comment count.
     *
     * @param pageable Pagination info
     * @return Page of trending posts
     */
    @Query("SELECT p FROM PostEntity p WHERE p.deletedAt IS NULL ORDER BY (p.likeCount + p.commentCount) DESC, p.createdAt DESC")
    Page<PostEntity> findTrendingPosts(Pageable pageable);

    /**
     * Count active posts by user.
     *
     * @param userId User ID
     * @return Count of posts
     */
    @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.userId = :userId AND p.deletedAt IS NULL")
    Long countByUserId(@Param("userId") Long userId);
}
