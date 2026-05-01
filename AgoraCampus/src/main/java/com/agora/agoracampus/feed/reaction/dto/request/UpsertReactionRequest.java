package com.agora.agoracampus.feed.reaction.dto.request;

import com.agora.agoracampus.feed.reaction.model.ReactionType;
import jakarta.validation.constraints.NotNull;

public record UpsertReactionRequest(
        @NotNull Long authorUserId,
        @NotNull ReactionType reactionType
) {}
