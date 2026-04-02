package be.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.auth.domain.User;
import be.auth.dto.request.UpdateOrganizationUserRequest;
import be.auth.jwt.Role;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;

class AdminUserServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);

	private final AdminUserService adminUserService =
		new AdminUserService(userRepository);

	@Test
	@DisplayName("관리자가 조직원 정보를 수정할 수 있다")
	void 조직원_정보_수정_성공() {
		UUID userId = UUID.randomUUID();
		User user = User.invitedUserByAdmin(
			userId,
			"기존이름",
			"before@test.com",
			"디자인팀",
			"사원",
			Role.USER
		);
		UpdateOrganizationUserRequest request = new UpdateOrganizationUserRequest(
			"새이름",
			"after@test.com",
			"개발팀",
			"대리",
			Role.ADMIN
		);

		when(userRepository.findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER))
			.thenReturn(user);
		when(userRepository.existsByEmail(request.email())).thenReturn(false);

		adminUserService.updateOrganizationUser(userId, request);

		verify(userRepository).findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER);
		assertThat(user.getNickname()).isEqualTo("새이름");
		assertThat(user.getEmail()).isEqualTo("after@test.com");
		assertThat(user.getDepartment()).isEqualTo("개발팀");
		assertThat(user.getPosition()).isEqualTo("대리");
		assertThat(user.getRole()).isEqualTo(Role.ADMIN);
	}

	@Test
	@DisplayName("존재하지 않는 조직원 수정 시 예외가 발생한다")
	void 존재하지_않는_조직원_수정_실패() {
		UUID userId = UUID.randomUUID();
		UpdateOrganizationUserRequest request = new UpdateOrganizationUserRequest(
			"새이름",
			"after@test.com",
			"개발팀",
			"대리",
			Role.USER
		);

		when(userRepository.findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER))
			.thenThrow(new CustomException(ErrorCode.NOT_FOUND_USER));

		assertThatThrownBy(() -> adminUserService.updateOrganizationUser(userId, request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.NOT_FOUND_USER);
	}

	@Test
	@DisplayName("다른 사용자가 이미 쓰는 이메일로 수정하면 예외가 발생한다")
	void 이메일_중복_수정_실패() {
		UUID userId = UUID.randomUUID();
		User user = User.invitedUserByAdmin(
			userId,
			"기존이름",
			"before@test.com",
			"디자인팀",
			"사원",
			Role.USER
		);
		UpdateOrganizationUserRequest request = new UpdateOrganizationUserRequest(
			"새이름",
			"duplicate@test.com",
			"개발팀",
			"대리",
			Role.USER
		);

		when(userRepository.findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER))
			.thenReturn(user);
		when(userRepository.existsByEmail(request.email())).thenReturn(true);

		assertThatThrownBy(() -> adminUserService.updateOrganizationUser(userId, request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException) e).getErrorCode())
			.isEqualTo(ErrorCode.EXIST_USER);
	}

	@Test
	@DisplayName("관리자가 조직원을 삭제 처리하면 isDeleted만 true로 변경된다")
	void 조직원_삭제_성공() {
		UUID userId = UUID.randomUUID();
		User user = User.invitedUserByAdmin(
			userId,
			"기존이름",
			"before@test.com",
			"디자인팀",
			"사원",
			Role.USER
		);

		when(userRepository.findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER))
			.thenReturn(user);

		adminUserService.deleteOrganizationUser(userId);

		assertThat(user.isDeleted()).isTrue();
		assertThat(user.isActive()).isFalse();
	}
}
