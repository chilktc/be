package be.notification.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.notification.dto.response.GreenroomNotificationTrackingResponse;
import be.notification.service.GreenroomNotificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "본인의 알림(이메일) 내역 조회", description = "프론트에서 이메일 알림 검증을 하기위한 개발용 api입니다.")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/notification/greenroom")
public class GreenroomNotificationQueryController {

	private final GreenroomNotificationQueryService queryService;

	@Operation(summary = "내역 조회", description = "전송, 알림, 기록 등 이메일 관련 내역를 통합 조회합니다.")
	@GetMapping("/users/tracking")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<GreenroomNotificationTrackingResponse> getTrackingByUserId(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader
	) {
		return ApiResult.ok(queryService.getTrackingByUserId(UUID.fromString(userIdHeader)));
	}
}
