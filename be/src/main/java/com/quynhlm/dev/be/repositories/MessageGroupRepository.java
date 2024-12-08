package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.MessageGroup;

public interface MessageGroupRepository extends JpaRepository<MessageGroup, Integer> {
    List<MessageGroup> findByGroupId(Integer groupId);

    @Query(value = """
                SELECT
                g.id,
                g.user_send_id,
                g.group_id,
                g.content,
                g.media_url,
                u.fullname,
                u.avatar_url,
                s.status,
                g.send_time
            FROM
                message_group g
            INNER JOIN
                User u ON u.id = g.user_send_id
            INNER JOIN
                message_status s ON s.message_id = g.id
            WHERE
                g.group_id = :group_id
            ORDER BY
                g.send_time ASC;
                """, nativeQuery = true)
    Page<Object[]> findAllMessageGroup(@Param("group_id") Integer group_id, Pageable pageable);

    @Query(value = """
                SELECT
                g.id,
                g.user_send_id,
                g.group_id,
                g.content,
                g.media_url,
                u.fullname,
                u.avatar_url,
                s.status,
                g.send_time
            FROM
                message_group g
            INNER JOIN
                User u ON u.id = g.user_send_id
            INNER JOIN
                message_status s ON s.message_id = g.id
            WHERE
                g.id = :id
                """, nativeQuery = true)
    List<Object[]> findAnMessage(@Param("id") Integer id);


    @Query(value = """
             select m from message_group m WHERE m.id = :id
                """, nativeQuery = true)
    MessageGroup findByMessageId(@Param("id") Integer id);
}
