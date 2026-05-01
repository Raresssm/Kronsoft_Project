package com.agora.agoracampus.feed.comment.controller;

import com.agora.agoracampus.feed.comment.dto.request.CreateCommentRequest;
import com.agora.agoracampus.feed.comment.dto.response.CommentResponse;
import com.agora.agoracampus.feed.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentResponse> list(@PathVariable Long postId) {
        return commentService.listByPost(postId);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(postId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long actingUserId
    ) {
        commentService.delete(commentId, actingUserId);
        return ResponseEntity.noContent().build();
    }
}
