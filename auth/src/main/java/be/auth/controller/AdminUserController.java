package be.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.auth.dto.request.UpdateOrganizationUserRequest;
import be.auth.dto.response.AdminOrganizationUserPageResponse;
import be.auth.service.AdminUserQueryService;
import be.auth.service.AdminUserService;
import be.common.api.ApiResult;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin의 조직원 관리", description = "조직원 수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

	private final AdminUserService adminUserService;
	private final AdminUserQueryService adminUserQueryService;

	@Operation(
		summary = "조직원 목록 조회",
		description = "관리자가 삭제되지 않은 조직원 목록을 페이지 단위로 조회합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.ACCESS_DENIED
	})
	@GetMapping
	public ApiResult<AdminOrganizationUserPageResponse> getOrganizationUsers(
		@Parameter(hidden = true)
		@RequestHeader("X-User-Role") String role,
		@RequestParam(defaultValue = "0") int page
	) {
		if (!"ADMIN".equals(role)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		return ApiResult.ok(adminUserQueryService.getOrganizationUsers(page));
	}

	@Operation(
		summary = "조직원 정보 수정",
		description = "관리자가 조직원의 이름, 이메일, 부서, 직급, 권한을 수정합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.NOT_FOUND_USER,
		ErrorCode.EXIST_USER,
		ErrorCode.ACCESS_DENIED
	})
	@PutMapping("/{userId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> updateOrganizationUser(
		@Parameter(hidden = true)
		@RequestHeader("X-User-Role") String role,
		@PathVariable UUID userId,
		@RequestBody @Valid UpdateOrganizationUserRequest request
	) {
		if (!"ADMIN".equals(role)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		adminUserService.updateOrganizationUser(userId, request);
		return ApiResult.ok();
	}

	@Operation(
		summary = "조직원 삭제",
		description = "관리자가 조직원을 삭제 처리합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.NOT_FOUND_USER,
		ErrorCode.ACCESS_DENIED
	})
	@PutMapping("/{userId}/delete")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> deleteOrganizationUser(
		@Parameter(hidden = true)
		@RequestHeader("X-User-Role") String role,
		@PathVariable UUID userId
	) {
		if (!"ADMIN".equals(role)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		adminUserService.deleteOrganizationUser(userId);
		return ApiResult.ok();
	}
}
