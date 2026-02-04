package be.auth.domain;

import java.util.UUID;

import be.auth.jwt.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class User {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String loginId;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;


	public User(UUID id, String loginId, String password, Role role){
		this.id = id;
		this.loginId = loginId;
		this.password = password;
		this.role = role;
	}
}
