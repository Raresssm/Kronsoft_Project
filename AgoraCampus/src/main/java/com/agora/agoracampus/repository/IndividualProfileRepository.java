package com.agora.agoracampus.repository;

import com.agora.agoracampus.models.IndividualProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndividualProfileRepository extends JpaRepository<IndividualProfile, Long> {

    Optional<IndividualProfile> findByProfileAppUserId(Long appUserId);
}
