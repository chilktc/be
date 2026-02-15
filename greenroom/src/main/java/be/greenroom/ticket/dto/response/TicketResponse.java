package be.greenroom.ticket.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import be.greenroom.ticket.domain.Ticket;

public record TicketResponse(
    UUID id,
    UUID userId,
    String situation,
    String thought,
    String action,
    String colleagueReaction,
    LocalDateTime createdAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getUserId(),
            ticket.getSituation(),
            ticket.getThought(),
            ticket.getAction(),
            ticket.getColleagueReaction(),
            ticket.getCreatedAt()
        );
    }
}
