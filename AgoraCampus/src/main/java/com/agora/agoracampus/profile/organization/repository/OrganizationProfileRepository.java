package com.agora.agoracampus.profile.organization.repository;

import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationProfileRepository extends JpaRepository<OrganizationProfile,Long> {
    Optional<OrganizationProfile> findByProfile_Id(Long profileId);
    List<OrganizationProfile> findByOrganizationNameContainingIgnoreCase(String name);

    @Query("""
            SELECT o FROM OrganizationProfile o
            WHERE LOWER(o.profile.location) LIKE LOWER(CONCAT('%', :location, '%'))
            """)
    List<OrganizationProfile> findByLocationContainingIgnoreCase(@Param("location") String location);

    List<OrganizationProfile> findByIndustryContainingIgnoreCase(String industry);
    List<OrganizationProfile> findBySpecialtiesContainingIgnoreCase(String specialties);


}
