package be.greenroom.tracking.service;

import org.springframework.stereotype.Component;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.tracking.domain.ResolvedHelpType;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.domain.UnresolvedBlockerType;
import be.greenroom.tracking.dto.request.CreateTrackingRequest;

@Component
public class TrackingRequestValidator {

	public void validate(CreateTrackingRequest request) {
		if (request.status() == TrackingStatus.RESOLVED) {
			validateResolved(request);
			return;
		}
		validateUnresolved(request);
	}

	private void validateResolved(CreateTrackingRequest request) {
		if (request.resolvedHelpType() == null || request.resolvedStateType() == null) {
			throw new CustomException(ErrorCode.TRACKING_RESOLVED_FIELDS_REQUIRED);
		}
		if (request.unresolvedBlockerType() != null
			|| request.unresolvedBlockerOther() != null
			|| request.unresolvedNeedType() != null) {
			throw new CustomException(ErrorCode.TRACKING_UNRESOLVED_FIELDS_FORBIDDEN);
		}
		if (request.resolvedHelpType() == ResolvedHelpType.ETC
			&& isBlank(request.resolvedHelpOther())) {
			throw new CustomException(ErrorCode.TRACKING_RESOLVED_ETC_REQUIRED);
		}
		if (request.resolvedHelpType() != ResolvedHelpType.ETC
			&& request.resolvedHelpOther() != null) {
			throw new CustomException(ErrorCode.TRACKING_RESOLVED_ETC_FORBIDDEN);
		}
	}

	private void validateUnresolved(CreateTrackingRequest request) {
		if (request.unresolvedBlockerType() == null || request.unresolvedNeedType() == null) {
			throw new CustomException(ErrorCode.TRACKING_UNRESOLVED_FIELDS_REQUIRED);
		}
		if (request.resolvedHelpType() != null
			|| request.resolvedHelpOther() != null
			|| request.resolvedStateType() != null) {
			throw new CustomException(ErrorCode.TRACKING_RESOLVED_FIELDS_FORBIDDEN);
		}
		if (request.unresolvedBlockerType() == UnresolvedBlockerType.ETC
			&& isBlank(request.unresolvedBlockerOther())) {
			throw new CustomException(ErrorCode.TRACKING_UNRESOLVED_ETC_REQUIRED);
		}
		if (request.unresolvedBlockerType() != UnresolvedBlockerType.ETC
			&& request.unresolvedBlockerOther() != null) {
			throw new CustomException(ErrorCode.TRACKING_UNRESOLVED_ETC_FORBIDDEN);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
