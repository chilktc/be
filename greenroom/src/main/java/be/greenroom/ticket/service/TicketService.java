package be.greenroom.ticket.service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.notification.service.GreenroomNotificationEventPublisher;
import be.greenroom.notification.event.GreenroomNotificationEventType;
import be.greenroom.notification.event.GreenroomTicketCreatedEvent;
import be.greenroom.notification.event.GreenroomTicketResolvedEvent;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.dto.response.TicketPreviewResponse;
import be.greenroom.ticket.dto.response.TicketResponse;
import be.greenroom.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
	private final GreenroomNotificationEventPublisher eventPublisher;

    @Transactional
    public TicketResponse create(UUID userId, CreateTicketRequest request) {
		// TODO : AI에게 요청 보내 받아옴
		// request -> response로 변경되어 AI의 응답을 저장
        Ticket ticket = Ticket.create(
            userId,
			UUID.randomUUID().toString(), // 임시 랜덤 이름
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
        return TicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketPreviewResponse> getMyTicketPreviews(UUID userId) {
        return ticketRepository.findNameAndCreatedAtByUserIdOrderByCreatedAtDesc(userId)
			.stream()
			.map(dao -> new TicketPreviewResponse(
				dao.ticketId(),
				dao.name(),
				dao.createdAt()
			))
			.toList();
    }

	@Transactional(readOnly = true)
	public TicketResponse getTicket(UUID ticketId){
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		return TicketResponse.from(ticket);
	}

	@Transactional
	public void resolveTicket(UUID userId, UUID ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		if (!ticket.getUserId().equals(userId)) {
			throw new CustomException(ErrorCode.NO_TICKET_ACCESS);
		}
		GreenroomTicketResolvedEvent event = new GreenroomTicketResolvedEvent(
			UUID.randomUUID(),
			GreenroomNotificationEventType.GREENROOM_TICKET_RESOLVED.name(),
			LocalDateTime.now(),
			ticketId,
			userId
		);
		eventPublisher.publish(userId.toString(), event);
	}
}
