package be.greenroom.ai.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.greenroom.ai.dto.request.StorySelectionRequest;
import be.greenroom.ai.dto.response.MindFrequencyResponse;
import be.greenroom.ai.service.AiStorySelectionService;
import be.greenroom.ai.service.MindFrequencyService;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "AI Ticket", description = "AI 연동 티켓 생성 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/ai/tickets")
public class AiTicketController {

	private final TicketService ticketService;
	private final MindFrequencyService mindFrequencyService;
	private final AiStorySelectionService aiStorySelectionService;

	@Operation(
		summary = "AI 연동 입장권 생성",
		description = "세션 생성 후 세션 ID를 응답 헤더로 반환하고, 팟캐스트 생성 및 티켓 저장은 비동기로 처리합니다."
	)
	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ResponseEntity<ApiResult<Void>> createWithAi(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@RequestBody @Valid CreateTicketRequest request
	) {
		UUID userId = UUID.fromString(userIdHeader);
		String sessionId = ticketService.createWithAi(userId, request);
		return ResponseEntity.status(HttpStatus.ACCEPTED)
			.header("X-AI-Session-Id", sessionId)
			.body(ApiResult.ok());
	}

	@Operation(summary = "마음 주파수 조회", description = "헤더의 sessionId로 마음 주파수를 조회합니다.")
	@GetMapping("/mind-frequencies")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<MindFrequencyResponse> getMindFrequency(
		@RequestHeader("X-AI-Session-Id") @NotBlank String sessionId
	) {
		return ApiResult.ok(mindFrequencyService.getBySessionId(sessionId));
	}

	@Operation(summary = "선택한 스토리 전달", description = "헤더의 sessionId와 요청의 storyId를 합쳐 AI 서버에 전달합니다.")
	@PostMapping("/story-selection")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> selectStory(
		@RequestHeader("X-AI-Session-Id") @NotBlank String sessionId,
		@RequestBody @Valid StorySelectionRequest request
	) {
		aiStorySelectionService.sendSelection(sessionId, request);
		return ApiResult.ok();
	}
}
