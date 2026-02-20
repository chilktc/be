package be.notification.template;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "greenroom")
public class GreenroomTemplateProperties {

	private Map<String, TemplateConfig> templates = new HashMap<>();

	@Getter
	@Setter
	public static class TemplateConfig {
		private String subject;
		private String body;
		private String ctaText;
	}
}
