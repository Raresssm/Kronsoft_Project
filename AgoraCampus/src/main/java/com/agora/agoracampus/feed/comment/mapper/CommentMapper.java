package com.agora.agoracampus.feed.comment.mapper;

import com.agora.agoracampus.feed.comment.dto.response.CommentResponse;
import com.agora.agoracampus.feed.comment.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthor().getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
