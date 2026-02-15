package be.greenroom.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
    @NotBlank String situation,
    @NotBlank String thought,
    @NotBlank String action,
    String colleagueReaction
) {
}
