package be.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.notification.domain.GreenroomNotificationProcessedEvent;

public interface GreenroomNotificationProcessedEventRepository
	extends JpaRepository<GreenroomNotificationProcessedEvent, UUID> {
}
