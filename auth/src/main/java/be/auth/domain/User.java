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

	@Column(nullable = false)
	private String nickname;

	@Column
	private String image;

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
		String nickname,
		String image,
		OauthProvider provider,
		String providerUserId,
		String password,
		Role role,
		boolean isActive,
		boolean firstLogin
	) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.image = image;
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
			null,
			null,
			role,
			true,
			true
		);
	}

	public void bindGoogleOAuth(String googleSub, String googleName) {
		this.provider = OauthProvider.GOOGLE;
		this.providerUserId = googleSub;

		if (this.nickname == null) {
			this.nickname = googleName;
		}
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
			email,  // 서버 가입 유저는 이메일을 닉네임으로 사용
			null,
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


	public void changeNickname(String nickname) {
		Preconditions.validate(nickname != null && !nickname.isBlank(), ErrorCode.INVALID_NICKNAME);
		this.nickname = nickname;
	}

	public void changeImage(String image) {
		this.image = image;
	}
}
