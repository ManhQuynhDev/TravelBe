package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
     @Query(value = "SELECT * FROM notification WHERE id = :id", nativeQuery = true)
    Notification findNotificationById(@Param("id") Integer id);
}