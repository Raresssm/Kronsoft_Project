package com.agora.agoracampus.feed.post.dto.request;

import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        String content,
        @Size(max = 1024)
        String mediaUrl
) {}
