package com.agora.agoracampus.feed.post.mapper;

import com.agora.agoracampus.feed.post.dto.request.CreatePostRequest;
import com.agora.agoracampus.feed.post.dto.request.UpdatePostRequest;
import com.agora.agoracampus.feed.post.dto.response.PostResponse;
import com.agora.agoracampus.feed.post.model.Post;
import com.agora.agoracampus.user.core.model.AppUser;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public Post toEntity(CreatePostRequest request, AppUser author) {
        return Post.builder()
                .author(author)
                .content(request.content())
                .mediaUrl(request.mediaUrl())
                .build();
    }

    public void updateEntity(Post post, UpdatePostRequest request) {
        if (request.content() != null) {
            post.setContent(request.content());
        }
        if (request.mediaUrl() != null) {
            post.setMediaUrl(request.mediaUrl());
        }
    }

    public PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getContent(),
                post.getMediaUrl(),
                post.getCreatedAt()
        );
    }
}
