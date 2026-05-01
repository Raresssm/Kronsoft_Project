package com.agora.agoracampus.profile.organization.repository;

import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationProfileRepository extends JpaRepository<OrganizationProfile,Long> {
    Optional<OrganizationProfile> findByProfile_Id(Long profileId);
    List<OrganizationProfile> findByOrganizationNameContainingIgnoreCase(String name);

    List<OrganizationProfile>findByOrganizationLocationContainingIgnoreCase(String location);
    List<OrganizationProfile>findByOrganizationIndustryContainingIgnoreCase(String industry);
    List<OrganizationProfile>findByOrganizationSpecialtiesContainingIgnoreCase(String specialties);


}
