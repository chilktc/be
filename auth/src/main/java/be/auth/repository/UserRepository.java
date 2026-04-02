package be.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import be.auth.dto.response.AdminOrganizationUserItemResponse;
import be.auth.domain.OauthProvider;
import be.auth.domain.User;
import be.common.api.CustomException;
import be.common.api.ErrorCode;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByProviderAndProviderUserId(
		OauthProvider provider,
		String providerUserId
	);

	Optional<User> findByEmail(String email);

	default User findByIdOrThrow(UUID id, ErrorCode errorCode) {
		return findById(id).orElseThrow(() -> new CustomException(errorCode));
	}

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	@Query(
		value = """
			select new be.auth.dto.response.AdminOrganizationUserItemResponse(
				u.id,
				u.nickname,
				u.email,
				u.department,
				u.position,
				u.role,
				u.isActive,
				u.createdAt
			)
			from User u
			where u.isDeleted = false
			order by u.createdAt desc, u.id desc
		""",
		countQuery = """
			select count(u)
			from User u
			where u.isDeleted = false
		"""
	)
	Page<AdminOrganizationUserItemResponse> findOrganizationUserPage(Pageable pageable);
}
