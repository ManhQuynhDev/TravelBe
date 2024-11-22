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

    @Query(value = """
              select
                u.id as user_id,
                t.id as plan_id,
                m.id as member_id,
                u.fullname,
                u.avatar_url,
                m.role,
                m.join_time
             from member_plan m
            inner join travel_plan t on t.id = m.plan_id
            inner join user u on u.id = m.user_id
            where m.plan_id  = :planId;
                                    """, nativeQuery = true)
    List<Object[]> foundMemberJoinPlan(@Param("planId") Integer planId);
}
