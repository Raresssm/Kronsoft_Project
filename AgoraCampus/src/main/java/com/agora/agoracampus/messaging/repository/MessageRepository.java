package com.agora.agoracampus.messaging.repository;

import com.agora.agoracampus.messaging.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.id = :u1 AND m.receiver.id = :u2)
               OR (m.sender.id = :u2 AND m.receiver.id = :u1)
            ORDER BY m.sentAt ASC
            """)
    List<Message> findConversationBetween(@Param("u1") Long userId1, @Param("u2") Long userId2);

    @Query("""
            SELECT m FROM Message m
            WHERE m.receiver.id = :userId AND m.acknowledged = false
            ORDER BY m.sentAt DESC
            """)
    List<Message> findUnreadIncoming(@Param("userId") Long userId);
}
