package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Integer> {
    
    @Query(value = "SELECT * FROM Invitation WHERE userId = :userId AND groupId =:groupId", nativeQuery = true)
    Invitation findInvitationById(@Param("userId") Integer userId , @Param("groupId") Integer groupId);

    // @Query(value = """
    //             SELECT 
    //                 i.user_id
    //                 i.group_id,
    //                 u.fullname,
    //                 u.avatar_url,
    //                 g.name,
    //                 g.bio,
    //                 g.cover_photo,
    //                 userGroup.fullname as admin_name,
    //                 userGroup.avatar_url as admin_avatar,
    //                 SUM(CASE WHEN m.status = 'APPROVED' THEN 1 ELSE 0 END) AS member_count
    //                 FROM Invitation i
    //             INNER JOIN User u on u.id = i.user_id
    //             INNER JOIN m_group g on g.id = i.group_id
    //             INNER JOIN member m on m.group_id = g.id
    //             INNER JOIN User userGroup on userGroup.id = g.user_id
    //             WHERE userId = :userId
    //         """, nativeQuery = true)
    // Page<Invitation> getAllInvitaionWithUserId(@Param("userId") Integer userId , Pageable pageable);

}
