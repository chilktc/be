package be.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import be.auth.domain.User;
import be.auth.jwt.Role;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserServiceTest {
	private final UserRepository userRepository = mock(UserRepository.class);
	private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);

	private final UserService userService =
		new UserService(userRepository, refreshTokenService);

	@Test
	@DisplayName("회원 탈퇴 성공")
	void 회원_탈퇴_성공() {
		// given
		UUID userId = UUID.randomUUID();
		User user = User.createServerUser(userId, "test@test.com", "pw", Role.USER);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when
		userService.deleteUser(userId, "test@test.com");

		// then
		assertThat(user.isDeleted()).isTrue();
		assertThat(user.isActive()).isFalse();
		assertThat(user.getEmail()).contains("_deleted_");

		verify(refreshTokenService).delete(userId);
	}

	@Test
	@DisplayName("이메일 불일치 시 탈퇴 실패")
	void 회원_탈퇴_이메일_불일치() {
		// given
		UUID userId = UUID.randomUUID();
		User user = User.createServerUser(userId, "test@test.com", "pw", Role.USER);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when & then
		assertThatThrownBy(() ->
			userService.deleteUser(userId, "wrong@test.com")
		).isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.INVALID_EMAIL);
	}

	@Test
	@DisplayName("이미 삭제된 유저 탈퇴 시도")
	void 회원_이미_삭제됨() {
		// given
		UUID userId = UUID.randomUUID();
		User user = User.createServerUser(userId, "test@test.com", "pw", Role.USER);
		user.delete();

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// when & then
		assertThatThrownBy(() ->
			userService.deleteUser(userId, "test@test.com")
		).isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.ALREADY_DELETED_USER);
	}
}
