package be.auth.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.auth.domain.User;
import be.auth.dto.request.ConsentRequest;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;

class UserConsentServiceTest {

	private final UserRepository userRepository
		= mock(UserRepository.class);

	private final UserConsentService userConsentService =
		new UserConsentService(userRepository);

	@Test
	@DisplayName("개인정보 동의 시 최초 로그인 상태가 해제된다.")
	void 개인정보_동의시__firstLogin_false로_변경() {

		//given
		UUID userId = UUID.randomUUID();
		User user = mock(User.class);

		when(userRepository.findById(userId))
			.thenReturn(Optional.of(user));
		when(user.isFirstLogin()).thenReturn(true);

		ConsentRequest request = new ConsentRequest(true);

		//when
		userConsentService.completeFirstLogin(userId, request);

		//then
		verify(user).completeFirstLogin();
	}

	@Test
	@DisplayName("이미 동의한 사용자는 예외가 발생한다.")
	void 이미_동의한_사용자__예외() {

		//given
		UUID userId = UUID.randomUUID();
		User user = mock(User.class);

		when(userRepository.findById(userId))
			.thenReturn(Optional.of(user));
		when(user.isFirstLogin()).thenReturn(false);

		ConsentRequest request = new ConsentRequest(true);

		//when & then
		assertThatThrownBy(() ->
			userConsentService.completeFirstLogin(userId, request)
		).isInstanceOf(CustomException.class);
	}

	@Test
	@DisplayName("개인정보 미동의 시 예외가 발생한다.")
	void 개인정보_미동의시__예외() {

		//given
		UUID userId = UUID.randomUUID();
		User user = mock(User.class);

		when(userRepository.findById(userId))
			.thenReturn(Optional.of(user));
		when(user.isFirstLogin()).thenReturn(true);

		ConsentRequest request = new ConsentRequest(false);

		//when & then
		assertThatThrownBy(() ->
			userConsentService.completeFirstLogin(userId, request)
		).isInstanceOf(CustomException.class);
	}

}