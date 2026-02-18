package be.common.utils;

import be.common.api.CustomException;
import be.common.api.ErrorCode;

public class Preconditions {
	public static void validate(boolean expression, ErrorCode errorCode) {
		if (!expression) {
			throw new CustomException(errorCode);
		}
	}
}
