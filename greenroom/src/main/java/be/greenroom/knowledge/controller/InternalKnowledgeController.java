package be.greenroom.knowledge.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.common.api.ErrorCode;
import be.common.docs.ApiErrorCodeExamples;
import be.greenroom.knowledge.dto.request.CreateKnowledgeRequest;
import be.greenroom.knowledge.dto.response.KnowledgeItemResponse;
import be.greenroom.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Internal Knowledge", description = "지식 문서 내부 조회/적재 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/internal/knowledge")
public class InternalKnowledgeController {

	private final KnowledgeService knowledgeService;

	@Operation(summary = "지식 문서 원문 조회", description = "chunk id 목록으로 지식 문서 원문과 메타데이터를 조회합니다.")
	@ApiErrorCodeExamples({
		ErrorCode.KNOWLEDGE_IDS_REQUIRED,
		ErrorCode.KNOWLEDGE_IDS_LIMIT_EXCEEDED
	})
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<List<KnowledgeItemResponse>> getKnowledge(
		@RequestParam String ids
	) {
		return ApiResult.ok(knowledgeService.getByIds(ids));
	}

	@Operation(summary = "지식 문서 단일 청크 적재", description = "지식 문서 청크 원문을 저장합니다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResult<Void> createKnowledge(
		@RequestBody @Valid CreateKnowledgeRequest request
	) {
		knowledgeService.save(request);
		return ApiResult.ok();
	}
}
