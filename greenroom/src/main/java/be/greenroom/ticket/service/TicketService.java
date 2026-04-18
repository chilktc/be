package be.greenroom.ticket.service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import be.greenroom.ai.repository.AiIngestRequestLogRepository;
import be.greenroom.ai.repository.MindFrequencyRepository;
import be.greenroom.ai.repository.PodcastRepository;
import be.greenroom.ai.client.AiServerClient;
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
import be.greenroom.tracking.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {

	private final TicketRepository ticketRepository;
	private final GreenroomNotificationEventPublisher eventPublisher;
	private final AiServerClient aiServerClient;
	private final AiSessionRedisService aiSessionRedisService;
	private final AsyncTicketCreateService asyncTicketCreateService;
	private final TrackingRepository trackingRepository;
	private final MindFrequencyRepository mindFrequencyRepository;
	private final PodcastRepository podcastRepository;
	private final AiIngestRequestLogRepository aiIngestRequestLogRepository;

    @Transactional
    public TicketResponse create(UUID userId, CreateTicketRequest request) {
		Ticket saved = saveAndPublishTicket(userId, request, UUID.randomUUID().toString());
		return TicketResponse.from(saved);
    }

	@Transactional
	public String createWithAi(UUID userId, CreateTicketRequest request) {
		log.info("[AI_TICKET] createWithAi start userId={}", userId);
		log.info("[AI_TICKET] requesting AI session creation for userId={}", userId);
		SessionCreateResponse session = aiServerClient.createSession(new SessionCreateRequest(userId, "podcast"));
		log.info("[AI_TICKET] AI session response userId={}, session={}", userId, session);
		Preconditions.validate(session != null && session.sessionId() != null, ErrorCode.INTERNAL_SERVER_ERROR);

		String sessionId = session.sessionId();
		log.info("[AI_TICKET] session created userId={}, sessionId={}", userId, sessionId);
		log.info("[AI_TICKET] deleting previous AI session cache userId={}", userId);
		aiSessionRedisService.delete(userId);
		log.info("[AI_TICKET] saving AI session cache userId={}, sessionId={}", userId, sessionId);
		aiSessionRedisService.save(userId, sessionId, Duration.ofHours(1));

		log.info("[AI_TICKET] dispatching async podcast creation userId={}, sessionId={}", userId, sessionId);
		asyncTicketCreateService.createPodcastAndSaveTicket(userId, sessionId, request);
		log.info("[AI_TICKET] createWithAi end userId={}, sessionId={}", userId, sessionId);
		return sessionId;
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

	@Transactional
	public void deleteTicket(UUID userId, UUID ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		Preconditions.validate(userId.equals(ticket.getUserId()), ErrorCode.NO_TICKET_ACCESS);

		String sessionId = ticket.getSessionId();

		trackingRepository.deleteAllByTicketId(ticketId);
		mindFrequencyRepository.deleteByTicketId(ticketId);

		if (sessionId != null && !sessionId.isBlank()) {
			podcastRepository.deleteBySessionId(sessionId);
			aiIngestRequestLogRepository.deleteAllBySessionId(sessionId);
			aiSessionRedisService.deleteCompleted(sessionId);
			aiSessionRedisService.deleteIfMatches(userId, sessionId);
		}

		ticketRepository.delete(ticket);
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

}
