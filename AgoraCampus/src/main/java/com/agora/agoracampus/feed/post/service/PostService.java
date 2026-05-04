package com.agora.agoracampus.feed.post.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.feed.model.FeedActorRole;
import com.agora.agoracampus.feed.post.dto.request.CreatePostRequest;
import com.agora.agoracampus.feed.post.dto.request.UpdatePostRequest;
import com.agora.agoracampus.feed.post.dto.response.PostResponse;
import com.agora.agoracampus.feed.post.mapper.PostMapper;
import com.agora.agoracampus.feed.post.model.Post;
import com.agora.agoracampus.feed.post.repository.PostRepository;
import com.agora.agoracampus.feed.service.FeedPermissionService;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AppUserRepository appUserRepository;
    private final PostMapper postMapper;
    private final FeedPermissionService feedPermissionService;

    public List<PostResponse> listByAuthor(Long authorUserId) {
        if (!appUserRepository.existsById(authorUserId)) {
            throw new NotFoundException("User " + authorUserId + " was not found.");
        }
        return postRepository.findByAuthor_IdOrderByCreatedAtDesc(authorUserId).stream()
                .map(postMapper::toResponse)
                .toList();
    }

    public PostResponse getById(Long postId) {
        return postMapper.toResponse(postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found.")));
    }

    @Transactional
    public PostResponse create(Long actingUserId, CreatePostRequest request) {
        var actor = feedPermissionService.resolveActor(actingUserId);
        feedPermissionService.requireUser(
                actor,
                actingUserId,
                request.authorUserId(),
                "Only individual or organization users can create posts as themselves."
        );
        var author = appUserRepository.findById(request.authorUserId())
                .orElseThrow(() -> new NotFoundException("User " + request.authorUserId() + " was not found."));
        Post saved = postRepository.save(postMapper.toEntity(request, author));
        return postMapper.toResponse(saved);
    }

    @Transactional
    public PostResponse update(Long postId, Long actingUserId, UpdatePostRequest request) {
        var actor = feedPermissionService.resolveActor(actingUserId);
        Post existing = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found."));
        if (actor.role() != FeedActorRole.ADMIN && !actingUserId.equals(existing.getAuthor().getId())) {
            throw new BadRequestException("Only the author can update this post.");
        }
        postMapper.updateEntity(existing, request);
        return postMapper.toResponse(postRepository.save(existing));
    }

    @Transactional
    public void delete(Long postId, Long actingUserId) {
        var actor = feedPermissionService.resolveActor(actingUserId);
        Post existing = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found."));
        if (actor.role() != FeedActorRole.ADMIN && !actingUserId.equals(existing.getAuthor().getId())) {
            throw new BadRequestException("Only the author can delete this post.");
        }
        postRepository.delete(existing);
    }
}
