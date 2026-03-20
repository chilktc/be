package be.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.auth.domain.User;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final RefreshTokenService refreshTokenService;

	public void deleteUser(UUID userId, String inputEmail) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

		Preconditions.validate(!user.isDeleted(), ErrorCode.ALREADY_DELETED_USER);
		Preconditions.validate(user.isActive(), ErrorCode.USER_DISABLED);

		if (!user.getEmail().equals(inputEmail)) {
			throw new CustomException(ErrorCode.INVALID_EMAIL);
		}

		user.delete();

		refreshTokenService.delete(user.getId());
	}
}
