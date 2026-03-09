package be.auth.dto.request;

public record ConsentRequest(
	boolean agreedPrivacy // TODO: 약관별 동의 분리 예정
) {
}