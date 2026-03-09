package be.auth.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import be.auth.dto.request.InviteUserRequest;
import be.auth.event.UserInvitedEvent;
import be.auth.jwt.Role;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;

class AdminInviteServiceTest {

	private final UserRepository userRepository
		= mock(UserRepository.class);

	private final ApplicationEventPublisher eventPublisher
		= mock(ApplicationEventPublisher.class);

	private final AdminInviteService adminInviteService =
		new AdminInviteService(userRepository, eventPublisher);

	@Test
	@DisplayName("조직원 초대 시 사용자 저장과 이벤트가 발행된다.")
	void 조직원_초대시__이벤트_발행() {

		//given
		String email = "test@test.com";
		when(userRepository.existsByEmail(email)).thenReturn(false);
		InviteUserRequest request = new InviteUserRequest(email, Role.USER);

		//when
		adminInviteService.inviteUser(request);

		//then
		verify(userRepository, times(1)).save(any());
		verify(eventPublisher, times(1)).publishEvent(any(UserInvitedEvent.class));
	}

	@Test
	@DisplayName("이미 존재하는 이메일이면 예외가 발생한다.")
	void 이메일_중복이면__예외() {

		//given
		String email = "test@test.com";
		when(userRepository.existsByEmail(email)).thenReturn(true);
		InviteUserRequest request = new InviteUserRequest(email, Role.USER);

		//when & then
		assertThatThrownBy(() -> adminInviteService.inviteUser(request))
			.isInstanceOf(CustomException.class);

		verify(eventPublisher, never()).publishEvent(any());
	}
}