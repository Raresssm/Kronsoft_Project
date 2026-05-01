package com.agora.agoracampus.profile.background.repository;

import com.agora.agoracampus.profile.background.model.Background;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackgroundRepository extends JpaRepository<Background,Long> {
    List<Background> findByIndividualProfile_Id(Long individualProfileId);
}
