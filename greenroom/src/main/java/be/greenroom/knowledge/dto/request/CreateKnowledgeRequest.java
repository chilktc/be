package be.greenroom.knowledge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateKnowledgeRequest(
	@NotBlank String id,
	@NotBlank String title,
	@NotBlank String content,
	@NotNull Integer page,
	@NotBlank String source,
	@NotBlank String domain
) {
}
