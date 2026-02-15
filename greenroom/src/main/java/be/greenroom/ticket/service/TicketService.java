package be.greenroom.ticket.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.greenroom.ticket.domain.Ticket;
import be.greenroom.ticket.dto.request.CreateTicketRequest;
import be.greenroom.ticket.dto.response.TicketResponse;
import be.greenroom.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public TicketResponse create(UUID userId, CreateTicketRequest request) {
        Ticket ticket = Ticket.create(
            userId,
            request.situation(),
            request.thought(),
            request.action(),
            request.colleagueReaction()
        );

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(UUID userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(TicketResponse::from)
            .toList();
    }
}
