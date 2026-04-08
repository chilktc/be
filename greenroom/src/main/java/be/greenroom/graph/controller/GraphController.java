package be.greenroom.graph.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import be.greenroom.graph.dto.request.CreateGraphAnalysisRequest;
import be.greenroom.graph.dto.request.PutGraphNodesRequest;
import be.greenroom.graph.dto.response.GraphNodesResponse;
import be.greenroom.graph.dto.response.GraphUserDataResponse;
import be.greenroom.graph.service.GraphCommandService;
import be.greenroom.graph.service.GraphQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Tag(name = "Graph", description = "그래프 누적 데이터 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class GraphController {

	private final GraphQueryService graphQueryService;
	private final GraphCommandService graphCommandService;

	@Operation(
		summary = "기존 누적 그래프 조회(BE -> AI)",
		description = "AI 서버의 EMA 계산용 기존 누적 데이터를 반환한다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.VALIDATION_ERROR,
		ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
	})
	@GetMapping("/graph_nodes")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<GraphNodesResponse> getGraphNodes(@RequestParam("user_id") @NotNull UUID userId) {
		return ApiResult.ok(graphQueryService.getCumulativeGraph(userId));
	}

	@Operation(
		summary = "누적 그래프 저장(AI -> BE)",
		description = "AI 서버가 계산한 누적 그래프를 저장한다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.GRAPH_REQUEST_SCHEMA_MISMATCH,
		ErrorCode.GRAPH_INVALID_REQUEST_TYPE,
		ErrorCode.GRAPH_PAYLOAD_REQUIRED,
		ErrorCode.GRAPH_INVALID_GROUP,
		ErrorCode.GRAPH_INVALID_TREND,
		ErrorCode.GRAPH_NODE_LABEL_REQUIRED,
		ErrorCode.GRAPH_EDGE_SOURCE_LABEL_REQUIRED,
		ErrorCode.GRAPH_EDGE_TARGET_LABEL_REQUIRED,
		ErrorCode.GRAPH_EDGE_RELATIONSHIP_REQUIRED,
		ErrorCode.GRAPH_WEIGHT_REQUIRED,
		ErrorCode.GRAPH_MENTION_COUNT_REQUIRED
	})
	@PutMapping("/graph_nodes")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> upsertGraphNodes(@RequestBody @Valid PutGraphNodesRequest request) {
		graphCommandService.upsertCumulativeGraph(request);
		return ApiResult.ok();
	}

	@Operation(
		summary = "그래프 분석 저장(AI -> BE)",
		description = "에피소드 단위 그래프 분석 원본을 저장한다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.GRAPH_REQUEST_SCHEMA_MISMATCH,
		ErrorCode.GRAPH_PAYLOAD_REQUIRED,
		ErrorCode.GRAPH_ANALYSIS_TYPE_REQUIRED,
		ErrorCode.GRAPH_ANALYSIS_PAYLOAD_SERIALIZATION_FAILED
	})
	@PostMapping("/graph_analyses")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResult<Void> createGraphAnalysis(@RequestBody @Valid CreateGraphAnalysisRequest request) {
		graphCommandService.saveGraphAnalysis(request);
		return ApiResult.ok();
	}

	@Operation(
		summary = "사용자 그래프 조회(FE)",
		description = "프론트엔드 시각화용 누적 그래프를 사용자 단위로 반환한다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.VALIDATION_ERROR,
		ErrorCode.ACCESS_DENIED,
		ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
	})
	@GetMapping("/graph/users/{userId}/data")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<GraphUserDataResponse> getUserGraphData(
		@Parameter(hidden = true)
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@Parameter(hidden = true)
		@RequestHeader("X-User-Role") @NotBlank String role,
		@PathVariable @NotNull UUID userId
	) {
		validateAdminAccess(userIdHeader, role);
		return ApiResult.ok(graphQueryService.getGraphVisualizationData(userId));
	}

	@Operation(
		summary = "조직 그래프 조회(FE)",
		description = "활성 사용자 전체를 조직 구성원으로 간주하여 프론트엔드 시각화용 누적 그래프를 반환한다."
	)
	@ApiErrorCodeExamples({
		ErrorCode.ACCESS_DENIED,
		ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
	})
	@GetMapping("/graph/organization/data")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<GraphUserDataResponse> getOrganizationGraphData(
		@Parameter(hidden = true)
		@RequestHeader("X-User-Id") @NotBlank String userIdHeader,
		@Parameter(hidden = true)
		@RequestHeader("X-User-Role") @NotBlank String role
	) {
		validateAdminAccess(userIdHeader, role);
		return ApiResult.ok(graphQueryService.getOrganizationGraphVisualizationData());
	}

	private void validateAdminAccess(String userIdHeader, String role) {
		UUID.fromString(userIdHeader);

		if (!"ADMIN".equals(role)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}
}
