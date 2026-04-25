package com.agora.agoracampus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.agora.agoracampus.models.Internship;

import java.util.List;

public interface InternshipRepository extends JpaRepository<Internship, Integer> {


    List<Internship> findByOpportunityId(Integer opportunityId);
}
