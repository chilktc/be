package be.notification.domain;

public enum GreenroomTemplateCode {
	GR_CHECK_IMMEDIATE,
	GR_CHECK_DAY3,
	GR_CHECK_DAY7,
	GR_CHECK_BIWEEKLY;

	public static GreenroomTemplateCode fromSequence(int sequence) {
		if (sequence == 1) {
			return GR_CHECK_IMMEDIATE;
		}
		if (sequence == 2) {
			return GR_CHECK_DAY3;
		}
		if (sequence == 3) {
			return GR_CHECK_DAY7;
		}
		return GR_CHECK_BIWEEKLY;
	}
}
