package be.greenroom.tracking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import be.greenroom.notification.event.GreenroomNotificationEventType;
import be.greenroom.notification.event.GreenroomTicketResolvedEvent;
import be.greenroom.notification.service.GreenroomNotificationEventPublisher;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.TicketRepository;
import be.greenroom.tracking.domain.Tracking;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.dto.request.CreateTrackingRequest;
import be.greenroom.tracking.dto.response.TrackingHistoryItemResponse;
import be.greenroom.tracking.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrackingService {

	private final TicketRepository ticketRepository;
	private final TrackingRepository trackingRepository;
	private final GreenroomNotificationEventPublisher eventPublisher;
	private final TrackingRequestValidator trackingRequestValidator;

	@Transactional
	public void create(UUID userId, UUID ticketId, CreateTrackingRequest request) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		validateOwner(ticket, userId);
		validateAlreadyResolved(ticketId);
		trackingRequestValidator.validate(request);

		Tracking record = Tracking.builder()
			.ticketId(ticketId)
			.userId(userId)
			.status(request.status())
			.resolvedHelpType(request.resolvedHelpType())
			.resolvedHelpOther(request.resolvedHelpOther())
			.resolvedStateType(request.resolvedStateType())
			.unresolvedBlockerType(request.unresolvedBlockerType())
			.unresolvedBlockerOther(request.unresolvedBlockerOther())
			.unresolvedNeedType(request.unresolvedNeedType())
			.note(request.note())
			.build();
		trackingRepository.save(record);

		if (request.status() == TrackingStatus.RESOLVED) {
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

	@Transactional(readOnly = true)
	public List<TrackingHistoryItemResponse> getHistory(UUID userId, UUID ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		validateOwner(ticket, userId);

		LocalDate baseDate = ticket.getCreatedAt().toLocalDate();
		return trackingRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
			.stream()
			.map(record -> {
				long days = ChronoUnit.DAYS.between(baseDate, record.getCreatedAt().toLocalDate());
				return new TrackingHistoryItemResponse(
					record.getStatus(),
					record.getCreatedAt(),
					"D+" + days,
					record.getNote(),
					record.getResolvedHelpType(),
					record.getResolvedStateType(),
					record.getUnresolvedBlockerType(),
					record.getUnresolvedNeedType()
				);
			})
			.toList();
	}

	private void validateOwner(Ticket ticket, UUID userId) {
		Preconditions.validate(ticket.getUserId().equals(userId), ErrorCode.NO_TICKET_ACCESS);
	}

	private void validateAlreadyResolved(UUID ticketId) {
		Preconditions.validate(!trackingRepository.existsByTicketIdAndStatus(ticketId, TrackingStatus.RESOLVED), ErrorCode.ALREADY_RESOLVED_TICKET);
	}
}
