package be.greenroom.tracking.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tracking {
	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, updatable = false)
	private UUID ticketId;

	@Column(nullable = false, updatable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TrackingStatus status;

	@Enumerated(EnumType.STRING)
	@Column
	private ResolvedHelpType resolvedHelpType;

	@Column
	private String resolvedHelpOther;

	@Enumerated(EnumType.STRING)
	@Column
	private ResolvedStateType resolvedStateType;

	@Enumerated(EnumType.STRING)
	@Column
	private UnresolvedBlockerType unresolvedBlockerType;

	@Column
	private String unresolvedBlockerOther;

	@Enumerated(EnumType.STRING)
	@Column
	private UnresolvedNeedType unresolvedNeedType;

	@Column
	private String note;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	private Tracking(
		UUID ticketId,
		UUID userId,
		TrackingStatus status,
		ResolvedHelpType resolvedHelpType,
		String resolvedHelpOther,
		ResolvedStateType resolvedStateType,
		UnresolvedBlockerType unresolvedBlockerType,
		String unresolvedBlockerOther,
		UnresolvedNeedType unresolvedNeedType,
		String note
	) {
		this.id = UUID.randomUUID();
		this.ticketId = ticketId;
		this.userId = userId;
		this.status = status;
		this.resolvedHelpType = resolvedHelpType;
		this.resolvedHelpOther = resolvedHelpOther;
		this.resolvedStateType = resolvedStateType;
		this.unresolvedBlockerType = unresolvedBlockerType;
		this.unresolvedBlockerOther = unresolvedBlockerOther;
		this.unresolvedNeedType = unresolvedNeedType;
		this.note = note;
	}

	@PrePersist
	public void prePersist() {
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
	}
}
