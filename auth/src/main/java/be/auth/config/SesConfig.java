package be.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SesConfig {
	@Value("${AWS_REGION:ap-northeast-2}")
	private String region;

	@Bean
	public SesClient sesClient() {
		return SesClient.builder()
			.region(Region.of(region))
			.build();
	}
}