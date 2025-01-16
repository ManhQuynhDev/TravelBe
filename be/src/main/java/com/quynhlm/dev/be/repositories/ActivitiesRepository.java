package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Activities;

public interface ActivitiesRepository extends JpaRepository<Activities, Integer> {
    @Query("SELECT a FROM Activities a WHERE a.name = :name AND a.planId = :planId AND a.delflag = 0")
    Activities findByNameAndPlanId(
            @Param("name") String name,
            @Param("planId") Integer planId);

    @Query(value = "SELECT * FROM Activities WHERE id = :id AND delflag = 0", nativeQuery = true)
    Activities findActivities(@Param("id") Integer id);

    @Query(value = """
            select a.id , u.id as user_id ,t.id as plan_id,a.location_id, a.name , u.fullname , u.avatar_url, t.plan_name , a.description , a.time  , a.cost , a.status , a.create_time
            from activities a
            INNER JOIN User u ON u.id = a.user_id
            INNER JOIN Travel_plan t ON t.id = a.plan_id
            WHERE a.id = :id AND a.delflag = 0;
                """, nativeQuery = true)
    List<Object[]> findActivitiesWithId(@Param("id") Integer id);

    @Query(value = "SELECT * FROM activities WHERE plan_id = :planId AND delflag = 0", nativeQuery = true)
    Page<Activities> findAllActivitiesWithPlanId(@Param("planId") Integer planId, Pageable pageable);

    @Query(value = "SELECT * FROM Activities WHERE LOWER(name) LIKE LOWER(CONCAT('%', :q, '%')) AND delflag = 0", nativeQuery = true)
    Page<Activities> searchActivitiesByName(@Param("q") String keyword, Pageable pageable);
}
