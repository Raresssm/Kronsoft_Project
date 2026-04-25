package com.agora.agoracampus.repository;

import com.agora.agoracampus.domain.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByAppUserId(Long appUserId);

    Optional<Profile> findByAppUserId(Long appUserId);
}
