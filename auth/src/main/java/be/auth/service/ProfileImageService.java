package be.auth.service;

import be.auth.domain.ProfileImage;
import be.auth.domain.ProfileImageType;
import be.auth.domain.RecentProfileImage;
import be.auth.domain.User;
import be.auth.dto.response.ProfileImageListResponse;
import be.auth.repository.ProfileImageRepository;
import be.auth.repository.RecentProfileImageRepository;
import be.auth.repository.UserRepository;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileImageService {
	private final ProfileImageRepository profileImageRepository;
	private final RecentProfileImageRepository recentProfileImageRepository;
	private final UserRepository userRepository;


	@Transactional(readOnly = true)
	public ProfileImageListResponse getProfileImages(UUID userId) {

		List<ProfileImage> defaultImages =
			profileImageRepository.findTop10ByType(ProfileImageType.DEFAULT);

		List<ProfileImage> characterImages =
			profileImageRepository.findTop10ByType(ProfileImageType.CHARACTER);

		List<RecentProfileImage> recentImages =
			recentProfileImageRepository.findTop10ByUserIdOrderByUsedAtDesc(userId);

		return ProfileImageListResponse.of(
			defaultImages,
			characterImages,
			recentImages
		);
	}

	@Transactional
	public void changeProfileImage(UUID userId, Long imageId) {

		ProfileImage image = profileImageRepository.findById(imageId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE_IMAGE));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

		RecentProfileImage recent = recentProfileImageRepository
			.findByUserIdAndProfileImage_Id(userId, imageId)
			.orElse(null);

		if (recent != null) {
			recent.updateUsedAt();
		} else {
			recentProfileImageRepository.save(
				RecentProfileImage.create(userId, image)
			);
		}

		trimRecentImages(userId);
		user.changeImage(image);
	}

	private void trimRecentImages(UUID userId) {

		List<RecentProfileImage> images =
			recentProfileImageRepository.findTop11ByUserIdOrderByUsedAtDesc(userId);

		if (images.size() > 10) {
			recentProfileImageRepository.delete(images.get(10));
		}
	}
}
