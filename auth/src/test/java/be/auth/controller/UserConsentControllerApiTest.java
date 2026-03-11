package be.auth.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import be.auth.service.AuthNotificationPreferenceService;
import be.auth.service.UserConsentService;

class UserConsentControllerApiTest {

	@Mock
	private UserConsentService userConsentService;
	@Mock
	private AuthNotificationPreferenceService authNotificationPreferenceService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		UserConsentController controller = new UserConsentController(
			userConsentService,
			authNotificationPreferenceService
		);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	@DisplayName("개인정보 동의 완료 시 firstLogin 완료와 알림 초기화 이벤트 발행을 수행한다")
	void 동의완료시_알림초기화_수행() throws Exception {
		// given
		UUID userId = UUID.randomUUID();
		String body = """
			{
			  "agreedPrivacy": true
			}
			""";

		// when then
		mockMvc.perform(
				post("/auth/consent")
					.header("X-User-Id", userId.toString())
					.contentType(MediaType.APPLICATION_JSON)
					.content(body)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("ok"));

		verify(userConsentService).completeFirstLogin(userId, new be.auth.dto.request.ConsentRequest(true));
		verify(authNotificationPreferenceService).initializeAndPublish(userId);
	}
}
