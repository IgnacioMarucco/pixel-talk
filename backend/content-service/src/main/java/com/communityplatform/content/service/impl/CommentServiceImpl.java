package com.communityplatform.content.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.communityplatform.content.dto.comment.CommentCreateDto;
import com.communityplatform.content.dto.comment.CommentResponseDto;
import com.communityplatform.content.dto.comment.CommentUpdateDto;
import com.communityplatform.content.entity.CommentEntity;
import com.communityplatform.content.exception.CommentNotFoundException;
import com.communityplatform.content.exception.PostNotFoundException;
import com.communityplatform.content.exception.UnauthorizedOperationException;
import com.communityplatform.content.mapper.CommentMapper;
import com.communityplatform.content.repository.CommentRepository;
import com.communityplatform.content.repository.PostRepository;
import com.communityplatform.content.service.CommentService;
import com.communityplatform.content.service.PostService;
import com.communityplatform.content.UserServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CommentService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final PostService postService;
    private final UserServiceClient userServiceClient;
    private final com.communityplatform.content.repository.LikeRepository likeRepository;

    @Override
    public CommentResponseDto createComment(CommentCreateDto dto) {
        log.debug("Creating comment for post: {}", dto.getPostId());

        // Verify post exists
        postRepository.findByIdAndActive(dto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(dto.getPostId()));

        // If replying to a comment, verify parent exists
        if (dto.getParentCommentId() != null) {
            commentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException(dto.getParentCommentId()));
        }

        CommentEntity entity = commentMapper.toEntity(dto);
        entity.setLikeCount(0);

        CommentEntity saved = commentRepository.save(entity);

        // Increment post comment count
        postService.incrementCommentCount(dto.getPostId());

        log.info("Comment created with id: {}", saved.getId());
        CommentResponseDto response = commentMapper.toResponseDto(saved);
        enrichCommentAuthor(response);
        response.setLikedByCurrentUser(false);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto getCommentById(Long commentId, Long currentUserId) {
        log.debug("Getting comment by id: {}", commentId);

        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!entity.isActive()) {
            throw new CommentNotFoundException(commentId);
        }

        CommentResponseDto response = commentMapper.toResponseDto(entity);
        enrichCommentAuthor(response);
        
        // Set liked by current user
        if (currentUserId != null) {
            boolean liked = likeRepository.existsByUserIdAndCommentId(currentUserId, commentId);
            response.setLikedByCurrentUser(liked);
        }
        
        return response;
    }

    @Override
    public CommentResponseDto updateComment(Long commentId, CommentUpdateDto dto, Long currentUserId) {
        log.debug("Updating comment: {}", commentId);

        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!entity.isActive()) {
            throw new CommentNotFoundException(commentId);
        }

        if (!entity.getUserId().equals(currentUserId)) {
            throw new UnauthorizedOperationException("You can only edit your own comments");
        }

        entity.setContent(dto.getContent());

        CommentEntity updated = commentRepository.save(entity);
        log.info("Comment updated: {}", commentId);

        CommentResponseDto response = commentMapper.toResponseDto(updated);
        enrichCommentAuthor(response);
        
        // Set liked by current user
        if (currentUserId != null) {
            boolean liked = likeRepository.existsByUserIdAndCommentId(currentUserId, commentId);
            response.setLikedByCurrentUser(liked);
        }
        
        return response;
    }

    @Override
    public void deleteComment(Long commentId, Long currentUserId) {
        log.debug("Deleting comment: {}", commentId);

        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!entity.isActive()) {
            throw new CommentNotFoundException(commentId);
        }

        // Check authorization
        if (!entity.getUserId().equals(currentUserId)) {
            throw new UnauthorizedOperationException("You can only delete your own comments");
        }

        entity.softDelete();
        commentRepository.save(entity);

        // Decrement post comment count
        postService.decrementCommentCount(entity.getPostId());

        log.info("Comment soft deleted: {}", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByPostId(Long postId, Long currentUserId, Pageable pageable) {
        log.debug("Getting comments for post: {}", postId);
        return commentRepository.findByPostIdAndActive(postId, pageable)
                .map(commentMapper::toResponseDto)
                .map(dto -> enrichCommentWithLike(dto, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getTopLevelComments(Long postId, Long currentUserId, Pageable pageable) {
        log.debug("Getting top-level comments for post: {}", postId);
        return commentRepository.findTopLevelComments(postId, pageable)
                .map(commentMapper::toResponseDto)
                .map(dto -> enrichCommentWithLike(dto, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getReplies(Long parentCommentId, Long currentUserId) {
        log.debug("Getting replies for comment: {}", parentCommentId);
        return commentRepository.findRepliesByParentId(parentCommentId)
                .stream()
                .map(commentMapper::toResponseDto)
                .map(dto -> enrichCommentWithLike(dto, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public void incrementLikeCount(Long commentId) {
        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        entity.setLikeCount(entity.getLikeCount() + 1);
        commentRepository.save(entity);
        log.debug("Incremented like count for comment: {}", commentId);
    }

    @Override
    public void decrementLikeCount(Long commentId) {
        CommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        entity.setLikeCount(Math.max(0, entity.getLikeCount() - 1));
        commentRepository.save(entity);
        log.debug("Decremented like count for comment: {}", commentId);
    }

    private CommentResponseDto enrichCommentAuthor(CommentResponseDto dto) {
        if (dto == null || dto.getUserId() == null) {
            return dto;
        }
        userServiceClient.getUserById(dto.getUserId())
                .ifPresent(profile -> {
                    dto.setUsername(profile.getUsername());
                    dto.setAuthorFullName(profile.toFullName());
                });
        return dto;
    }
    
    private CommentResponseDto enrichCommentWithLike(CommentResponseDto dto, Long currentUserId) {
        enrichCommentAuthor(dto);
        if (dto != null && currentUserId != null) {
            boolean liked = likeRepository.existsByUserIdAndCommentId(currentUserId, dto.getId());
            dto.setLikedByCurrentUser(liked);
        }
        return dto;
    }
}
