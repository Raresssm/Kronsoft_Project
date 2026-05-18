package com.agora.agoracampus.feed.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotNull Long authorUserId,
        @NotBlank String content,
        @Size(max = 1024)
        String mediaUrl
) {}
