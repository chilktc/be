package be.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.auth.domain.AuthUserNotificationPreference;
import be.auth.domain.User;
import be.auth.dto.response.NotificationPreferenceResponse;
import be.auth.notification.service.AuthNotificationEventPublisher;
import be.auth.repository.AuthUserNotificationPreferenceRepository;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthNotificationPreferenceService {

	private final AuthUserNotificationPreferenceRepository preferenceRepository;
	private final AuthNotificationEventPublisher eventPublisher;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public NotificationPreferenceResponse get(UUID userId) {
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
		boolean enabled = preferenceRepository.findById(userId)
			.map(AuthUserNotificationPreference::isEnabled)
			.orElse(true);
		return new NotificationPreferenceResponse(enabled);
	}

	@Transactional
	public NotificationPreferenceResponse toggle(UUID userId) {
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
		AuthUserNotificationPreference preference = preferenceRepository.findById(userId)
			.orElseGet(() -> AuthUserNotificationPreference.create(userId, true));
		boolean enabled = !preference.isEnabled();
		preference.changeEnabled(enabled);
		preferenceRepository.save(preference);
		eventPublisher.publishUserPreferenceUpdated(userId, enabled);
		return new NotificationPreferenceResponse(enabled);
	}

	@Transactional
	public void initializeAndPublish(UUID userId) {
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
		AuthUserNotificationPreference preference = preferenceRepository.findById(userId)
			.orElseGet(() -> AuthUserNotificationPreference.create(userId, true));
		preferenceRepository.save(preference);
		eventPublisher.publishUserPreferenceUpdated(userId, preference.isEnabled());
	}
}
