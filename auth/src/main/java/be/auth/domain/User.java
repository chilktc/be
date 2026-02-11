package be.auth.domain;

import java.util.UUID;

import be.auth.jwt.Role;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
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
			name = "uk_provider_user",
			columnNames = {"provider", "provider_user_id"}
		)
	}
)
public class User {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	private OauthProvider provider;

	@Column(name = "provider_user_id")
	private String providerUserId;

	@Column
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(nullable = false)
	private boolean isActive;

	@Column(nullable = false)
	private boolean firstLogin;


	private User(
		UUID id,
		String email,
		OauthProvider provider,
		String providerUserId,
		String password,
		Role role,
		boolean isActive,
		boolean firstLogin
	) {
		this.id = id;
		this.email = email;
		this.provider = provider;
		this.providerUserId = providerUserId;
		this.password = password;
		this.role = role;
		this.isActive = isActive;
		this.firstLogin = firstLogin;
	}

	public static User invitedUserByAdmin(
		UUID id,
		String email,
		Role role
	) {
		return new User(
			id,
			email,
			null,
			null,
			null,
			role,
			true,
			true
		);
	}

	public void bindGoogleOAuth(String googleSub) {
		this.provider = OauthProvider.GOOGLE;
		this.providerUserId = googleSub;
	}


	public static User createServerUser(
		UUID id,
		String email,
		String encodedPassword,
		Role role
	){
		Preconditions.validate(encodedPassword != null, ErrorCode.INVALID_PASSWORD);
		return new User(
			id,
			email,
			OauthProvider.SERVER,
			email,
			encodedPassword,
			role,
			true,
			false
		);
	}

	//TODO: 어드민 조직원 초대 API
	public void completeFirstLogin() {
		this.firstLogin = false;
	}
}
