package be.greenroom.knowledge.service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.knowledge.domain.KnowledgeBase;
import be.greenroom.knowledge.dto.request.CreateKnowledgeRequest;
import be.greenroom.knowledge.dto.response.KnowledgeItemResponse;
import be.greenroom.knowledge.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

	private static final int MAX_IDS = 10;

	private final KnowledgeBaseRepository knowledgeBaseRepository;

	@Transactional
	public void save(CreateKnowledgeRequest request) {
		KnowledgeBase knowledgeBase = KnowledgeBase.builder()
			.id(request.id())
			.title(request.title())
			.content(request.content())
			.page(request.page())
			.source(request.source())
			.domain(request.domain())
			.build();
		knowledgeBaseRepository.save(knowledgeBase);
	}

	@Transactional(readOnly = true)
	public List<KnowledgeItemResponse> getByIds(String ids) {
		List<String> parsedIds = parseIds(ids);
		Map<String, KnowledgeBase> knowledgeById = new LinkedHashMap<>();

		knowledgeBaseRepository.findAllById(parsedIds)
			.forEach(knowledge -> knowledgeById.put(knowledge.getId(), knowledge));

		return parsedIds.stream()
			.map(knowledgeById::get)
			.filter(knowledge -> knowledge != null)
			.map(KnowledgeItemResponse::from)
			.toList();
	}

	private List<String> parseIds(String ids) {
		if (ids == null || ids.isBlank()) {
			throw new CustomException(ErrorCode.KNOWLEDGE_IDS_REQUIRED);
		}

		List<String> parsedIds = Arrays.stream(ids.split(","))
			.map(String::trim)
			.filter(id -> !id.isBlank())
			.distinct()
			.toList();

		if (parsedIds.isEmpty()) {
			throw new CustomException(ErrorCode.KNOWLEDGE_IDS_REQUIRED);
		}
		if (parsedIds.size() > MAX_IDS) {
			throw new CustomException(ErrorCode.KNOWLEDGE_IDS_LIMIT_EXCEEDED);
		}
		return parsedIds;
	}
}
