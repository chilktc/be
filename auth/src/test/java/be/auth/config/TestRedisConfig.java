package be.auth.config;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile("test")
public class TestRedisConfig {

	@Bean
	RedisConnectionFactory redisConnectionFactory() {
		return mock(RedisConnectionFactory.class);
	}

	@Bean
	StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return mock(StringRedisTemplate.class);
	}
}
