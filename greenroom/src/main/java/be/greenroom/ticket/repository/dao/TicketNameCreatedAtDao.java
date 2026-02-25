package be.greenroom.ticket.repository.dao;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketNameCreatedAtDao (
	UUID ticketId,
	String name,
	LocalDateTime createdAt
){}
