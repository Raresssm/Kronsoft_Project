package com.agora.agoracampus.opportunity.studentproject.repository;

import com.agora.agoracampus.opportunity.studentproject.model.StudentProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProjectRepository extends JpaRepository<StudentProject, Long> {

    Optional<StudentProject> findByOpportunity_Id(Long opportunityId);
}
