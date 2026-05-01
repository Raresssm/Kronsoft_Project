package com.agora.agoracampus.profile.individual.repository;

import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndividualProfileRepository extends JpaRepository<IndividualProfile, Long> {

    Optional<IndividualProfile> findByProfileAppUserId(Long appUserId);
}
