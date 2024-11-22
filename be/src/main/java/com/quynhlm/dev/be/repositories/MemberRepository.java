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

    @Query(value = """
            SELECT
            m.user_id,
            m.id AS member_id,
            g.id AS group_id,
            g.name,
            userGroup.fullname AS adminName,
            g.cover_photo,
            g.bio,
            m.status,
            m.role,
            m.join_time,
            (SELECT COUNT(*)
            FROM member
            WHERE group_id = g.id AND status = "APPROVED") AS member_count
            FROM member m
            INNER JOIN user u ON u.id = m.user_id
            INNER JOIN m_group g ON g.id = m.group_id
            INNER JOIN user userGroup ON g.user_id = userGroup.id
            WHERE m.user_id = :userId AND m.status = "APPROVED";
                """, nativeQuery = true)
    Page<Object[]> foundUserJoinGroup(@Param("userId") Integer userId,
            Pageable pageable);

    @Query(value = """
                        SELECT
                g.id AS group_id,
                u.id AS admin_id,
                g.name AS group_name,
                u.fullname AS admin_name,
                g.cover_photo,
                g.bio,
                g.create_time,
                SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count
            FROM
                member m
            INNER JOIN
                m_group g ON g.id = m.group_id
            INNER JOIN
                user u ON u.id = m.user_id
            WHERE
                m.user_id = :userId
                AND m.role = 'ADMIN'
            GROUP BY
                g.id, u.id, g.name, u.fullname, g.cover_photo, g.bio, g.create_time;
                        """, nativeQuery = true)
    Page<Object[]> fetchGroupUserCreate(@Param("userId") Integer userId, Pageable pageable);

    @Query(value = """
                select
            	u.id as user_id,
                g.id as group_id,
                m.id as member_id,
            	u.fullname,
                u.avatar_url,
                m.role,
                m.join_time
             from member m
            inner join m_group g on g.id = m.group_id
            inner join user u on u.id = m.user_id
            where m.group_id  = :groupId;
                                    """, nativeQuery = true)
    Page<Object[]> foundMemberJoinGroup(@Param("groupId") Integer groupId, Pageable pageable);

    @Query(value = """
                select
                u.id as user_id,
                g.id as group_id,
                m.id as member_id,
                u.fullname,
                u.avatar_url,
                m.role,
                m.join_time
             from member m
            inner join m_group g on g.id = m.group_id
            inner join user u on u.id = m.user_id
            where m.group_id  = :groupId;
                                    """, nativeQuery = true)
    List<Object[]> getMemberJoinGroup(@Param("groupId") Integer groupId);

}
