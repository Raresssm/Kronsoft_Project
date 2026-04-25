package com.agora.agoracampus.repository;

import com.agora.agoracampus.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByKeycloakId(String keycloakId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByKeycloakId(String keycloakId);
}
