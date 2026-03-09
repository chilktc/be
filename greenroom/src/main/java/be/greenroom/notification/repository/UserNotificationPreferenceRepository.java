package be.greenroom.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.notification.domain.UserNotificationPreference;

public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, UUID> {
}
