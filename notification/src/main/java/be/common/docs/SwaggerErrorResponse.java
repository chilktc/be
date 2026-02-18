package be.common.docs;

import be.common.api.ErrorCode;
import lombok.Getter;

@Getter
public class SwaggerErrorResponse {
	private final String code;
	private final String message;

	// 기본 생성자
	private SwaggerErrorResponse(ErrorCode errorCode) {
		this.code = errorCode.getStatus().series().name();
		this.message = errorCode.getMessage();
	}

	// Exception과 함께 메시지 추가
	private SwaggerErrorResponse(ErrorCode errorCode, Exception e) {
		this.code = errorCode.getStatus().toString();
		this.message = errorCode.getMessage() + " - " + e.getMessage();
	}

	// 커스텀 메시지 추가
	private SwaggerErrorResponse(ErrorCode errorCode, String message) {
		this.code = errorCode.getStatus().toString();
		this.message = errorCode.getMessage() + " - " + message;
	}

	// 정적 팩토리 메서드
	public static SwaggerErrorResponse from(ErrorCode errorCode) {
		return new SwaggerErrorResponse(errorCode);
	}

	public static SwaggerErrorResponse of(ErrorCode errorCode, Exception e) {
		return new SwaggerErrorResponse(errorCode, e);
	}

	public static SwaggerErrorResponse of(ErrorCode errorCode, String message) {
		return new SwaggerErrorResponse(errorCode, message);
	}

}
