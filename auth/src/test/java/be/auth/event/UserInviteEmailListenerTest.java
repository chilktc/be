package be.auth.event;

import static org.mockito.Mockito.*;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.auth.service.InvitedEmailService;

class UserInviteEmailListenerTest {

	private final InvitedEmailService invitedEmailService
		= mock(InvitedEmailService.class);

	private final UserInviteEmailListener listener =
		new UserInviteEmailListener(invitedEmailService);

	@Test
	@DisplayName("초대 이벤트 발생 시 이메일 전송이 실행된다.")
	void 초대_이벤트_발생시__이메일_전송() {

		//given
		String email = "invite@test.com";

		UserInvitedEvent event =
			new UserInvitedEvent(UUID.randomUUID(), email);

		//when
		listener.handleUserInvited(event);

		//then
		verify(invitedEmailService, times(1))
			.sendInviteEmail(email);
	}

}