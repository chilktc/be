package be.common.api;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에러입니다. 백엔드팀에 문의하세요."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	GREENROOM_TEMPLATE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "그린룸 알림 템플릿을 찾을 수 없습니다."),
	MISSED_AFTER_3_MIN(HttpStatus.REQUEST_TIMEOUT, "알림 예정 시각으로부터 3분이 지나 전송 실패 처리되었습니다.")
	,;
	private final HttpStatus status;
	private final String message;

}
