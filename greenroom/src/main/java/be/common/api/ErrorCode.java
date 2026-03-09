package be.common.api;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에러입니다. 백엔드팀에 문의하세요."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	NOTIFICATION_EVENT_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 이벤트 직렬화에 실패했습니다."),
	DOES_NOT_EXIST_TICKET(HttpStatus.BAD_REQUEST, "존재하지 않는 티켓 id입니다."),
	NO_TICKET_ACCESS(HttpStatus.FORBIDDEN, "해당 티켓에 접근 권한이 없습니다."),
	;
	private final HttpStatus status;
	private final String message;

}
