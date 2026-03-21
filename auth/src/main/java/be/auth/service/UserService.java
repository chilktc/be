package be.auth.service;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.auth.domain.User;
import be.auth.dto.request.UpdateNicknameRequest;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final RefreshTokenService refreshTokenService;

	@Transactional
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

	private static final Pattern PATTERN =
		Pattern.compile("^[a-zA-Z0-9가-힣_]+$");

	private void validate(String nickname) {
		Preconditions.validate(
			PATTERN.matcher(nickname).matches(),
			ErrorCode.INVALID_NICKNAME_FORMAT
		);
	}

	@Transactional
	public void updateNickname(UUID userId, UpdateNicknameRequest request) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

		String nickname = request.nickname();

		validate(nickname);

		if (!user.getNickname().equals(nickname)
			&& userRepository.existsByNickname(nickname)) {
			throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
		}

		user.changeNickname(nickname);
	}
}
