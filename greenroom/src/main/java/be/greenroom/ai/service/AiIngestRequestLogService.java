package be.greenroom.ai.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.ai.domain.AiIngestRequestLog;
import be.greenroom.ai.repository.AiIngestRequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiIngestRequestLogService {

	private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final AiIngestRequestLogRepository aiIngestRequestLogRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void save(String endpoint, String rawRequest) {
		try {
			Map<String, Object> requestMap = objectMapper.readValue(rawRequest, MAP_TYPE);
			Map<String, Object> normalized = normalizeMap(requestMap);
			String payload = objectMapper.writeValueAsString(normalized);

			aiIngestRequestLogRepository.save(
				AiIngestRequestLog.create(
					endpoint,
					stringValue(normalized.get("type")),
					stringValue(normalized.get("user_id")),
					stringValue(normalized.get("session_id")),
					payload
				)
			);
			log.info(
				"[AI_INGEST] saved {} requestLog userId={}, sessionId={}, type={}",
				endpoint,
				stringValue(normalized.get("user_id")),
				stringValue(normalized.get("session_id")),
				stringValue(normalized.get("type"))
			);
		} catch (JsonProcessingException e) {
			log.error("[AI_INGEST] failed to serialize {} request rawRequest={}", endpoint, rawRequest, e);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private Map<String, Object> normalizeMap(Map<String, Object> source) throws JsonProcessingException {
		Map<String, Object> normalized = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			normalized.put(entry.getKey(), normalizeValue(entry.getValue()));
		}
		return normalized;
	}

	private Object normalizeValue(Object value) throws JsonProcessingException {
		if (value == null) {
			return null;
		}
		if (value instanceof Map<?, ?> map) {
			Map<String, Object> normalized = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				normalized.put(String.valueOf(entry.getKey()), normalizeValue(entry.getValue()));
			}
			return normalized;
		}
		if (value instanceof List<?> list) {
			return objectMapper.writeValueAsString(list);
		}
		return value;
	}

	private String stringValue(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
