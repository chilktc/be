package be.notification.template;

import org.springframework.stereotype.Component;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.notification.domain.GreenroomTemplateCode;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GreenroomTemplateRegistry {

	private final GreenroomTemplateProperties properties;

	public GreenroomTemplate get(GreenroomTemplateCode code) {
		GreenroomTemplateProperties.TemplateConfig config = properties.getTemplates().get(code.name());
		if (config == null) {
			throw new CustomException(ErrorCode.GREENROOM_TEMPLATE_NOT_FOUND);
		}
		return new GreenroomTemplate(
			config.getSubject(),
			config.getBody(),
			config.getCtaText()
		);
	}
}
