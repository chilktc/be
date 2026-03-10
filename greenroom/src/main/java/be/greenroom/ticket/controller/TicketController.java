package be.greenroom.ticket.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExample;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.dto.response.TicketPreviewPageResponse;
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
	@Operation(summary = "그린룸 입장권 이름 조회", description = "무한스크롤용으로 본인의 티켓 목록을 조회합니다.")
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<TicketPreviewPageResponse> getMyTickets(
        @RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@RequestParam(required = false) String cursorCreatedAt,
		@RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = UUID.fromString(userIdHeader);
		LocalDateTime cursor = cursorCreatedAt == null ? null : LocalDateTime.parse(cursorCreatedAt);
        return ApiResult.ok(ticketService.getMyTicketPreviews(userId, cursor, size));
    }

	@Operation(summary = "그린룸 입장권 단건 조회", description = "ticketId로 그린룸 입장권 단건을 조회합니다.")
	@ApiErrorCodeExample(ErrorCode.NO_TICKET_ACCESS)
	@GetMapping("/{ticketId}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<TicketResponse> getTicket(
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@PathVariable UUID ticketId
	) {
		UUID userId = UUID.fromString(userIdHeader);
		return ApiResult.ok(ticketService.getTicket(userId, ticketId));
	}
}
