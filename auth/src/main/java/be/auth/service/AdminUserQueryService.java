package be.auth.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.auth.dto.response.AdminOrganizationUserPageResponse;
import be.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserQueryService {

	private static final int PAGE_SIZE = 9;

	private final UserRepository userRepository;

	public AdminOrganizationUserPageResponse getOrganizationUsers(int page) {
		var result = userRepository.findOrganizationUserPage(
			PageRequest.of(
				page,
				PAGE_SIZE,
				Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
			)
		);

		return new AdminOrganizationUserPageResponse(
			result.getContent(),
			result.getNumber(),
			result.getSize(),
			result.getTotalElements(),
			result.getTotalPages(),
			result.hasNext()
		);
	}
}
