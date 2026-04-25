package com.agora.agoracampus.repository;

import com.agora.agoracampus.domain.IndividualProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndividualProfileRepository extends JpaRepository<IndividualProfile, Long> {

    Optional<IndividualProfile> findByProfileAppUserId(Long appUserId);
}
