package be.auth.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.auth.dto.request.InviteUserRequest;
import be.auth.jwt.Role;

@SpringBootTest
class InviteUserIntegrationTest {
	@Autowired
	private AdminInviteService adminInviteService;

	@MockitoBean
	private InvitedEmailService invitedEmailService;

	@MockitoBean
	private GoogleOauthService googleOauthService;

	@Test
	@DisplayName("조직원 초대 후 트랜잭션 커밋 시 이메일 전송 이벤트가 실행된다")
	void 조직원_초대_후__이메일_전송이_실행() {

		//given
		String email = UUID.randomUUID() + "@test.com";
		InviteUserRequest request = new InviteUserRequest(email, Role.USER);

		//when
		adminInviteService.inviteUser(request);

		//then
		verify(invitedEmailService, times(1))
			.sendInviteEmail(email);
	}
}