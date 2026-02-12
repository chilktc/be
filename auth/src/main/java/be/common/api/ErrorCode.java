package be.common.api;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에러입니다. 백엔드팀에 문의하세요."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
	EXIST_LOGINID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다."),
	EXIST_USER(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
	FAIL_LOGIN(HttpStatus.UNAUTHORIZED, "아이디 혹은 비밀번호가 일치하지 않습니다."),
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호를 입력하세요."),
	DOES_NOT_MATCH_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호가 일치하지 않습니다."),
	CAN_NOT_ALLOWED_SAME_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
	ACCOUNT_INACTIVATED(HttpStatus.FORBIDDEN, "비활성화 된 계정입니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "다시 로그인 해주세요."),
	OAUTH_INVALID_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 OAuth 인증 코드입니다."),
	OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.UNAUTHORIZED, "OAuth 토큰 교환에 실패했습니다."),
	OAUTH_USERINFO_FAILED(HttpStatus.UNAUTHORIZED, "OAuth 사용자 정보를 가져오지 못했습니다."),
	OAUTH_EMAIL_NOT_REGISTERED(HttpStatus.FORBIDDEN, "이메일로 등록된 사용자가 없습니다."),
	OAUTH_PROVIDER_MISMATCH(HttpStatus.FORBIDDEN, "OAuth 제공자 정보가 일치하지 않습니다.")
	,;
	private final HttpStatus status;
	private final String message;

}