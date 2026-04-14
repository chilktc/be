package be.greenroom.tracking.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.tracking.domain.ResolvedHelpType;
import be.greenroom.tracking.domain.ResolvedStateType;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.domain.UnresolvedBlockerType;
import be.greenroom.tracking.domain.UnresolvedNeedType;
import be.greenroom.tracking.dto.request.CreateTrackingRequest;

class TrackingRequestValidatorTest {

	private final TrackingRequestValidator validator = new TrackingRequestValidator();

	@Test
	@DisplayName("해결 상태의 정상 요청은 검증을 통과한다")
	void 해결_정상요청_검증통과() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.RESOLVED,
			ResolvedHelpType.COMMUNICATION_RESOLVED,
			null,
			ResolvedStateType.FULLY_DONE,
			null,
			null,
			null,
			"정리됨"
		);

		// when then
		assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("해결 상태에서 해결 필드 누락 시 예외가 발생한다")
	void 해결_필수필드누락_예외() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.RESOLVED,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);

		// when then
		assertThatThrownBy(() -> validator.validate(request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getErrorCode())
			.isEqualTo(ErrorCode.TRACKING_RESOLVED_FIELDS_REQUIRED);
	}

	@Test
	@DisplayName("해결 상태에서 미해결 필드 포함 시 예외가 발생한다")
	void 해결_금지필드포함_예외() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.RESOLVED,
			ResolvedHelpType.COMMUNICATION_RESOLVED,
			null,
			ResolvedStateType.FULLY_DONE,
			UnresolvedBlockerType.HARD_TO_ACT,
			null,
			UnresolvedNeedType.NEED_SMALL_ACTION,
			null
		);

		// when then
		assertThatThrownBy(() -> validator.validate(request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getErrorCode())
			.isEqualTo(ErrorCode.TRACKING_UNRESOLVED_FIELDS_FORBIDDEN);
	}

	@Test
	@DisplayName("해결 상태에서 ETC 선택 후 기타값 없이도 검증을 통과한다")
	void 해결_ETC_기타값없어도_검증통과() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.RESOLVED,
			ResolvedHelpType.ETC,
			null,
			ResolvedStateType.MOSTLY_OK_SOMETIMES_RECALL,
			null,
			null,
			null,
			null
		);

		// when then
		assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("미해결 상태의 정상 요청은 검증을 통과한다")
	void 미해결_정상요청_검증통과() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.UNRESOLVED,
			null,
			null,
			null,
			UnresolvedBlockerType.HARD_TO_ACT,
			null,
			UnresolvedNeedType.NEED_SMALL_ACTION,
			"아직 힘듦"
		);

		// when then
		assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("미해결 상태에서 미해결 필드 누락 시 예외가 발생한다")
	void 미해결_필수필드누락_예외() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.UNRESOLVED,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);

		// when then
		assertThatThrownBy(() -> validator.validate(request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getErrorCode())
			.isEqualTo(ErrorCode.TRACKING_UNRESOLVED_FIELDS_REQUIRED);
	}

	@Test
	@DisplayName("미해결 상태에서 해결 필드 포함 시 예외가 발생한다")
	void 미해결_금지필드포함_예외() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.UNRESOLVED,
			ResolvedHelpType.COMMUNICATION_RESOLVED,
			null,
			ResolvedStateType.FULLY_DONE,
			UnresolvedBlockerType.HARD_TO_ACT,
			null,
			UnresolvedNeedType.NEED_SMALL_ACTION,
			null
		);

		// when then
		assertThatThrownBy(() -> validator.validate(request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getErrorCode())
			.isEqualTo(ErrorCode.TRACKING_RESOLVED_FIELDS_FORBIDDEN);
	}

	@Test
	@DisplayName("미해결 상태에서 ETC 선택 후 기타값 없이도 검증을 통과한다")
	void 미해결_ETC_기타값없어도_검증통과() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.UNRESOLVED,
			null,
			null,
			null,
			UnresolvedBlockerType.ETC,
			null,
			UnresolvedNeedType.NEED_SMALL_ACTION,
			null
		);

		// when then
		assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("해결 상태에서 기타값을 보내면 예외가 발생한다")
	void 해결_기타값전달_예외() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.RESOLVED,
			ResolvedHelpType.ETC,
			"사용자 입력",
			ResolvedStateType.MOSTLY_OK_SOMETIMES_RECALL,
			null,
			null,
			null,
			null
		);

		// when then
		assertThatThrownBy(() -> validator.validate(request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getErrorCode())
			.isEqualTo(ErrorCode.TRACKING_RESOLVED_ETC_FORBIDDEN);
	}

	@Test
	@DisplayName("미해결 상태에서 기타값을 보내면 예외가 발생한다")
	void 미해결_기타값전달_예외() {
		// given
		CreateTrackingRequest request = new CreateTrackingRequest(
			TrackingStatus.UNRESOLVED,
			null,
			null,
			null,
			UnresolvedBlockerType.ETC,
			"사용자 입력",
			UnresolvedNeedType.NEED_SMALL_ACTION,
			null
		);

		// when then
		assertThatThrownBy(() -> validator.validate(request))
			.isInstanceOf(CustomException.class)
			.extracting(e -> ((CustomException)e).getErrorCode())
			.isEqualTo(ErrorCode.TRACKING_UNRESOLVED_ETC_FORBIDDEN);
	}
}
