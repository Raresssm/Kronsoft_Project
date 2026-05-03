package com.agora.agoracampus.profile.individual.repository;

import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IndividualProfileRepository extends JpaRepository<IndividualProfile, Long> {


    List<IndividualProfile> findProfileByName(String name);
    List<IndividualProfile> findProfileByLocation(String location);


}
