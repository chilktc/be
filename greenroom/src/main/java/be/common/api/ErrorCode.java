package be.common.api;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에러입니다. 백엔드팀에 문의하세요."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	NOTIFICATION_EVENT_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 이벤트 직렬화에 실패했습니다."),
	DOES_NOT_EXIST_TICKET(HttpStatus.BAD_REQUEST, "존재하지 않는 티켓 id입니다."),
	NO_TICKET_ACCESS(HttpStatus.FORBIDDEN, "해당 티켓에 접근 권한이 없습니다."),
	ALREADY_RESOLVED_TICKET(HttpStatus.BAD_REQUEST, "이미 해결된 티켓입니다."),
	TRACKING_RESOLVED_FIELDS_REQUIRED(HttpStatus.BAD_REQUEST, "해결 상태에서는 해결 필드가 필수입니다."),
	TRACKING_UNRESOLVED_FIELDS_REQUIRED(HttpStatus.BAD_REQUEST, "미해결 상태에서는 미해결 필드가 필수입니다."),
	TRACKING_RESOLVED_FIELDS_FORBIDDEN(HttpStatus.BAD_REQUEST, "미해결 상태에서는 해결 필드를 보낼 수 없습니다."),
	TRACKING_UNRESOLVED_FIELDS_FORBIDDEN(HttpStatus.BAD_REQUEST, "해결 상태에서는 미해결 필드를 보낼 수 없습니다."),
	TRACKING_RESOLVED_ETC_REQUIRED(HttpStatus.BAD_REQUEST, "해결 도움 항목이 ETC면 resolvedHelpOther가 필요합니다."),
	TRACKING_UNRESOLVED_ETC_REQUIRED(HttpStatus.BAD_REQUEST, "미해결 걸림돌 항목이 ETC면 unresolvedBlockerOther가 필요합니다."),
	TRACKING_RESOLVED_ETC_FORBIDDEN(HttpStatus.BAD_REQUEST, "해결 도움 항목이 ETC가 아니면 resolvedHelpOther를 보낼 수 없습니다."),
	TRACKING_UNRESOLVED_ETC_FORBIDDEN(HttpStatus.BAD_REQUEST, "미해결 걸림돌 항목이 ETC가 아니면 unresolvedBlockerOther를 보낼 수 없습니다."),
	GRAPH_REQUEST_SCHEMA_MISMATCH(HttpStatus.BAD_REQUEST, "AI 서버가 보낸 그래프 요청 데이터 구조가 백엔드 스펙과 다릅니다."),
	GRAPH_RESPONSE_SCHEMA_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "그래프 응답 데이터 구조가 백엔드 응답 스펙과 다릅니다."),
	GRAPH_INVALID_REQUEST_TYPE(HttpStatus.BAD_REQUEST, "graph_nodes type은 graph_cumulative 이어야 합니다."),
	GRAPH_PAYLOAD_REQUIRED(HttpStatus.BAD_REQUEST, "그래프 요청 data payload는 필수입니다."),
	GRAPH_INVALID_GROUP(HttpStatus.BAD_REQUEST, "유효하지 않은 graph group 입니다."),
	GRAPH_INVALID_TREND(HttpStatus.BAD_REQUEST, "유효하지 않은 graph trend 입니다."),
	GRAPH_NODE_LABEL_REQUIRED(HttpStatus.BAD_REQUEST, "그래프 노드 label은 필수입니다."),
	GRAPH_EDGE_SOURCE_LABEL_REQUIRED(HttpStatus.BAD_REQUEST, "그래프 엣지 source_label은 필수입니다."),
	GRAPH_EDGE_TARGET_LABEL_REQUIRED(HttpStatus.BAD_REQUEST, "그래프 엣지 target_label은 필수입니다."),
	GRAPH_EDGE_RELATIONSHIP_REQUIRED(HttpStatus.BAD_REQUEST, "그래프 엣지 relationship은 필수입니다."),
	GRAPH_WEIGHT_REQUIRED(HttpStatus.BAD_REQUEST, "그래프 weight 값은 필수입니다."),
	GRAPH_MENTION_COUNT_REQUIRED(HttpStatus.BAD_REQUEST, "그래프 mention_count 값은 필수입니다."),
	GRAPH_ANALYSIS_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "그래프 분석 analysis_type은 필수입니다."),
	GRAPH_ANALYSIS_PAYLOAD_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "그래프 분석 payload 직렬화에 실패했습니다."),
	;
	private final HttpStatus status;
	private final String message;

}
