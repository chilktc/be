package be.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.notification.service.GreenroomNotificationQueryService;
import be.notification.service.GreenroomNotificationQueryService.GreenroomNotificationSliceResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Greenroom Notification", description = "그린룸 알림 조회 API")
public class GreenroomNotificationQueryController {

	private final GreenroomNotificationQueryService notificationQueryService;

	@Operation(
		summary = "내 알림 조회",
		description = "최근 30일 알림을 커서 기반 무한 스크롤로 조회합니다."
	)
	@GetMapping
	public ApiResult<GreenroomNotificationSliceResponse> getNotifications(
		@Parameter(hidden = true)
		@RequestHeader("X-USER-ID") String userIdHeader,
		@Parameter(description = "다음 페이지 조회용 커서", example = "2026-03-25T08:30:00Z_0f8fad5b-d9cb-469f-a165-70867728950e")
		@RequestParam(required = false) String cursor,
		@Parameter(description = "페이지 크기", example = "20")
		@RequestParam(required = false) Integer size
	) {
		return ApiResult.ok(notificationQueryService.getNotifications(userIdHeader, cursor, size));
	}
}
