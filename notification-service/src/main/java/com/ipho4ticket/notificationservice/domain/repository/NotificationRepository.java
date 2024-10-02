package com.ipho4ticket.notificationservice.domain.repository;

import com.ipho4ticket.notificationservice.domain.model.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}
