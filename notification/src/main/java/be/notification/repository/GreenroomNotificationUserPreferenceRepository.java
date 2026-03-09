package be.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.notification.domain.GreenroomNotificationUserPreference;

public interface GreenroomNotificationUserPreferenceRepository
	extends JpaRepository<GreenroomNotificationUserPreference, UUID> {
}
