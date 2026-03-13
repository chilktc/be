package be.greenroom.ticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import be.greenroom.ai.client.AiServerClient;
import be.greenroom.ai.dto.request.PodcastEpisodeRequest;
import be.greenroom.ai.dto.request.SessionCloseRequest;
import be.greenroom.ai.dto.request.SessionCreateRequest;
import be.greenroom.ai.dto.response.SessionCreateResponse;
import be.greenroom.notification.service.GreenroomNotificationEventPublisher;
import be.greenroom.notification.event.GreenroomNotificationEventType;
import be.greenroom.notification.event.GreenroomTicketCreatedEvent;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.dto.response.TicketPreviewPageResponse;
import be.greenroom.ticket.dto.response.TicketPreviewResponse;
import be.greenroom.ticket.dto.response.TicketResponse;
import be.greenroom.ticket.repository.TicketRepository;
import be.greenroom.ticket.repository.dao.TicketPreviewDao;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
	private final GreenroomNotificationEventPublisher eventPublisher;
	private final AiServerClient aiServerClient;

    @Transactional
    public TicketResponse create(UUID userId, CreateTicketRequest request) {
		Ticket saved = saveAndPublishTicket(userId, request, UUID.randomUUID().toString());
		return TicketResponse.from(saved);
    }

	@Transactional
	public TicketResponse createWithAi(UUID userId, CreateTicketRequest request) {
		SessionCreateResponse session = aiServerClient.createSession(new SessionCreateRequest(userId, "podcast"));
		Preconditions.validate(session != null && session.session_id() != null, ErrorCode.INTERNAL_SERVER_ERROR);

		String sessionId = session.session_id();
		try {
			aiServerClient.createPodcastEpisode(
				new PodcastEpisodeRequest(
					userId,
					sessionId,
					request.situation(),
					buildDescription(request),
					Map.of(),
					Map.of("source", "greenroom")
				)
			);
			Ticket saved = saveAndPublishTicket(userId, request, request.situation());
			return TicketResponse.from(saved);
		} finally {
			aiServerClient.closeSession(
				sessionId,
				new SessionCloseRequest(userId, sessionId, "ticket-created")
			);
		}
    }

	@Transactional(readOnly = true)
	public TicketPreviewPageResponse getMyTicketPreviews(
		UUID userId,
		LocalDateTime cursorCreatedAt,
		int size
	) {
		int pageSize = Math.min(Math.max(size, 1), 50);

		List<TicketPreviewDao> rows = ticketRepository.findTicketSlice(
			userId,
			cursorCreatedAt,
			PageRequest.of(0, pageSize + 1)
		);

		boolean hasNext = rows.size() > pageSize;
		List<TicketPreviewDao> content = hasNext ? rows.subList(0, pageSize) : rows;

		List<TicketPreviewResponse> items = content.stream()
			.map(row -> new TicketPreviewResponse(row.ticketId(), row.name(), row.createdAt()))
			.toList();

		if (items.isEmpty()) {
			return new TicketPreviewPageResponse(items, false, null);
		}

		TicketPreviewResponse last = items.get(items.size() - 1);
		return new TicketPreviewPageResponse(
			items,
			hasNext,
			hasNext ? last.createdAt().toString() : null
		);
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicket(UUID userId, UUID ticketId){
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		Preconditions.validate(userId.equals(ticket.getUserId()), ErrorCode.NO_TICKET_ACCESS);
		return TicketResponse.from(ticket);
	}

	private Ticket saveAndPublishTicket(UUID userId, CreateTicketRequest request, String ticketName) {
		Ticket ticket = Ticket.create(
			userId,
			ticketName,
			request.situation(),
			request.thought(),
			request.action(),
			request.colleagueReaction()
		);

		Ticket saved = ticketRepository.save(ticket);
		GreenroomTicketCreatedEvent event = new GreenroomTicketCreatedEvent(
			UUID.randomUUID(),
			GreenroomNotificationEventType.GREENROOM_TICKET_CREATED.name(),
			LocalDateTime.now(),
			saved.getId(),
			saved.getUserId(),
			saved.getCreatedAt()
		);
		eventPublisher.publish(saved.getUserId().toString(), event);
		return saved;
	}

	private String buildDescription(CreateTicketRequest request) {
		return "thought: " + request.thought()
			+ "\naction: " + request.action()
			+ "\ncolleagueReaction: " + (request.colleagueReaction() == null ? "" : request.colleagueReaction());
	}
}
