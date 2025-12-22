package com.communityplatform.content.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.communityplatform.content.dto.comment.CommentCreateDto;
import com.communityplatform.content.dto.comment.CommentResponseDto;
import com.communityplatform.content.dto.comment.CommentUpdateDto;
import com.communityplatform.content.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * REST Controller for Comment management.
 */
@RestController
@RequestMapping("${api.base-path}")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comments", description = "Comment management endpoints")
public class CommentController {

        private final CommentService commentService;

        @Operation(summary = "Create a new comment")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Comment created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request"),
                        @ApiResponse(responseCode = "404", description = "Post not found")
        })
        @PostMapping("/posts/{postId}/comments")
        public ResponseEntity<CommentResponseDto> createComment(
                        @PathVariable Long postId,
                        @RequestHeader("X-User-Id") Long userId,
                        @Valid @RequestBody CommentCreateDto dto) {
                log.info("Creating comment for post: {} by user: {}", postId, userId);
                // Set from path and Gateway header
                dto.setPostId(postId);
                dto.setUserId(userId);
                CommentResponseDto response = commentService.createComment(dto);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Get comment by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        @GetMapping("/comments/{commentId}")
        public ResponseEntity<CommentResponseDto> getComment(
                        @PathVariable Long commentId,
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
                log.info("Getting comment: {}", commentId);
                CommentResponseDto response = commentService.getCommentById(commentId, currentUserId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Delete comment")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
                        @ApiResponse(responseCode = "403", description = "Unauthorized to delete"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        @DeleteMapping("/comments/{commentId}")
        public ResponseEntity<Void> deleteComment(
                        @PathVariable Long commentId,
                        @RequestHeader("X-User-Id") Long currentUserId) {
                log.info("Deleting comment: {}", commentId);
                commentService.deleteComment(commentId, currentUserId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Update comment")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
                        @ApiResponse(responseCode = "403", description = "Unauthorized to update"),
                        @ApiResponse(responseCode = "404", description = "Comment not found")
        })
        @PutMapping("/comments/{commentId}")
        public ResponseEntity<CommentResponseDto> updateComment(
                        @PathVariable Long commentId,
                        @RequestHeader("X-User-Id") Long currentUserId,
                        @Valid @RequestBody CommentUpdateDto dto) {
                log.info("Updating comment: {}", commentId);
                CommentResponseDto response = commentService.updateComment(commentId, dto, currentUserId);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get all comments for a post")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully")
        })
        @GetMapping("/posts/{postId}/comments")
        public ResponseEntity<Page<CommentResponseDto>> getCommentsByPost(
                        @PathVariable Long postId,
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
                        @PageableDefault(size = 20) Pageable pageable) {
                log.info("Getting comments for post: {}", postId);
                Page<CommentResponseDto> response = commentService.getCommentsByPostId(postId, currentUserId, pageable);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get top-level comments for a post")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Top-level comments retrieved successfully")
        })
        @GetMapping("/posts/{postId}/comments/top")
        public ResponseEntity<Page<CommentResponseDto>> getTopLevelComments(
                        @PathVariable Long postId,
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
                        @PageableDefault(size = 20) Pageable pageable) {
                log.info("Getting top-level comments for post: {}", postId);
                Page<CommentResponseDto> response = commentService.getTopLevelComments(postId, currentUserId, pageable);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get replies to a comment")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Replies retrieved successfully")
        })
        @GetMapping("/comments/{commentId}/replies")
        public ResponseEntity<List<CommentResponseDto>> getReplies(
                        @PathVariable Long commentId,
                        @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
                log.info("Getting replies for comment: {}", commentId);
                List<CommentResponseDto> response = commentService.getReplies(commentId, currentUserId);
                return ResponseEntity.ok(response);
        }
}
