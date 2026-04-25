package com.agora.agoracampus.repository;

import com.agora.agoracampus.domain.OrganizationProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationProfileRepository extends JpaRepository<OrganizationProfile, Long> {

    Optional<OrganizationProfile> findByProfileAppUserId(Long appUserId);
}
