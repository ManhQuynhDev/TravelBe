package com.quynhlm.dev.be.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.MemberPlan;

public interface MemberPlanRepository extends JpaRepository<MemberPlan, Integer> {
    @Query("SELECT m FROM MemberPlan m WHERE m.userId = :userId AND m.planId = :planId AND m.status IN :status")
    Optional<MemberPlan> findByUserIdAndPlanIdAndStatusIn(
            @Param("userId") Integer userId,
            @Param("planId") Integer planId,
            @Param("status") List<String> status);

     @Query("SELECT m FROM MemberPlan m WHERE m.planId = :planId AND m.status = :status")
    Page<MemberPlan> getRequestToJoinPlan(
                        @Param("planId") Integer planId,
                        @Param("status") String status,
                        Pageable pageable);
}
