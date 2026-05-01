package com.agora.agoracampus.feed.reaction.dto.response;

import com.agora.agoracampus.feed.reaction.model.ReactionType;

import java.time.Instant;

public record ReactionResponse(
        Long reactionId,
        Long postId,
        Long authorUserId,
        ReactionType reactionType,
        Instant createdAt
) {}
