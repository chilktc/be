package be.greenroom.ticket.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketPreviewResponse (
	UUID ticketId,
	String name,
	LocalDateTime createdAt
){}
