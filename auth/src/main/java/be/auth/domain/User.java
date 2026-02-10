package be.auth.domain;

import java.util.UUID;

import be.auth.jwt.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
	name = "user",
	uniqueConstraints = {
		@UniqueConstraint(
			columnNames = {"provider", "provider_user_id"}
		)
	}
)
public class User {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OauthProvider provider;

	@Column(name = "provider_user_id", nullable = false)
	private String providerUserId;

	@Column(unique = true)
	private String loginId;

	@Column
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;


	private User(UUID id, OauthProvider provider, String providerUserId, String loginId, String password, Role role){
		this.id = id;
		this.provider = provider;
		this.providerUserId = providerUserId;
		this.loginId = loginId;
		this.password = password;
		this.role = role;
	}

	public static User createGoogleUser(
		UUID id,
		String googleSub,
		Role role
	){
		return new User(
			id,
			OauthProvider.GOOGLE,
			googleSub,
			null,
			null,
			role
		);
	}

	public static User createServerUser(
		UUID id,
		String loginId,
		String encodedPassword,
		Role role
	){
		return new User(
			id,
			OauthProvider.SERVER,
			loginId,
			loginId,
			encodedPassword,
			role
		);
	}
}
