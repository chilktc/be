package be.greenroom.knowledge.dto.response;

import be.greenroom.knowledge.domain.KnowledgeBase;

public record KnowledgeItemResponse(
	String id,
	String title,
	String content,
	Integer page,
	String source,
	String domain
) {
	public static KnowledgeItemResponse from(KnowledgeBase knowledgeBase) {
		return new KnowledgeItemResponse(
			knowledgeBase.getId(),
			knowledgeBase.getTitle(),
			knowledgeBase.getContent(),
			knowledgeBase.getPage(),
			knowledgeBase.getSource(),
			knowledgeBase.getDomain()
		);
	}
}
