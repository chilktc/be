package be.greenroom.ai.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
@Table(name = "mind_frequencies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MindFrequency {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String sessionId;

	@Column(nullable = false, length = 1000)
	private String keywords;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private MindFrequency(String sessionId, String keywords, String description) {
		this.id = UUID.randomUUID();
		this.sessionId = sessionId;
		this.keywords = keywords;
		this.description = description;
	}

	public static MindFrequency create(String sessionId, List<String> keywords, String description) {
		return new MindFrequency(sessionId, String.join(",", keywords), description);
	}

	public void update(List<String> keywords, String description) {
		this.keywords = String.join(",", keywords);
		this.description = description;
	}

	public List<String> getKeywordsAsList() {
		if (keywords == null || keywords.isBlank()) {
			return List.of();
		}
		return Arrays.stream(keywords.split(","))
			.map(String::trim)
			.filter(value -> !value.isBlank())
			.toList();
	}

	@PrePersist
	public void prePersist() {
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
	}
}
