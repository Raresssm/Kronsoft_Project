package com.agora.agoracampus.feed.reaction.repository;

import com.agora.agoracampus.feed.reaction.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    List<Reaction> findByPost_Id(Long postId);

    Optional<Reaction> findByPost_IdAndAuthor_Id(Long postId, Long authorUserId);
}
