package com.agora.agoracampus.feed.comment.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.feed.comment.dto.request.CreateCommentRequest;
import com.agora.agoracampus.feed.comment.dto.response.CommentResponse;
import com.agora.agoracampus.feed.comment.mapper.CommentMapper;
import com.agora.agoracampus.feed.comment.model.Comment;
import com.agora.agoracampus.feed.comment.repository.CommentRepository;
import com.agora.agoracampus.feed.model.FeedActorRole;
import com.agora.agoracampus.feed.post.repository.PostRepository;
import com.agora.agoracampus.feed.service.FeedPermissionService;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AppUserRepository appUserRepository;
    private final CommentMapper commentMapper;
    private final FeedPermissionService feedPermissionService;

    public List<CommentResponse> listByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post not found.");
        }
        return commentRepository.findByPost_IdOrderByCreatedAtAsc(postId).stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse create(Long postId, Long actingUserId, CreateCommentRequest request) {
        var actor = feedPermissionService.resolveActor(actingUserId);
        feedPermissionService.requireUser(
                actor,
                actingUserId,
                request.authorUserId(),
                "Only individual or organization users can comment as themselves."
        );
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found."));
        var author = appUserRepository.findById(request.authorUserId())
                .orElseThrow(() -> new NotFoundException("User " + request.authorUserId() + " was not found."));

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(request.content())
                .build();

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void delete(Long commentId, Long actingUserId) {
        var actor = feedPermissionService.resolveActor(actingUserId);
        Comment existing = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found."));
        if (actor.role() != FeedActorRole.ADMIN && !actingUserId.equals(existing.getAuthor().getId())) {
            throw new BadRequestException("Only the comment author can delete this comment.");
        }
        commentRepository.delete(existing);
    }
}
