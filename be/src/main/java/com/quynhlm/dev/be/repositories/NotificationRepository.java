package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quynhlm.dev.be.model.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

}