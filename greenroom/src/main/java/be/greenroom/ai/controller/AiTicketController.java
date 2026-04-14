package be.greenroom.ai.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.greenroom.ai.dto.request.StorySelectionRequest;
import be.greenroom.ai.dto.response.AiTicketCreateResponse;
import be.greenroom.ai.dto.response.MindFrequencyResponse;
import be.greenroom.ai.dto.response.PodcastResponse;
import be.greenroom.ai.service.AiStorySelectionService;
import be.greenroom.ai.service.MindFrequencyService;
import be.greenroom.ai.service.PodcastService;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "AI Ticket", description = "AI 연동 티켓 생성 API")
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/greenroom/ai/tickets")
public class AiTicketController {

	private final TicketService ticketService;
	private final MindFrequencyService mindFrequencyService;
	private final PodcastService podcastService;
	private final AiStorySelectionService aiStorySelectionService;

	@Operation(
		summary = "AI 연동 입장권 생성",
		description = "세션 생성 후 세션 ID를 응답 body로 반환하고, 팟캐스트 생성 및 티켓 저장은 비동기로 처리합니다."
	)
	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ApiResult<AiTicketCreateResponse> createWithAi(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@RequestBody @Valid CreateTicketRequest request
	) {
		log.info(
			"[AI_TICKET] createWithAi request received userIdHeader={}, situationLength={}, thoughtLength={}, actionLength={}, hasColleagueReaction={}",
			userIdHeader,
			request.situation() != null ? request.situation().length() : 0,
			request.thought() != null ? request.thought().length() : 0,
			request.action() != null ? request.action().length() : 0,
			request.colleagueReaction() != null && !request.colleagueReaction().isBlank()
		);
		UUID userId = UUID.fromString(userIdHeader);
		log.info("[AI_TICKET] parsed userId={}", userId);
		String sessionId = ticketService.createWithAi(userId, request);
		log.info("[AI_TICKET] createWithAi completed userId={}, sessionId={}", userId, sessionId);
		return ApiResult.ok(new AiTicketCreateResponse(sessionId));
	}

	@Operation(summary = "마음 주파수 조회", description = "헤더의 sessionId로 마음 주파수를 조회합니다.")
	@GetMapping("/mind-frequencies")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<MindFrequencyResponse> getMindFrequency(
		@RequestHeader("X-AI-Session-Id") @NotBlank String sessionId
	) {
		return ApiResult.ok(mindFrequencyService.getBySessionId(sessionId));
	}

	@Operation(summary = "마음 주파수 조회 by ticketId", description = "ticketId로 마음 주파수를 조회하며, 트래킹이 이미 완료되었다면 에러를 반환합니다.")
	@GetMapping("/mind-frequencies/{ticketId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<MindFrequencyResponse> getMindFrequencyByTicketId(
		@PathVariable UUID ticketId
	) {
		return ApiResult.ok(mindFrequencyService.getByTicketId(ticketId));
	}

	@Operation(summary = "팟캐스트 조회", description = "헤더의 sessionId로 팟캐스트를 조회합니다.")
	@GetMapping("/podcast")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<PodcastResponse> getPodcast(
		@RequestHeader("X-AI-Session-Id") @NotBlank String sessionId
	) {
		return ApiResult.ok(podcastService.getBySessionId(sessionId));
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
