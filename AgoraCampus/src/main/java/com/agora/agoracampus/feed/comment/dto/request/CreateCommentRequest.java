package com.agora.agoracampus.feed.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        @NotNull Long authorUserId,
        @NotBlank String content
) {}
