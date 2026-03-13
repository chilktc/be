package be.greenroom.ai.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.dto.response.TicketResponse;
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

	@Operation(
		summary = "AI 연동 입장권 생성",
		description = "세션 생성 → 팟캐스트 생성 → 세션 종료를 수행한 뒤 티켓을 생성합니다."
	)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResult<TicketResponse> createWithAi(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@RequestBody @Valid CreateTicketRequest request
	) {
		UUID userId = UUID.fromString(userIdHeader);
		return ApiResult.ok(ticketService.createWithAi(userId, request));
	}
}
