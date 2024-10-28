package com.quynhlm.dev.be.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    @Query(value = "SELECT * FROM Member WHERE id = :id", nativeQuery = true)
    Member findMemberById(@Param("id") Integer id);

    @Query("SELECT m FROM Member m WHERE m.user_id = :user_id AND m.group_id = :group_id AND m.status IN :status")
    Optional<Member> findByUser_idAndGroup_idAndStatusIn(
        @Param("user_id") Integer user_id,
        @Param("group_id") Integer group_id,
        @Param("status") List<String> status
    );
}
