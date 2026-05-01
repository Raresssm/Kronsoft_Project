package com.agora.agoracampus.feed.reaction.controller;

import com.agora.agoracampus.feed.reaction.dto.request.UpsertReactionRequest;
import com.agora.agoracampus.feed.reaction.dto.response.ReactionResponse;
import com.agora.agoracampus.feed.reaction.service.ReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @GetMapping
    public List<ReactionResponse> list(@PathVariable Long postId) {
        return reactionService.listByPost(postId);
    }

    @PutMapping
    public ReactionResponse upsert(@PathVariable Long postId, @Valid @RequestBody UpsertReactionRequest request) {
        return reactionService.upsert(postId, request);
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(
            @PathVariable Long postId,
            @RequestParam Long authorUserId,
            @RequestParam Long actingUserId
    ) {
        reactionService.remove(postId, authorUserId, actingUserId);
        return ResponseEntity.noContent().build();
    }
}
