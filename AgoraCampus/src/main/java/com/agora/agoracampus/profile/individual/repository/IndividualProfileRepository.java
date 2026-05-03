package com.agora.agoracampus.profile.individual.repository;

import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IndividualProfileRepository extends JpaRepository<IndividualProfile, Long> {


    @Query("""
            SELECT i FROM IndividualProfile i
            WHERE LOWER(i.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(i.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(CONCAT(i.firstName, ' ', i.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    List<IndividualProfile> findProfileByName(@Param("name") String name);

    @Query("""
            SELECT i FROM IndividualProfile i
            WHERE LOWER(i.profile.location) LIKE LOWER(CONCAT('%', :location, '%'))
            """)
    List<IndividualProfile> findProfileByLocation(@Param("location") String location);


}
