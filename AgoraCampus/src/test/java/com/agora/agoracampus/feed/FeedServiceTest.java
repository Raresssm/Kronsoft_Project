package com.agora.agoracampus.feed;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.feed.comment.dto.request.CreateCommentRequest;
import com.agora.agoracampus.feed.comment.mapper.CommentMapper;
import com.agora.agoracampus.feed.comment.model.Comment;
import com.agora.agoracampus.feed.comment.repository.CommentRepository;
import com.agora.agoracampus.feed.comment.service.CommentService;
import com.agora.agoracampus.feed.post.dto.request.CreatePostRequest;
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
import com.agora.agoracampus.feed.service.FeedPermissionService;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    @Mock
    private ProfileRepository profileRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onlyPostAuthorCanUpdatePost() {
        PostService service = postService();
        Post post = post(10L, user(1L), "old");

        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(profile(user(2L), ProfileType.INDIVIDUAL)));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertThrows(
                BadRequestException.class,
                () -> service.update(10L, 2L, new UpdatePostRequest("new", null))
        );
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void postAuthorCanUpdatePost() {
        PostService service = postService();
        Post post = post(10L, user(1L), "old");

        when(appUserRepository.existsById(1L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(profile(user(1L), ProfileType.INDIVIDUAL)));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        service.update(10L, 1L, new UpdatePostRequest("new", "media"));

        assertEquals("new", post.getContent());
        assertEquals("media", post.getMediaUrl());
    }

    @Test
    void adminCanDeleteAnyPost() {
        PostService service = postService();
        Post post = post(10L, user(1L), "post");
        authenticateAsAdmin();

        when(appUserRepository.existsById(99L)).thenReturn(true);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        service.delete(10L, 99L);

        verify(postRepository).delete(post);
    }

    @Test
    void userWithoutProfileCannotCreatePost() {
        PostService service = postService();

        when(appUserRepository.existsById(1L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> service.create(1L, new CreatePostRequest(1L, "post", null))
        );
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void adminCanCreatePostAsSelf() {
        PostService service = postService();
        AppUser author = user(99L);
        authenticateAsAdmin();

        when(appUserRepository.existsById(99L)).thenReturn(true);
        when(appUserRepository.findById(99L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.create(99L, new CreatePostRequest(99L, "post", null)));
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void organizationUserCanCreatePostAsSelf() {
        PostService service = postService();
        AppUser author = user(1L);

        when(appUserRepository.existsById(1L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(1L)).thenReturn(Optional.of(profile(author, ProfileType.ORGANIZATION)));
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.create(1L, new CreatePostRequest(1L, "post", null)));
    }

    @Test
    void onlyCommentAuthorCanDeleteComment() {
        CommentService service = commentService();
        Comment comment = Comment.builder()
                .id(20L)
                .post(post(10L, user(1L), "post"))
                .author(user(1L))
                .content("comment")
                .build();

        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(profile(user(2L), ProfileType.INDIVIDUAL)));
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));

        assertThrows(BadRequestException.class, () -> service.delete(20L, 2L));
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void individualUserCanCreateCommentAsSelf() {
        CommentService service = commentService();
        Post post = post(10L, user(1L), "post");
        AppUser author = user(2L);

        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(profile(author, ProfileType.INDIVIDUAL)));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.create(10L, 2L, new CreateCommentRequest(2L, "comment")));
    }

    @Test
    void adminCanDeleteAnyComment() {
        CommentService service = commentService();
        Comment comment = Comment.builder()
                .id(20L)
                .post(post(10L, user(1L), "post"))
                .author(user(1L))
                .content("comment")
                .build();
        authenticateAsAdmin();

        when(appUserRepository.existsById(99L)).thenReturn(true);
        when(commentRepository.findById(20L)).thenReturn(Optional.of(comment));

        service.delete(20L, 99L);

        verify(commentRepository).delete(comment);
    }

    @Test
    void onlyReactionAuthorCanRemoveReaction() {
        ReactionService service = reactionService();

        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(profile(user(2L), ProfileType.INDIVIDUAL)));

        assertThrows(BadRequestException.class, () -> service.remove(10L, 1L, 2L));
        verify(reactionRepository, never()).delete(any(Reaction.class));
    }

    @Test
    void upsertUpdatesExistingReaction() {
        ReactionService service = reactionService();
        Post post = post(10L, user(1L), "post");
        AppUser author = user(2L);
        Reaction existing = Reaction.builder()
                .id(30L)
                .post(post)
                .author(author)
                .reactionType(ReactionType.LIKE)
                .build();

        when(appUserRepository.existsById(2L)).thenReturn(true);
        when(profileRepository.findByAppUser_Id(2L)).thenReturn(Optional.of(profile(author, ProfileType.INDIVIDUAL)));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(author));
        when(reactionRepository.findByPost_IdAndAuthor_Id(10L, 2L)).thenReturn(Optional.of(existing));
        when(reactionRepository.save(existing)).thenReturn(existing);

        service.upsert(10L, 2L, new UpsertReactionRequest(2L, ReactionType.LOVE));

        assertEquals(ReactionType.LOVE, existing.getReactionType());
    }

    @Test
    void adminCanRemoveAnyReaction() {
        ReactionService service = reactionService();
        Reaction existing = Reaction.builder()
                .id(30L)
                .post(post(10L, user(1L), "post"))
                .author(user(2L))
                .reactionType(ReactionType.LIKE)
                .build();
        authenticateAsAdmin();

        when(appUserRepository.existsById(99L)).thenReturn(true);
        when(reactionRepository.findByPost_IdAndAuthor_Id(10L, 2L)).thenReturn(Optional.of(existing));

        service.remove(10L, 2L, 99L);

        verify(reactionRepository).delete(existing);
    }

    private PostService postService() {
        return new PostService(postRepository, appUserRepository, new PostMapper(), permissionService());
    }

    private CommentService commentService() {
        return new CommentService(commentRepository, postRepository, appUserRepository, new CommentMapper(), permissionService());
    }

    private ReactionService reactionService() {
        return new ReactionService(reactionRepository, postRepository, appUserRepository, new ReactionMapper(), permissionService());
    }

    private FeedPermissionService permissionService() {
        return new FeedPermissionService(appUserRepository, profileRepository);
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

    private Profile profile(AppUser user, ProfileType profileType) {
        Profile profile = new Profile();
        profile.setAppUser(user);
        profile.setProfileType(profileType);
        return profile;
    }

    private void authenticateAsAdmin() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", null, "ROLE_ADMIN");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
