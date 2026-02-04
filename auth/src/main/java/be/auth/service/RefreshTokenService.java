package be.auth.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final StringRedisTemplate redisTemplate;
	private static final String PREFIX = "RT:";

	private String key(UUID userId) {
		return PREFIX + userId.toString();
	}

	public void save(UUID userId, String refreshToken, long expireMs) {
		redisTemplate.opsForValue()
			.set(key(userId), refreshToken, Duration.ofMillis(expireMs));
	}

	public boolean isSame(UUID userId, String refreshToken) {
		String stored = redisTemplate.opsForValue().get(key(userId));
		return refreshToken.equals(stored);
	}

	public void delete(UUID userId) {
		redisTemplate.delete(key(userId));
	}
}
