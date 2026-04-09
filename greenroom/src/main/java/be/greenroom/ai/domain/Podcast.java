package be.greenroom.ai.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "podcasts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Podcast {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false)
	private String sessionId;

	@Column
	private String imageUrl;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String text;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private Podcast(UUID userId, String sessionId, String imageUrl, String text) {
		this.id = UUID.randomUUID();
		this.userId = userId;
		this.sessionId = sessionId;
		this.imageUrl = imageUrl;
		this.text = text;
	}

	public static Podcast create(UUID userId, String sessionId, String imageUrl, String text) {
		return new Podcast(userId, sessionId, imageUrl, text);
	}

	@PrePersist
	public void prePersist() {
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
	}
}
