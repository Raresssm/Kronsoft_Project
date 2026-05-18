package com.agora.agoracampus.feed.comment.dto.response;

import java.time.Instant;

public record CommentResponse(
        Long commentId,
        Long postId,
        Long authorUserId,
        String content,
        Instant createdAt
) {}
