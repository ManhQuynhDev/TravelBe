package com.quynhlm.dev.be.repositories;

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
    Page<FriendShip> findByUserReceivedIdAndStatus(
            @Param("userReceivedId") Integer userReceivedId,
            @Param("status") String status, Pageable pageable);
}
