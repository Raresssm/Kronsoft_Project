package com.agora.agoracampus.feed.reaction.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.feed.post.repository.PostRepository;
import com.agora.agoracampus.feed.reaction.dto.request.UpsertReactionRequest;
import com.agora.agoracampus.feed.reaction.dto.response.ReactionResponse;
import com.agora.agoracampus.feed.reaction.mapper.ReactionMapper;
import com.agora.agoracampus.feed.reaction.model.Reaction;
import com.agora.agoracampus.feed.reaction.repository.ReactionRepository;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final AppUserRepository appUserRepository;
    private final ReactionMapper reactionMapper;

    public List<ReactionResponse> listByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post not found.");
        }
        return reactionRepository.findByPost_Id(postId).stream()
                .map(reactionMapper::toResponse)
                .toList();
    }

    @Transactional
    public ReactionResponse upsert(Long postId, UpsertReactionRequest request) {
        var post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found."));
        var author = appUserRepository.findById(request.authorUserId())
                .orElseThrow(() -> new NotFoundException("User " + request.authorUserId() + " was not found."));

        return reactionRepository.findByPost_IdAndAuthor_Id(postId, request.authorUserId())
                .map(existing -> {
                    existing.setReactionType(request.reactionType());
                    return reactionMapper.toResponse(reactionRepository.save(existing));
                })
                .orElseGet(() -> {
                    Reaction created = Reaction.builder()
                            .post(post)
                            .author(author)
                            .reactionType(request.reactionType())
                            .build();
                    return reactionMapper.toResponse(reactionRepository.save(created));
                });
    }

    @Transactional
    public void remove(Long postId, Long authorUserId, Long actingUserId) {
        if (!actingUserId.equals(authorUserId)) {
            throw new BadRequestException("Only the reaction author can remove their reaction.");
        }
        Reaction existing = reactionRepository.findByPost_IdAndAuthor_Id(postId, authorUserId)
                .orElseThrow(() -> new NotFoundException("Reaction not found."));
        reactionRepository.delete(existing);
    }
}
