package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Activities;

public interface ActivitiesRepository extends JpaRepository<Activities, Integer> {
     @Query("SELECT a FROM Activities a WHERE a.name = :name AND a.planId = :planId")
    Activities findByNameAndPlanId(
        @Param("name") String name,
        @Param("planId") Integer planId
    );

    @Query(value = "SELECT * FROM Activities WHERE id = :id", nativeQuery = true)
    Activities findActivities(@Param("id") Integer id);

    Page<Activities> findAll(Pageable pageable);
}