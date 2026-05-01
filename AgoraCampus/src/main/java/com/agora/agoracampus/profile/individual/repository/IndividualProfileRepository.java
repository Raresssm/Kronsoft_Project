package com.agora.agoracampus.profile.individual.repository;

import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IndividualProfileRepository extends JpaRepository<IndividualProfile, Long> {

    Optional<IndividualProfile> findByProfileAppUserId(Long appUserId);
    List<IndividualProfile> findProfileByName(String name);
    List<IndividualProfile> findProfileByLocation(String location);

}
