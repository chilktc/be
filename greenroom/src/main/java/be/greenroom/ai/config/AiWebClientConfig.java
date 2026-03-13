package be.greenroom.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AiServerProperties.class)
public class AiWebClientConfig {

	@Bean
	public WebClient aiWebClient(AiServerProperties aiServerProperties) {
		return WebClient.builder()
			.baseUrl(aiServerProperties.baseUrl())
			.build();
	}
}
