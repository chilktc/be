package be.greenroom.ai.dto.response;

public record SessionCloseResponse(
	boolean success,
	String message
) {
}
