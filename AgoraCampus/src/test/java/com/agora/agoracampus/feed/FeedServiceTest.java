package com.agora.agoracampus.feed;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.feed.comment.mapper.CommentMapper;
import com.agora.agoracampus.feed.comment.model.Comment;
import com.agora.agoracampus.feed.comment.repository.CommentRepository;
import com.agora.agoracampus.feed.comment.service.CommentService;
import com.agora.agoracampus.feed.post.dto.request.UpdatePostRequest;
import com.agora.agoracampus.feed.post.mapper.PostMapper;
import com.agora.agoracampus.feed.post.model.Post;
import com.agora.agoracampus.feed.post.repository.PostRepository;
import com.agora.agoracampus.feed.post.service.PostService;
import com.agora.agoracampus.feed.reaction.dto.request.UpsertReactionRequest;
import com.agora.agoracampus.feed.reaction.mapper.ReactionMapper;
import com.agora.agoracampus.feed.reaction.model.Reaction;
import com.agora.agoracampus.feed.reaction.model.ReactionType;
import com.agora.agoracampus.feed.reaction.repository.ReactionRepository;
import com.agora.agoracampus.feed.reaction.service.ReactionService;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Test
    void onlyPostAuthorCanUpdatePost() {
        PostService service = new PostService(postRepository, appUserRepository, new PostMapper());
        Post post = post(10L, user(1L), "old");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThrows(
                BadRequestException.class,
                () -> service.update(10L, 2L, new UpdatePostRequest("new", null))
        );
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void postAuthorCanUpdatePost() {
        PostService service = new PostService(postRepository, appUserRepository, new PostMapper());
        Post post = post(10L, user(1L), "old");

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        service.update(10L, 1L, new UpdatePostRequest("new", "media"));

        assertEquals("new", post.getContent());
        assertEquals("media", post.getMediaUrl());
    }

    @Test
    void onlyCommentAuthorCanDeleteComment() {
        CommentService service = new CommentService(commentRepository, postRepository, appUserRepository, new CommentMapper());
        Comment comment = Comment.builder()
                .id(20L)
                .post(post(10L, user(1L), "post"))
                .author(user(1L))
                .content("comment")
                .build();

        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));

        assertThrows(BadRequestException.class, () -> service.delete(20L, 2L));
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void onlyReactionAuthorCanRemoveReaction() {
        ReactionService service = new ReactionService(reactionRepository, postRepository, appUserRepository, new ReactionMapper());

        assertThrows(BadRequestException.class, () -> service.remove(10L, 1L, 2L));
        verify(reactionRepository, never()).delete(any(Reaction.class));
    }

    @Test
    void upsertUpdatesExistingReaction() {
        ReactionService service = new ReactionService(reactionRepository, postRepository, appUserRepository, new ReactionMapper());
        Post post = post(10L, user(1L), "post");
        AppUser author = user(2L);
        Reaction existing = Reaction.builder()
                .id(30L)
                .post(post)
                .author(author)
                .reactionType(ReactionType.LIKE)
                .build();

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(author));
        when(reactionRepository.findByPost_IdAndAuthor_Id(10L, 2L)).thenReturn(Optional.of(existing));
        when(reactionRepository.save(existing)).thenReturn(existing);

        service.upsert(10L, new UpsertReactionRequest(2L, ReactionType.LOVE));

        assertEquals(ReactionType.LOVE, existing.getReactionType());
    }

    private Post post(Long id, AppUser author, String content) {
        return Post.builder()
                .id(id)
                .author(author)
                .content(content)
                .build();
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setKeycloakId("keycloak-" + id);
        user.setEmail("user" + id + "@example.com");
        user.setUsername("user" + id);
        return user;
    }
}
