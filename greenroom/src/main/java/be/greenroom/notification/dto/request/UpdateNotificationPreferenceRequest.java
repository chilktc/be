package be.greenroom.notification.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateNotificationPreferenceRequest(
	@NotNull @Min(0) @Max(23) Integer preferredHour,
	@NotNull @Min(0) @Max(59) Integer preferredMinute,
	@NotBlank String timezone
) {
}
