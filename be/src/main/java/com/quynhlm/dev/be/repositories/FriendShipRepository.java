package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.FriendShip;

public interface FriendShipRepository extends JpaRepository<FriendShip, Integer> {

        @Query(value = "SELECT DISTINCT f FROM FriendShip f WHERE " +
                        "(f.userSendId = :userSendId AND f.userReceivedId = :userReceivedId OR " +
                        "f.userSendId = :userReceivedId AND f.userReceivedId = :userSendId) " +
                        "AND (f.status = 'PENDING' OR f.status = 'APPROVED')")
        FriendShip findByUserIdsWithFixedStatuses(
                        @Param("userSendId") Integer userSendId,
                        @Param("userReceivedId") Integer userReceivedId);

        @Query(value = "SELECT DISTINCT * FROM friend_ship f WHERE f.user_received_id = :userReceivedId AND f.status = :status", nativeQuery = true)
        List<FriendShip> fetchByUserReceivedIdAndStatus(
                        @Param("userReceivedId") Integer userReceivedId,
                        @Param("status") String status);

        @Query(value = """
                        SELECT DISTINCT u.id , u.fullname , u.avatar_url FROM friend_ship f
                        inner join user u on u.id = f.user_received_id  WHERE f.user_received_id = :userReceivedId AND f.status = :status""", nativeQuery = true)
        List<Object[]> fetchByUserFriends(
                        @Param("userReceivedId") Integer userReceivedId,
                        @Param("status") String status);

        @Query(value = """
                        SELECT DISTINCT u.id, u.fullname, u.avatar_url
                        FROM friend_ship f
                        JOIN user u ON u.id = CASE
                          WHEN f.user_send_id = :user_id THEN f.user_received_id
                                   ELSE f.user_send_id
                                             END
                             WHERE (f.user_send_id = :user_id OR f.user_received_id = :user_id)
                          AND f.status = 'APPROVED'""", nativeQuery = true)
        Page<Object[]> getAllListUserFriends(@Param("user_id") Integer user_id, Pageable pageable);

        @Query(value = """
                        SELECT u.id, u.fullname, u.avatar_url, f.status, MIN(f.create_time) AS create_time
                        FROM friend_ship f
                        JOIN user u ON u.id = f.user_send_id
                        WHERE f.user_received_id = :user_id
                          AND f.status = :status
                        GROUP BY u.id, u.fullname, u.avatar_url, f.status
                        """, nativeQuery = true)
        Page<Object[]> findByUserFriendsWithStatus(@Param("user_id") Integer user_id,
                        @Param("status") String status,
                        Pageable pageable);

        @Query(value = """
                        SELECT u.id, u.fullname, u.avatar_url
                        FROM user u
                        WHERE u.id != :user_id
                          AND u.id NOT IN (
                              SELECT CASE
                                  WHEN f.user_send_id = :user_id THEN f.user_received_id
                                  ELSE f.user_send_id
                              END
                              FROM friend_ship f
                              WHERE (f.user_send_id = :user_id OR f.user_received_id = :user_id)
                                AND f.status = 'APPROVED' OR f.status = 'PENDING'
                          )
                        """, nativeQuery = true)
        Page<Object[]> suggestionFriends(@Param("user_id") Integer user_id, Pageable pageable);
}
