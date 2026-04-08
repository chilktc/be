package be.greenroom.graph.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "graph_analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraphAnalysis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "episode_id", length = 100)
	private String episodeId;

	@Column(name = "session_id", length = 100)
	private String sessionId;

	@Column(name = "analysis_type", nullable = false, length = 50)
	private String analysisType;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String payload;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	private GraphAnalysis(
		UUID userId,
		String episodeId,
		String sessionId,
		String analysisType,
		String payload
	) {
		this.userId = userId;
		this.episodeId = episodeId;
		this.sessionId = sessionId;
		this.analysisType = analysisType;
		this.payload = payload;
	}

	public static GraphAnalysis create(
		UUID userId,
		String episodeId,
		String sessionId,
		String analysisType,
		String payload
	) {
		return GraphAnalysis.builder()
			.userId(userId)
			.episodeId(episodeId)
			.sessionId(sessionId)
			.analysisType(analysisType)
			.payload(payload)
			.build();
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
