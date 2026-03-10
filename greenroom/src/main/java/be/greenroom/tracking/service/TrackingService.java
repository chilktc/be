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
import be.greenroom.notification.event.GreenroomNotificationEventType;
import be.greenroom.notification.event.GreenroomTicketResolvedEvent;
import be.greenroom.notification.service.GreenroomNotificationEventPublisher;
import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.repository.TicketRepository;
import be.greenroom.tracking.domain.ResolvedHelpType;
import be.greenroom.tracking.domain.Tracking;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.domain.UnresolvedBlockerType;
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

	@Transactional
	public void create(UUID userId, UUID ticketId, CreateTrackingRequest request) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException(ErrorCode.DOES_NOT_EXIST_TICKET));
		validateOwner(ticket, userId);
		validateAlreadyResolved(ticketId);
		validateRequest(request);

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
		if (!ticket.getUserId().equals(userId)) {
			throw new CustomException(ErrorCode.NO_TICKET_ACCESS);
		}
	}

	private void validateRequest(CreateTrackingRequest request) {
		if (request.status() == TrackingStatus.RESOLVED) {
			boolean hasResolved = request.resolvedHelpType() != null && request.resolvedStateType() != null;
			boolean hasUnresolved = request.unresolvedBlockerType() != null
				|| request.unresolvedBlockerOther() != null
				|| request.unresolvedNeedType() != null;
			if (!hasResolved || hasUnresolved) {
				throw new CustomException(ErrorCode.INVALID_TRACKING_REQUEST);
			}
			if (request.resolvedHelpType() == ResolvedHelpType.ETC
				&& (request.resolvedHelpOther() == null || request.resolvedHelpOther().isBlank())) {
				throw new CustomException(ErrorCode.TRACKING_ETC_CONTENT_REQUIRED);
			}
			if (request.resolvedHelpType() != ResolvedHelpType.ETC && request.resolvedHelpOther() != null) {
				throw new CustomException(ErrorCode.INVALID_TRACKING_REQUEST);
			}
			return;
		}

		if (request.status() == TrackingStatus.UNRESOLVED) {
			boolean hasResolved = request.resolvedHelpType() != null
				|| request.resolvedHelpOther() != null
				|| request.resolvedStateType() != null;
			boolean hasUnresolved = request.unresolvedBlockerType() != null && request.unresolvedNeedType() != null;
			if (hasResolved || !hasUnresolved) {
				throw new CustomException(ErrorCode.INVALID_TRACKING_REQUEST);
			}
			if (request.unresolvedBlockerType() == UnresolvedBlockerType.ETC
				&& (request.unresolvedBlockerOther() == null || request.unresolvedBlockerOther().isBlank())) {
				throw new CustomException(ErrorCode.TRACKING_ETC_CONTENT_REQUIRED);
			}
			if (request.unresolvedBlockerType() != UnresolvedBlockerType.ETC
				&& request.unresolvedBlockerOther() != null) {
				throw new CustomException(ErrorCode.INVALID_TRACKING_REQUEST);
			}
		}
	}

	private void validateAlreadyResolved(UUID ticketId) {
		if (trackingRepository.existsByTicketIdAndStatus(ticketId, TrackingStatus.RESOLVED)) {
			throw new CustomException(ErrorCode.ALREADY_RESOLVED_TICKET);
		}
	}
}
