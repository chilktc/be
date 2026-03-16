package be.auth.service;

import be.auth.domain.ProfileImage;
import be.auth.domain.ProfileImageType;
import be.auth.domain.RecentProfileImage;
import be.auth.domain.User;
import be.auth.dto.response.ProfileImageListResponse;
import be.auth.jwt.Role;
import be.auth.repository.ProfileImageRepository;
import be.auth.repository.RecentProfileImageRepository;
import be.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProfileImageServiceTest {
	@Autowired
	private ProfileImageService profileImageService;

	@Autowired
	private ProfileImageRepository profileImageRepository;

	@Autowired
	private RecentProfileImageRepository recentProfileImageRepository;

	@Autowired
	private UserRepository userRepository;

	private User createUser() {
		User user = User.createServerUser(
			UUID.randomUUID(),
			"test@test.com",
			"password",
			Role.USER
		);
		return userRepository.save(user);
	}

	private ProfileImage createImage(ProfileImageType type) {
		return profileImageRepository.save(
			ProfileImage.builder()
				.imageUrl("/image.png")
				.type(type)
				.build()
		);
	}

	@Test
	@DisplayName("프로필 이미지 목록 조회 테스트")
	void 프로필_이미지_목록_조회() {

		// given
		User user = createUser();

		for (int i = 0; i < 3; i++) {
			createImage(ProfileImageType.DEFAULT);
			createImage(ProfileImageType.CHARACTER);
		}

		// when
		ProfileImageListResponse response =
			profileImageService.getProfileImages(user.getId());

		// then
		assertThat(response.getDefaultImages()).hasSize(3);
		assertThat(response.getCharacterImages()).hasSize(3);
	}

	@Test
	@DisplayName("프로필 이미지 변경 시 최근 이미지 생성")
	void 프로필_이미지_변경_최근이미지_생성() {

		// given
		User user = createUser();
		ProfileImage image = createImage(ProfileImageType.DEFAULT);

		// when
		profileImageService.changeProfileImage(user.getId(), image.getId());

		// then
		List<RecentProfileImage> recent =
			recentProfileImageRepository.findTop10ByUserIdOrderByUsedAtDesc(user.getId());

		assertThat(recent).hasSize(1);
		assertThat(recent.get(0).getProfileImage().getId()).isEqualTo(image.getId());
	}

	@Test
	@DisplayName("동일 이미지 재선택 시 usedAt 업데이트")
	void 동일이미지_재선택_usedAt_업데이트() {

		// given
		User user = createUser();
		ProfileImage image = createImage(ProfileImageType.DEFAULT);

		profileImageService.changeProfileImage(user.getId(), image.getId());

		RecentProfileImage before =
			recentProfileImageRepository
				.findByUserIdAndProfileImage_Id(user.getId(), image.getId())
				.orElseThrow();

		// when
		profileImageService.changeProfileImage(user.getId(), image.getId());

		// then
		RecentProfileImage after =
			recentProfileImageRepository
				.findByUserIdAndProfileImage_Id(user.getId(), image.getId())
				.orElseThrow();

		assertThat(after.getUsedAt()).isAfterOrEqualTo(before.getUsedAt());
	}

	@Test
	@DisplayName("최근 이미지 10개 초과 시 오래된 데이터 삭제")
	void 최근이미지_10개_초과_삭제() {

		// given
		User user = createUser();

		for (int i = 0; i < 11; i++) {
			ProfileImage image = createImage(ProfileImageType.DEFAULT);
			profileImageService.changeProfileImage(user.getId(), image.getId());
		}

		// when
		List<RecentProfileImage> result =
			recentProfileImageRepository.findTop11ByUserIdOrderByUsedAtDesc(user.getId());

		// then
		assertThat(result.size()).isLessThanOrEqualTo(10);
	}

	@Test
	@DisplayName("최근 이미지 목록 조회")
	void 최근이미지_조회() {

		// given
		User user = createUser();

		for (int i = 0; i < 3; i++) {
			ProfileImage image = createImage(ProfileImageType.DEFAULT);
			profileImageService.changeProfileImage(user.getId(), image.getId());
		}

		// when
		ProfileImageListResponse response =
			profileImageService.getProfileImages(user.getId());

		// then
		assertThat(response.getRecentImages()).hasSize(3);
	}

	@Test
	@DisplayName("이미지 타입별 필터링 정확성 테스트")
	void 이미지타입_필터링() {

		// given
		User user = createUser();

		createImage(ProfileImageType.DEFAULT);
		createImage(ProfileImageType.DEFAULT);
		createImage(ProfileImageType.CHARACTER);

		// when
		ProfileImageListResponse response =
			profileImageService.getProfileImages(user.getId());

		// then
		assertThat(response.getDefaultImages()).hasSize(2);
		assertThat(response.getCharacterImages()).hasSize(1);
	}

	@Test
	@DisplayName("최근 이미지 최신순 정렬")
	void 최근이미지_정렬() {

		User user = createUser();

		ProfileImage img1 = createImage(ProfileImageType.DEFAULT);
		ProfileImage img2 = createImage(ProfileImageType.DEFAULT);

		profileImageService.changeProfileImage(user.getId(), img1.getId());
		profileImageService.changeProfileImage(user.getId(), img2.getId());

		List<RecentProfileImage> list =
			recentProfileImageRepository.findTop10ByUserIdOrderByUsedAtDesc(user.getId());

		assertThat(list.get(0).getProfileImage().getId()).isEqualTo(img2.getId());
	}

}