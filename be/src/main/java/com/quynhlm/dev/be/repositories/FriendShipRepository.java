package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.FriendShip;

public interface FriendShipRepository extends JpaRepository<FriendShip, Integer> {

        @Query(value = "SELECT f FROM FriendShip f WHERE f.userSendId = :userSendId AND f.userReceivedId = :userReceivedId AND f.status = :status")
        FriendShip findByUserSendIdAndUserReceivedIdAndStatusIn(
                        @Param("userSendId") Integer userSendId,
                        @Param("userReceivedId") Integer userReceivedId,
                        @Param("status") String status);

        @Query(value = "SELECT * FROM friend_ship f WHERE f.user_received_id = :userReceivedId AND f.status = :status", nativeQuery = true)
        List<FriendShip> fetchByUserReceivedIdAndStatus(
                        @Param("userReceivedId") Integer userReceivedId,
                        @Param("status") String status);

        @Query(value = """
                        SELECT u.id , u.fullname , u.avatar_url FROM friend_ship f
                        inner join user u on u.id = f.user_received_id  WHERE f.user_received_id = :userReceivedId AND f.status = :status""", nativeQuery = true)
        List<Object[]> fetchByUserFriends(
                        @Param("userReceivedId") Integer userReceivedId,
                        @Param("status") String status);

        @Query(value = """
                        SELECT u.id, u.fullname, u.avatar_url
                        FROM friend_ship f
                        JOIN user u ON u.id = CASE
                          WHEN f.user_send_id = :user_id THEN f.user_received_id
                                   ELSE f.user_send_id
                                             END
                             WHERE (f.user_send_id = :user_id OR f.user_received_id = :user_id)
                          AND f.status = 'APPROVED'""", nativeQuery = true)
        Page<Object[]> getAllListUserFriends(@Param("user_id") Integer user_id, Pageable pageable);

        @Query(value = """
                        SELECT u.id, u.fullname, u.avatar_url , f.status , f.create_time
                                        FROM friend_ship f
                                        JOIN user u ON u.id = CASE
                                          WHEN f.user_send_id = :user_id THEN f.user_received_id
                                                   ELSE f.user_send_id
                                                             END
                                             WHERE (f.user_send_id = :user_id OR f.user_received_id = :user_id)
                                          AND f.status = :status""", nativeQuery = true)
        Page<Object[]> findByUserFriends(@Param("user_id") Integer user_id, @Param("status") String status,
                        Pageable pageable);
}
