package be.auth.dto.response;

import be.auth.domain.ProfileImage;
import be.auth.domain.RecentProfileImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
@Builder
public class ProfileImageListResponse {
	private List<ImageDto> defaultImages;
	private List<ImageDto> characterImages;
	private List<ImageDto> recentImages;

	public static ProfileImageListResponse of(
		List<ProfileImage> defaultImages,
		List<ProfileImage> characterImages,
		List<RecentProfileImage> recentImages
	) {

		return ProfileImageListResponse.builder()
			.defaultImages(
				defaultImages.stream()
					.map(ImageDto::from)
					.toList()
			)
			.characterImages(
				characterImages.stream()
					.map(ImageDto::from)
					.toList()
			)
			.recentImages(
				recentImages.stream()
					.map(RecentProfileImage::getProfileImage)
					.filter(Objects::nonNull)
					.map(ImageDto::from)
					.toList()
			)
			.build();
	}

	@Getter
	@Builder
	public static class ImageDto {

		private Long id;
		private String imageUrl;

		public static ImageDto from(ProfileImage image) {
			return ImageDto.builder()
				.id(image.getId())
				.imageUrl(image.getImageUrl())
				.build();
		}
	}
}
