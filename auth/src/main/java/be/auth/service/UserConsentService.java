package be.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.auth.domain.User;
import be.auth.dto.request.ConsentRequest;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserConsentService {
	private final UserRepository userRepository;

	public void completeFirstLogin(UUID userId, ConsentRequest request) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

		Preconditions.validate(user.isFirstLogin(), ErrorCode.ALREADY_CONSENTED);
		Preconditions.validate(request.agreedPrivacy(), ErrorCode.PRIVACY_NOT_AGREED);

		user.completeFirstLogin();
	}
}