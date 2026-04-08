package be.greenroom.graph.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Check(constraints = "NOT (source_label = target_label AND source_grp = target_grp)")
@Table(
	name = "user_graph_edges",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_user_edge", columnNames = {
			"user_id", "source_label", "source_grp", "target_label", "target_grp"
		})
	},
	indexes = {
		@Index(name = "idx_user_edge_user_id", columnList = "user_id")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGraphEdge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "source_label", nullable = false, length = 200)
	private String sourceLabel;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_grp", nullable = false, length = 50)
	private GraphGroup sourceGroup;

	@Column(name = "target_label", nullable = false, length = 200)
	private String targetLabel;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_grp", nullable = false, length = 50)
	private GraphGroup targetGroup;

	@ColumnDefault("1")
	@Column(nullable = false)
	private int weight = 1;

	@ColumnDefault("'related'")
	@Column(nullable = false, length = 100)
	private String relationship = "related";

	@Column(name = "first_seen", nullable = false)
	private LocalDateTime firstSeen;

	@Column(name = "last_seen", nullable = false)
	private LocalDateTime lastSeen;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private UserGraphEdge(
		UUID userId,
		String sourceLabel,
		GraphGroup sourceGroup,
		String targetLabel,
		GraphGroup targetGroup,
		int weight,
		String relationship,
		LocalDateTime firstSeen,
		LocalDateTime lastSeen
	) {
		this.userId = userId;
		this.sourceLabel = sourceLabel;
		this.sourceGroup = sourceGroup;
		this.targetLabel = targetLabel;
		this.targetGroup = targetGroup;
		this.weight = weight;
		this.relationship = relationship;
		this.firstSeen = firstSeen;
		this.lastSeen = lastSeen;
	}

	public static UserGraphEdge create(
		UUID userId,
		String sourceLabel,
		GraphGroup sourceGroup,
		String targetLabel,
		GraphGroup targetGroup,
		int weight,
		String relationship,
		LocalDateTime firstSeen,
		LocalDateTime lastSeen
	) {
		return UserGraphEdge.builder()
			.userId(userId)
			.sourceLabel(sourceLabel)
			.sourceGroup(sourceGroup)
			.targetLabel(targetLabel)
			.targetGroup(targetGroup)
			.weight(weight)
			.relationship(relationship)
			.firstSeen(firstSeen)
			.lastSeen(lastSeen)
			.build();
	}

	public void updateCumulative(int weight, String relationship, LocalDateTime lastSeen) {
		this.weight = weight;
		this.relationship = relationship;
		this.lastSeen = lastSeen;
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
		if (firstSeen == null) {
			firstSeen = now;
		}
		if (lastSeen == null) {
			lastSeen = now;
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
