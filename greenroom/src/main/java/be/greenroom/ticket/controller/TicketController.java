package be.greenroom.ticket.controller;

import java.util.List;
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
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.dto.response.TicketPreviewResponse;
import be.greenroom.ticket.dto.response.TicketResponse;
import be.greenroom.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "그린룸 입장권", description = "그린룸 입장권 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/greenroom/tickets")
public class TicketController {

    private final TicketService ticketService;

	@Operation(summary = "그린룸 입장권 생성", description = "그린룸 입장권을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<TicketResponse> create(
        @RequestHeader("X-User-Id") @NotBlank String userIdHeader,
        @RequestBody @Valid CreateTicketRequest request
    ) {
        UUID userId = UUID.fromString(userIdHeader);
        return ApiResult.ok(ticketService.create(userId, request));
    }

	// TODO : 페이징 필요시 추가
	@Operation(summary = "그린룸 입장권 이름 조회", description = "본인의 그린룸 입장권 이름과 시간을 조회합니다.")
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<List<TicketPreviewResponse>> getMyTickets(
        @RequestHeader("X-User-Id") @NotBlank String userIdHeader
    ) {
        UUID userId = UUID.fromString(userIdHeader);
        return ApiResult.ok(ticketService.getMyTicketPreviews(userId));
    }

	@Operation(summary = "그린룸 입장권 단건 조회", description = "ticketId로 그린룸 입장권 단건을 조회합니다.")
	@GetMapping("/{ticketId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<TicketResponse> getTicket(
		@PathVariable UUID ticketId
	) {
		return ApiResult.ok(ticketService.getTicket(ticketId));
	}

	@Operation(summary = "그린룸 티켓 해결 처리", description = "해결 완료된 티켓은 알림 발송 대상에서 제외됩니다.")
	@PostMapping("/{ticketId}/resolve")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> resolveTicket(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@PathVariable UUID ticketId
	) {
		ticketService.resolveTicket(UUID.fromString(userIdHeader), ticketId);
		return ApiResult.ok();
	}
}
