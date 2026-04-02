package be.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.auth.dto.request.UpdateOrganizationUserRequest;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

	private final UserRepository userRepository;

	public void updateOrganizationUser(
		UUID userId,
		UpdateOrganizationUserRequest request
	) {
		var user = userRepository.findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER);

		if (!user.getEmail().equals(request.email())
			&& userRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.EXIST_USER);
		}

		user.updateOrganizationInfo(
			request.name(),
			request.email(),
			request.department(),
			request.position(),
			request.role()
		);
	}

	public void deleteOrganizationUser(UUID userId) {
		var user = userRepository.findByIdOrThrow(userId, ErrorCode.NOT_FOUND_USER);
		user.delete();
	}
}
