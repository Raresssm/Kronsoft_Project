package com.agora.agoracampus.feed.post.dto.response;

import java.time.Instant;

public record PostResponse(
        Long postId,
        Long authorUserId,
        String content,
        String mediaUrl,
        Instant createdAt
) {}
