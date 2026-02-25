package be.greenroom.ticket.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
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

        return TicketResponse.from(ticketRepository.save(ticket));
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
}
