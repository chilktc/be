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
import org.hibernate.annotations.ColumnDefault;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "user_graph_nodes",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_user_label_grp", columnNames = {"user_id", "label", "grp"})
	},
	indexes = {
		@Index(name = "idx_user_id", columnList = "user_id"),
		@Index(name = "idx_user_grp", columnList = "user_id, grp")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGraphNode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(nullable = false, length = 200)
	private String label;

	@Enumerated(EnumType.STRING)
	@Column(name = "grp", nullable = false, length = 50)
	private GraphGroup group;

	@ColumnDefault("0.5")
	@Column(nullable = false)
	private double weight = 0.5d;

	@ColumnDefault("1")
	@Column(name = "mention_count", nullable = false)
	private int mentionCount = 1;

	@Enumerated(EnumType.STRING)
	@ColumnDefault("'STABLE'")
	@Column(nullable = false, length = 20)
	private GraphTrend trend = GraphTrend.STABLE;

	@Column(name = "first_seen", nullable = false)
	private LocalDateTime firstSeen;

	@Column(name = "last_seen", nullable = false)
	private LocalDateTime lastSeen;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private UserGraphNode(
		UUID userId,
		String label,
		GraphGroup group,
		double weight,
		int mentionCount,
		GraphTrend trend,
		LocalDateTime firstSeen,
		LocalDateTime lastSeen
	) {
		this.userId = userId;
		this.label = label;
		this.group = group;
		this.weight = weight;
		this.mentionCount = mentionCount;
		this.trend = trend;
		this.firstSeen = firstSeen;
		this.lastSeen = lastSeen;
	}

	public static UserGraphNode create(
		UUID userId,
		String label,
		GraphGroup group,
		double weight,
		int mentionCount,
		GraphTrend trend,
		LocalDateTime firstSeen,
		LocalDateTime lastSeen
	) {
		return UserGraphNode.builder()
			.userId(userId)
			.label(label)
			.group(group)
			.weight(weight)
			.mentionCount(mentionCount)
			.trend(trend)
			.firstSeen(firstSeen)
			.lastSeen(lastSeen)
			.build();
	}

	public void updateCumulative(double weight, int mentionCount, GraphTrend trend, LocalDateTime lastSeen) {
		this.weight = weight;
		this.mentionCount = mentionCount;
		this.trend = trend;
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
