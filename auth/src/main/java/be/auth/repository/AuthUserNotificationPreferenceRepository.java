package be.auth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.auth.domain.AuthUserNotificationPreference;

public interface AuthUserNotificationPreferenceRepository extends JpaRepository<AuthUserNotificationPreference, UUID> {
}
