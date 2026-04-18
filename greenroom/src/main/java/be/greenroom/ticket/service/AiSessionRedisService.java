package be.greenroom.ticket.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiSessionRedisService {

	private static final String PREFIX = "AI:SESSION:";
	private static final String COMPLETED_PREFIX = "AI:SESSION:COMPLETED:";

	private final StringRedisTemplate redisTemplate;

	private String key(UUID userId) {
		return PREFIX + userId;
	}

	private String completedKey(String sessionId) {
		return COMPLETED_PREFIX + sessionId;
	}

	public void save(UUID userId, String sessionId, Duration ttl) {
		log.info("[AI_TICKET][REDIS] save userId={}, sessionId={}, ttlSeconds={}", userId, sessionId, ttl.getSeconds());
		redisTemplate.opsForValue().set(key(userId), sessionId, ttl);
	}

	public String get(UUID userId) {
		return redisTemplate.opsForValue().get(key(userId));
	}

	public void delete(UUID userId) {
		log.info("[AI_TICKET][REDIS] delete userId={}", userId);
		redisTemplate.delete(key(userId));
	}

	public void deleteIfMatches(UUID userId, String sessionId) {
		String currentSessionId = get(userId);
		if (sessionId != null && sessionId.equals(currentSessionId)) {
			log.info("[AI_TICKET][REDIS] deleteIfMatches userId={}, sessionId={}", userId, sessionId);
			redisTemplate.delete(key(userId));
		}
	}

	public void saveCompleted(String sessionId, Duration ttl) {
		log.info("[AI_TICKET][REDIS] saveCompleted sessionId={}, ttlSeconds={}", sessionId, ttl.getSeconds());
		redisTemplate.opsForValue().set(completedKey(sessionId), Boolean.TRUE.toString(), ttl);
	}

	public void deleteCompleted(String sessionId) {
		log.info("[AI_TICKET][REDIS] deleteCompleted sessionId={}", sessionId);
		redisTemplate.delete(completedKey(sessionId));
	}
}
