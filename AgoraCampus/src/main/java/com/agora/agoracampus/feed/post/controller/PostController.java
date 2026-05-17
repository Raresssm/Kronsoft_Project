package com.agora.agoracampus.feed.post.controller;

import com.agora.agoracampus.feed.post.dto.request.CreatePostRequest;
import com.agora.agoracampus.feed.post.dto.request.UpdatePostRequest;
import com.agora.agoracampus.feed.post.dto.response.PostResponse;
import com.agora.agoracampus.feed.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<PostResponse> listAll() {
        return postService.listAll();
    }

    @GetMapping("/users/{authorUserId}")
    public List<PostResponse> listByAuthor(@PathVariable Long authorUserId) {
        return postService.listByAuthor(authorUserId);
    }

    @GetMapping("/{id}")
    public PostResponse getById(@PathVariable Long id) {
        return postService.getById(id);
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(
            @RequestParam Long actingUserId,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(actingUserId, request));
    }

    @PutMapping("/{id}")
    public PostResponse update(
            @PathVariable Long id,
            @RequestParam Long actingUserId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return postService.update(id, actingUserId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long actingUserId) {
        postService.delete(id, actingUserId);
        return ResponseEntity.noContent().build();
    }
}
