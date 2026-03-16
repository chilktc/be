package be.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import be.auth.dto.response.ProfileImageListResponse;
import be.auth.service.ProfileImageService;
import be.common.api.ApiResult;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "프로필 이미지", description = "프로필 이미지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/profile-images")
public class ProfileImageController {

	private final ProfileImageService profileImageService;

	@Operation(
		summary = "프로필 이미지 목록 조회",
		description = "대표 이미지, 캐릭터 이미지, 최근 사용 이미지를 조회합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.JWT_INVALID_TOKEN
	})
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<ProfileImageListResponse> getProfileImages(
		@Parameter(hidden = true)
		@RequestHeader(value = "X-User-Id", required = false) UUID userId
	) {

		if (userId == null) {
			throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);
		}

		return ApiResult.ok(profileImageService.getProfileImages(userId));
	}

	@Operation(
		summary = "프로필 이미지 변경",
		description = "사용자의 프로필 이미지를 변경합니다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.JWT_INVALID_TOKEN,
		ErrorCode.NOT_FOUND_USER,
		ErrorCode.NOT_FOUND_PROFILE_IMAGE
	})
	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> changeProfileImage(
		@Parameter(hidden = true)
		@RequestHeader(value = "X-User-Id", required = false) UUID userId,
		@RequestParam Long imageId
	) {

		if (userId == null) {
			throw new CustomException(ErrorCode.JWT_INVALID_TOKEN);
		}

		profileImageService.changeProfileImage(userId, imageId);

		return ApiResult.ok();
	}
}
