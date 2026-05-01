package com.agora.agoracampus.profile.core.repository;

import com.agora.agoracampus.profile.core.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile,Long> {
    Optional<Profile> findByAppUser_Id(Long appUserId);

}
