package be.gateway.jwt;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistService {
	private final StringRedisTemplate redisTemplate;

	private static final String PREFIX = "BL:AT:";

	private String key(String jti) {
		return PREFIX + jti;
	}

	public boolean isExist(String jti) {
		return redisTemplate.hasKey(key(jti));
	}
}

