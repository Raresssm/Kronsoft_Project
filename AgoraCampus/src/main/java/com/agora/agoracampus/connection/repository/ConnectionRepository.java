package com.agora.agoracampus.connection.repository;

import com.agora.agoracampus.connection.model.Connection;
import com.agora.agoracampus.connection.model.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    boolean existsByRequester_IdAndReceiver_Id(Long requesterId, Long receiverId);

    Optional<Connection> findByRequester_IdAndReceiver_Id(Long requesterId, Long receiverId);

    @Query("""
            SELECT c FROM Connection c
            WHERE (c.requester.id = :a AND c.receiver.id = :b)
               OR (c.requester.id = :b AND c.receiver.id = :a)
            """)
    Optional<Connection> findBetweenUsers(@Param("a") Long userId1, @Param("b") Long userId2);

    @Query("""
            SELECT c FROM Connection c
            WHERE c.requester.id = :userId OR c.receiver.id = :userId
            ORDER BY c.createdAt DESC
            """)
    List<Connection> findAllForUser(@Param("userId") Long userId);

    List<Connection> findByReceiver_IdAndStatus(Long receiverId, ConnectionStatus status);
}
