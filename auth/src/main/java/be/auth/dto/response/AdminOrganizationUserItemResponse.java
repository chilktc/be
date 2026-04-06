package be.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import be.auth.jwt.Role;

public record AdminOrganizationUserItemResponse(
	UUID userId,
	String name,
	String email,
	String department,
	String position,
	Role role,
	boolean isActive,
	LocalDateTime createdAt
) {
}
