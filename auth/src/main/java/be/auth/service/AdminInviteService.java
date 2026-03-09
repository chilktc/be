package be.auth.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.auth.domain.User;
import be.auth.dto.request.InviteUserRequest;
import be.auth.event.UserInvitedEvent;
import be.auth.repository.UserRepository;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminInviteService {

	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	public void inviteUser(InviteUserRequest request) {

		Preconditions.validate(
			!userRepository.existsByEmail(request.email()),
			ErrorCode.EXIST_USER
		);

		User user = User.invitedUserByAdmin(
			UUID.randomUUID(),
			request.email(),
			request.role()
		);

		userRepository.save(user);

		eventPublisher.publishEvent(
			new UserInvitedEvent(user.getId(), user.getEmail())
		);
	}


}