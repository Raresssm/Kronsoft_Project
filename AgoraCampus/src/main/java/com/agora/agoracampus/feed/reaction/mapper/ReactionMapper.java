package com.agora.agoracampus.feed.reaction.mapper;

import com.agora.agoracampus.feed.reaction.dto.response.ReactionResponse;
import com.agora.agoracampus.feed.reaction.model.Reaction;
import org.springframework.stereotype.Component;

@Component
public class ReactionMapper {

    public ReactionResponse toResponse(Reaction reaction) {
        return new ReactionResponse(
                reaction.getId(),
                reaction.getPost().getId(),
                reaction.getAuthor().getId(),
                reaction.getReactionType(),
                reaction.getCreatedAt()
        );
    }
}
