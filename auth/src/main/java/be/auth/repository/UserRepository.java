package be.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import be.auth.domain.OauthProvider;
import be.auth.domain.User;
import be.common.api.CustomException;
import be.common.api.ErrorCode;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByProviderAndProviderUserId(
		OauthProvider provider,
		String providerUserId
	);

	Optional<User> findByLoginIdAndProvider(
		String loginId,
		OauthProvider provider
	);

	boolean existsByLoginIdAndProvider(
		String loginId,
		OauthProvider provider
	);

	default User findByIdOrThrow(UUID id, ErrorCode errorCode) {
		return findById(id).orElseThrow(() -> new CustomException(errorCode));
	}

}
