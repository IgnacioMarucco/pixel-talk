package com.communityplatform.content.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.communityplatform.content.dto.post.PostCreateDto;
import com.communityplatform.content.dto.post.PostResponseDto;
import com.communityplatform.content.dto.post.PostSummaryDto;
import com.communityplatform.content.dto.post.PostUpdateDto;
import com.communityplatform.content.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Post management.
 */
@RestController
@RequestMapping("${api.base-path}/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Posts", description = "Post management endpoints")
public class PostController {

        private final PostService postService;

        @Operation(summary = "Create a new post")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Post created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request")
        })
        @PostMapping
        public ResponseEntity<PostResponseDto> createPost(
                        @RequestHeader("X-User-Id") Long userId,
                        @Valid @RequestBody PostCreateDto dto) {
                log.info("Creating post for user: {}", userId);
                dto.setUserId(userId); // Set from Gateway header
                PostResponseDto response = postService.createPost(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Get post by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Post retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Post not found")
        })
        @GetMapping("/{postId}")
        public ResponseEntity<PostResponseDto> getPost(
                        @PathVariable Long postId,
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
                log.info("Getting post: {}", postId);
                PostResponseDto response = postService.getPostById(postId, currentUserId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Update post")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Post updated successfully"),
                        @ApiResponse(responseCode = "403", description = "Unauthorized to update"),
                        @ApiResponse(responseCode = "404", description = "Post not found")
        })
        @PutMapping("/{postId}")
        public ResponseEntity<PostResponseDto> updatePost(
                        @PathVariable Long postId,
                        @RequestHeader("X-User-Id") Long currentUserId,
                        @Valid @RequestBody PostUpdateDto dto) {
                log.info("Updating post: {}", postId);
                PostResponseDto response = postService.updatePost(postId, dto, currentUserId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Delete post")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
                        @ApiResponse(responseCode = "403", description = "Unauthorized to delete"),
                        @ApiResponse(responseCode = "404", description = "Post not found")
        })
        @DeleteMapping("/{postId}")
        public ResponseEntity<Void> deletePost(
                        @PathVariable Long postId,
                        @RequestHeader("X-User-Id") Long currentUserId) {
                log.info("Deleting post: {}", postId);
                postService.deletePost(postId, currentUserId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Get all posts (feed)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
        })
        @GetMapping
        public ResponseEntity<Page<PostSummaryDto>> getAllPosts(
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
                        @PageableDefault(size = 20) Pageable pageable) {
                log.info("Getting all posts, page: {}", pageable.getPageNumber());
                Page<PostSummaryDto> response = postService.getAllPosts(currentUserId, pageable);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get posts from following users")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Following posts retrieved successfully")
        })
        @GetMapping("/following")
        public ResponseEntity<Page<PostSummaryDto>> getFollowingPosts(
                        @RequestHeader("X-User-Id") Long userId,
                        @PageableDefault(size = 20) Pageable pageable) {
                log.info("Getting following posts for user: {}", userId);
                Page<PostSummaryDto> response = postService.getFollowingPosts(userId, pageable);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get feed posts (own + following)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Feed posts retrieved successfully")
        })
        @GetMapping("/feed")
        public ResponseEntity<Page<PostSummaryDto>> getFeedPosts(
                        @RequestHeader("X-User-Id") Long userId,
                        @PageableDefault(size = 20) Pageable pageable) {
                log.info("Getting feed posts for user: {}", userId);
                Page<PostSummaryDto> response = postService.getFeedPosts(userId, pageable);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get trending posts")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Trending posts retrieved successfully")
        })
        @GetMapping("/trending")
        public ResponseEntity<Page<PostSummaryDto>> getTrendingPosts(
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
                        @PageableDefault(size = 20) Pageable pageable) {
                log.info("Getting trending posts");
                Page<PostSummaryDto> response = postService.getTrendingPosts(currentUserId, pageable);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Search posts by title or content")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
        })
        @GetMapping("/search")
        public ResponseEntity<Page<PostSummaryDto>> searchPosts(
                        @RequestParam String q,
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
                        @PageableDefault(size = 20) Pageable pageable) {
                log.info("Searching posts with term: {}", q);
                Page<PostSummaryDto> response = postService.searchPosts(q, currentUserId, pageable);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get posts by user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
        })
        @GetMapping("/user/{userId}")
        public ResponseEntity<Page<PostSummaryDto>> getPostsByUser(
                        @PathVariable Long userId,
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
                        @PageableDefault(size = 20) Pageable pageable) {
                log.info("Getting posts for user: {}", userId);
                Page<PostSummaryDto> response = postService.getPostsByUserId(userId, currentUserId, pageable);
                return ResponseEntity.ok(response);
        }
}
