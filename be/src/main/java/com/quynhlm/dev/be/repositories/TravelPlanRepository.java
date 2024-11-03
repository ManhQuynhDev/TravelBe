package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Travel_Plan;

public interface TravelPlanRepository extends JpaRepository<Travel_Plan, Integer> {
    @Query(value = "SELECT * FROM Travel_Plan WHERE id = :id", nativeQuery = true)
    Travel_Plan getAnTravel_Plan(@Param("id") int id);
}
