package be.greenroom.tracking.dto.request;

import be.greenroom.tracking.domain.ResolvedHelpType;
import be.greenroom.tracking.domain.ResolvedStateType;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.domain.UnresolvedBlockerType;
import be.greenroom.tracking.domain.UnresolvedNeedType;
import jakarta.validation.constraints.NotNull;

public record CreateTrackingRequest(
	@NotNull TrackingStatus status,
	ResolvedHelpType resolvedHelpType,
	String resolvedHelpOther,
	ResolvedStateType resolvedStateType,
	UnresolvedBlockerType unresolvedBlockerType,
	String unresolvedBlockerOther,
	UnresolvedNeedType unresolvedNeedType,
	String note
) {
}
