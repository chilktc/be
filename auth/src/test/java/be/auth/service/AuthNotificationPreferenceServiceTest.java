package be.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.auth.domain.AuthUserNotificationPreference;
import be.auth.dto.response.NotificationPreferenceResponse;
import be.auth.notification.service.AuthNotificationEventPublisher;
import be.auth.repository.AuthUserNotificationPreferenceRepository;

class AuthNotificationPreferenceServiceTest {

	private final AuthUserNotificationPreferenceRepository preferenceRepository = mock(
		AuthUserNotificationPreferenceRepository.class
	);
	private final AuthNotificationEventPublisher eventPublisher = mock(AuthNotificationEventPublisher.class);

	private final AuthNotificationPreferenceService service = new AuthNotificationPreferenceService(
		preferenceRepository,
		eventPublisher
	);

	@Test
	@DisplayName("알림 설정 정보가 없으면 조회 시 true를 반환한다")
	void 조회시_정보없으면_true반환() {
		// given
		UUID userId = UUID.randomUUID();
		when(preferenceRepository.findById(userId)).thenReturn(Optional.empty());

		// when
		NotificationPreferenceResponse response = service.get(userId);

		// then
		assertThat(response.enabled()).isTrue();
	}

	@Test
	@DisplayName("토글 시 기존 true 값을 false로 변경하고 이벤트를 발행한다")
	void 토글시_true를_false로_변경하고_이벤트발행() {
		// given
		UUID userId = UUID.randomUUID();
		AuthUserNotificationPreference preference = AuthUserNotificationPreference.create(userId, true);
		when(preferenceRepository.findById(userId)).thenReturn(Optional.of(preference));

		// when
		NotificationPreferenceResponse response = service.toggle(userId);

		// then
		assertThat(response.enabled()).isFalse();
		verify(preferenceRepository).save(preference);
		verify(eventPublisher).publishUserPreferenceUpdated(userId, false);
	}

	@Test
	@DisplayName("토글 시 데이터가 없으면 true로 생성 후 false로 변경하고 이벤트를 발행한다")
	void 토글시_없으면_true생성후_false_이벤트발행() {
		// given
		UUID userId = UUID.randomUUID();
		when(preferenceRepository.findById(userId)).thenReturn(Optional.empty());

		// when
		NotificationPreferenceResponse response = service.toggle(userId);

		// then
		assertThat(response.enabled()).isFalse();
		verify(preferenceRepository).save(any(AuthUserNotificationPreference.class));
		verify(eventPublisher).publishUserPreferenceUpdated(userId, false);
	}

	@Test
	@DisplayName("초기화 시 정보가 없으면 true로 저장하고 이벤트를 발행한다")
	void 초기화시_없으면_true저장_이벤트발행() {
		// given
		UUID userId = UUID.randomUUID();
		when(preferenceRepository.findById(userId)).thenReturn(Optional.empty());

		// when
		service.initializeAndPublish(userId);

		// then
		verify(preferenceRepository).save(any(AuthUserNotificationPreference.class));
		verify(eventPublisher).publishUserPreferenceUpdated(userId, true);
	}

	@Test
	@DisplayName("초기화 시 기존 설정이 있으면 기존 값을 유지해 이벤트를 발행한다")
	void 초기화시_기존값유지_이벤트발행() {
		// given
		UUID userId = UUID.randomUUID();
		AuthUserNotificationPreference preference = AuthUserNotificationPreference.create(userId, false);
		when(preferenceRepository.findById(userId)).thenReturn(Optional.of(preference));

		// when
		service.initializeAndPublish(userId);

		// then
		verify(preferenceRepository).save(preference);
		verify(eventPublisher).publishUserPreferenceUpdated(userId, false);
		verify(eventPublisher, never()).publishUserPreferenceUpdated(userId, true);
	}
}
