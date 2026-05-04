package com.agora.agoracampus.web;

import com.agora.agoracampus.connection.controller.ConnectionController;
import com.agora.agoracampus.connection.dto.request.CreateConnectionRequest;
import com.agora.agoracampus.connection.dto.request.UpdateConnectionStatusRequest;
import com.agora.agoracampus.connection.model.ConnectionStatus;
import com.agora.agoracampus.connection.service.ConnectionService;
import com.agora.agoracampus.feed.comment.controller.CommentController;
import com.agora.agoracampus.feed.comment.dto.request.CreateCommentRequest;
import com.agora.agoracampus.feed.comment.service.CommentService;
import com.agora.agoracampus.feed.post.controller.PostController;
import com.agora.agoracampus.feed.post.dto.request.CreatePostRequest;
import com.agora.agoracampus.feed.post.dto.request.UpdatePostRequest;
import com.agora.agoracampus.feed.post.service.PostService;
import com.agora.agoracampus.feed.reaction.controller.ReactionController;
import com.agora.agoracampus.feed.reaction.dto.request.UpsertReactionRequest;
import com.agora.agoracampus.feed.reaction.model.ReactionType;
import com.agora.agoracampus.feed.reaction.service.ReactionService;
import com.agora.agoracampus.messaging.controller.MessageController;
import com.agora.agoracampus.messaging.dto.request.CreateMessageRequest;
import com.agora.agoracampus.messaging.service.MessageService;
import com.agora.agoracampus.user.core.controller.AppUserController;
import com.agora.agoracampus.user.core.dto.request.CreateAppUserRequest;
import com.agora.agoracampus.user.core.service.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class SocialEndpointTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private ConnectionService connectionService;

    @Mock
    private MessageService messageService;

    @Mock
    private PostService postService;

    @Mock
    private CommentService commentService;

    @Mock
    private ReactionService reactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(
                new AppUserController(appUserService),
                new ConnectionController(connectionService),
                new MessageController(messageService),
                new PostController(postService),
                new CommentController(commentService),
                new ReactionController(reactionService)
        ).build();
    }

    @Test
    void userCreateEndpointDelegatesAndReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "keycloakId": "kc-1",
                                  "email": "user@example.com",
                                  "username": "user"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(appUserService).createUser(any(CreateAppUserRequest.class));
    }

    @Test
    void connectionEndpointsAreMapped() throws Exception {
        when(connectionService.listForUser(1L, 1L)).thenReturn(List.of());
        when(connectionService.listPendingIncoming(2L, 2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/connections/users/1").param("actingUserId", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/connections/incoming/2/pending").param("actingUserId", "2"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/connections/10").param("actingUserId", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/connections")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requesterUserId": 1,
                                  "receiverUserId": 2
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(patch("/api/connections/10/status")
                        .param("actingUserId", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "ACCEPTED"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/connections/10").param("actingUserId", "1"))
                .andExpect(status().isNoContent());

        verify(connectionService).listForUser(1L, 1L);
        verify(connectionService).listPendingIncoming(2L, 2L);
        verify(connectionService).getById(10L, 1L);
        verify(connectionService).create(eq(1L), any(CreateConnectionRequest.class));
        verify(connectionService).updateStatus(eq(10L), eq(2L), any(UpdateConnectionStatusRequest.class));
        verify(connectionService).delete(10L, 1L);
    }

    @Test
    void messageEndpointsAreMapped() throws Exception {
        when(messageService.getConversation(1L, 2L, 1L)).thenReturn(List.of());
        when(messageService.getUnreadIncoming(2L, 2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/messages/between")
                        .param("userIdA", "1")
                        .param("userIdB", "2")
                        .param("actingUserId", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/messages/incoming/2/unread").param("actingUserId", "2"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/messages/10").param("actingUserId", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/messages")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senderUserId": 1,
                                  "receiverUserId": 2,
                                  "content": "hello"
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(patch("/api/messages/10/read").param("actingReceiverUserId", "2"))
                .andExpect(status().isOk());

        verify(messageService).getConversation(1L, 2L, 1L);
        verify(messageService).getUnreadIncoming(2L, 2L);
        verify(messageService).getById(10L, 1L);
        verify(messageService).send(eq(1L), any(CreateMessageRequest.class));
        verify(messageService).markRead(10L, 2L);
    }

    @Test
    void feedEndpointsAreMapped() throws Exception {
        when(postService.listByAuthor(1L)).thenReturn(List.of());
        when(commentService.listByPost(10L)).thenReturn(List.of());
        when(reactionService.listByPost(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/posts/users/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/posts/10")).andExpect(status().isOk());
        mockMvc.perform(post("/api/posts")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorUserId": 1,
                                  "content": "post"
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/posts/10")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "updated"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/posts/10").param("actingUserId", "1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/10/comments")).andExpect(status().isOk());
        mockMvc.perform(post("/api/posts/10/comments")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorUserId": 1,
                                  "content": "comment"
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/posts/10/comments/20").param("actingUserId", "1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/10/reactions")).andExpect(status().isOk());
        mockMvc.perform(put("/api/posts/10/reactions")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "authorUserId": 1,
                                  "reactionType": "LIKE"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/posts/10/reactions")
                        .param("authorUserId", "1")
                        .param("actingUserId", "1"))
                .andExpect(status().isNoContent());

        verify(postService).listByAuthor(1L);
        verify(postService).getById(10L);
        verify(postService).create(eq(1L), any(CreatePostRequest.class));
        verify(postService).update(eq(10L), eq(1L), any(UpdatePostRequest.class));
        verify(postService).delete(10L, 1L);
        verify(commentService).listByPost(10L);
        verify(commentService).create(eq(10L), eq(1L), any(CreateCommentRequest.class));
        verify(commentService).delete(20L, 1L);
        verify(reactionService).listByPost(10L);
        verify(reactionService).upsert(eq(10L), eq(1L), any(UpsertReactionRequest.class));
        verify(reactionService).remove(10L, 1L, 1L);
    }
}
