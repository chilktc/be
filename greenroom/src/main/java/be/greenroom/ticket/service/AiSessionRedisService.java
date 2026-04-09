package be.greenroom.ticket.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
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
		redisTemplate.opsForValue().set(key(userId), sessionId, ttl);
	}

	public String get(UUID userId) {
		return redisTemplate.opsForValue().get(key(userId));
	}

	public void delete(UUID userId) {
		redisTemplate.delete(key(userId));
	}

	public void saveCompleted(String sessionId, Duration ttl) {
		redisTemplate.opsForValue().set(completedKey(sessionId), Boolean.TRUE.toString(), ttl);
	}
}
