package be.auth;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class AuthApplicationTests {

	@MockitoBean
	private RedisConnectionFactory redisConnectionFactory;

	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	@Test
	void contextLoads() {
	}

}
