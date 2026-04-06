package be.auth.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import be.auth.domain.User;
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
		InviteUserRequest request = new InviteUserRequest(
			"테스트유저",
			email,
			"개발팀",
			"사원",
			Role.USER
		);

		//when
		adminInviteService.inviteUser(request);

		//then
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository, times(1)).save(userCaptor.capture());
		assertThat(userCaptor.getValue().getEmail()).isEqualTo(email);
		assertThat(userCaptor.getValue().getNickname()).isEqualTo("테스트유저");
		assertThat(userCaptor.getValue().getDepartment()).isEqualTo("개발팀");
		assertThat(userCaptor.getValue().getPosition()).isEqualTo("사원");
		verify(eventPublisher, times(1)).publishEvent(any(UserInvitedEvent.class));
	}

	@Test
	@DisplayName("이미 존재하는 이메일이면 예외가 발생한다.")
	void 이메일_중복이면__예외() {

		//given
		String email = "test@test.com";
		when(userRepository.existsByEmail(email)).thenReturn(true);
		InviteUserRequest request = new InviteUserRequest(
			"테스트유저",
			email,
			"개발팀",
			"사원",
			Role.USER
		);

		//when & then
		assertThatThrownBy(() -> adminInviteService.inviteUser(request))
			.isInstanceOf(CustomException.class);

		verify(eventPublisher, never()).publishEvent(any());
	}
}
