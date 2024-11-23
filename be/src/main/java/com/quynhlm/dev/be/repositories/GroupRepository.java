package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    @Query(value = "SELECT * FROM m_group WHERE name = :name", nativeQuery = true)
    Group findGroupByName(@Param("name") String name);

    @Query(value = "SELECT * FROM m_group WHERE id = :id", nativeQuery = true)
    Group findGroupById(@Param("id") Integer id);

    Page<Group> findAll(Pageable pageable);

    @Query(value = """
            SELECT
                g.id AS groupId,
                u.id AS adminId,
                g.name AS group_name,
                u.fullname AS admin_name,
                g.cover_photo,
                g.bio,
                g.status,
                g.create_time,
                SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count
            FROM
                m_group g
            INNER JOIN
                member m ON m.group_id = g.id
            INNER JOIN
                user u ON g.user_id = u.id
            GROUP BY
                g.id, u.id, g.name, g.cover_photo, g.status, g.bio, u.fullname, g.create_time
            """, nativeQuery = true)
    Page<Object[]> fetchGroup(Pageable pageable);
}
