package be.auth.dto.request;

import be.auth.jwt.Role;

public record InviteUserRequest(
	String email,
	Role role
)
{}