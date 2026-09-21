package com.taskmanager.notification.repository;

import com.taskmanager.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
