package be.greenroom.tracking.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import be.greenroom.tracking.domain.ResolvedHelpType;
import be.greenroom.tracking.domain.ResolvedStateType;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.domain.UnresolvedBlockerType;
import be.greenroom.tracking.domain.UnresolvedNeedType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrackingHistoryItemResponse(
	TrackingStatus status,
	LocalDateTime trackedAt,
	String dDay,
	String note,
	ResolvedHelpType resolvedHelpType,
	ResolvedStateType resolvedStateType,
	UnresolvedBlockerType unresolvedBlockerType,
	UnresolvedNeedType unresolvedNeedType
) {
}
