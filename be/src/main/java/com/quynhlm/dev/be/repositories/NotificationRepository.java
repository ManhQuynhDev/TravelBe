package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query(value = "SELECT * FROM notification WHERE id = :id", nativeQuery = true)
    Notification findNotificationById(@Param("id") Integer id);

    @Query(value = """
            SELECT n.id , n.title , n.message , n.media_url , n.status ,u.id , u.fullname , u.avatar_url , n.notification_time FROM notification n
            INNER JOIN User u on u.id = n.user_received_id
            WHERE user_received_id = :id
            ORDER BY  n.notification_time DESC
                                """, nativeQuery = true)
    List<Object[]> getAllNotificationWithUserId(@Param("id") Integer id);

    @Query(value = """
            SELECT n.id , n.title , n.message , n.media_url , n.status ,u.id , u.fullname , u.avatar_url , n.notification_time FROM notification n
            INNER JOIN User u on u.id = n.user_send_id
            WHERE n.id = :id;
                                """, nativeQuery = true)
    List<Object[]> getAllNotificationWithId(@Param("id") Integer id);
}