package com.agora.agoracampus.feed.post.repository;

import com.agora.agoracampus.feed.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findByAuthor_IdOrderByCreatedAtDesc(Long authorUserId);
}
