package com.agora.agoracampus.repository;

import com.agora.agoracampus.models.OrganizationProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationProfileRepository extends JpaRepository<OrganizationProfile,Long> {
    Optional<OrganizationProfile> findByProfile_ProfileId(Integer profileId);
    List<OrganizationProfile> findByOrganizationNameContainingIgnoreCase(String name);

}
