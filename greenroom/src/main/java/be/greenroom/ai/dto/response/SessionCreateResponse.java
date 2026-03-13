package be.greenroom.ai.dto.response;

public record SessionCreateResponse(
	String session_id,
	String mode,
	String created_at
) {
}
