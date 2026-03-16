package be.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
	name = "recent_profile_image",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_recent_user_image",
			columnNames = {"user_id", "image_id"}
		)
	},
	indexes = {
		@Index(name = "idx_recent_user_used_at", columnList = "user_id, used_at")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecentProfileImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "image_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_recent_profile_image")
	)
	private ProfileImage profileImage;

	@Column(nullable = false)
	private LocalDateTime usedAt;

	public static RecentProfileImage create(UUID userId, ProfileImage profileImage) {
		return RecentProfileImage.builder()
			.userId(userId)
			.profileImage(profileImage)
			.usedAt(LocalDateTime.now())
			.build();
	}

	public void updateUsedAt() {
		this.usedAt = LocalDateTime.now();
	}
}
