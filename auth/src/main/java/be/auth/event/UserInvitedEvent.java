package be.auth.event;

import java.util.UUID;

public record UserInvitedEvent(
	UUID userId,
	String email
) {
}