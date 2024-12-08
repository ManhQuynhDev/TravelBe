package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Integer> {
    
    @Query(value = "SELECT * FROM Invitation WHERE user_received_id = :userId AND group_id =:groupId", nativeQuery = true)
    Invitation findInvitationById(@Param("userId") Integer userId , @Param("groupId") Integer groupId);

    @Query(value = """
               SELECT 
                    i.user_received_id,
                    i.group_id,
                    u.fullname,
                    u.avatar_url,
                    g.name,
                    g.bio,
                    g.cover_photo,
                    userGroup.fullname as admin_name,
                    userGroup.avatar_url as admin_avatar
                    FROM Invitation i
                INNER JOIN User u on u.id = i.user_received_id
                INNER JOIN m_group g on g.id = i.group_id
                INNER JOIN User userGroup on userGroup.id = g.user_id
                WHERE user_received_id = :userId;
            """, nativeQuery = true)
    Page<Object[]> getAllInvitaionWithUserId(@Param("userId") Integer userId , Pageable pageable);
}
