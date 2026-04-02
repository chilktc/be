package be.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import be.auth.dto.response.AdminOrganizationUserItemResponse;
import be.auth.jwt.Role;
import be.auth.repository.UserRepository;

class AdminUserQueryServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);

	private final AdminUserQueryService adminUserQueryService =
		new AdminUserQueryService(userRepository);

	@Test
	@DisplayName("조직원 목록 조회는 페이지 크기 9로 고정된다")
	void 조직원_목록_조회_성공() {
		AdminOrganizationUserItemResponse item =
			new AdminOrganizationUserItemResponse(
				UUID.randomUUID(),
				"홍길동",
				"hong@test.com",
				"개발팀",
				"대리",
				Role.USER,
				true,
				LocalDateTime.of(2026, 3, 31, 9, 0)
			);
		PageImpl<AdminOrganizationUserItemResponse> page = new PageImpl<>(
			List.of(item),
			PageRequest.of(1, 9),
			10
		);

		when(userRepository.findOrganizationUserPage(org.mockito.ArgumentMatchers.any(Pageable.class)))
			.thenReturn(page);

		var response = adminUserQueryService.getOrganizationUsers(1);

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(userRepository).findOrganizationUserPage(pageableCaptor.capture());
		assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(9);
		assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
		assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();

		assertThat(response.items()).hasSize(1);
		assertThat(response.page()).isEqualTo(1);
		assertThat(response.size()).isEqualTo(9);
		assertThat(response.totalElements()).isEqualTo(10);
		assertThat(response.totalPages()).isEqualTo(2);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.items().get(0).userId()).isNotNull();
		assertThat(response.items().get(0).name()).isEqualTo("홍길동");
	}
}
