package com.quynhlm.dev.be.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {
        @Query(value = "SELECT * FROM Member WHERE id = :id", nativeQuery = true)
        Member findMemberById(@Param("id") Integer id);

        @Query("SELECT m FROM Member m WHERE m.userId = :userId AND m.groupId = :groupId AND m.status IN :status")
        Optional<Member> findByUser_idAndGroup_idAndStatusIn(
                        @Param("userId") Integer userId,
                        @Param("groupId") Integer groupId,
                        @Param("status") List<String> status);

        @Query(value = "SELECT * FROM Member m WHERE m.groupId = :groupId AND m.status = :status", nativeQuery = true)
        List<Member> findByGroup_idAndStatus(@Param("groupId") Integer groupId, @Param("status") String status);

        @Query("SELECT m FROM Member m WHERE m.groupId = :groupId AND m.status = :status")
        Page<Member> getRequestToJoinGroup(
                        @Param("groupId") Integer groupId,
                        @Param("status") String status,
                        Pageable pageable);
}
