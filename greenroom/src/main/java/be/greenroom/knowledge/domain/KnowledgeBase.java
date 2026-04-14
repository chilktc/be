package be.greenroom.knowledge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "knowledge_base")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeBase {

	@Id
	@Column(nullable = false, updatable = false)
	private String id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private Integer page;

	@Column(nullable = false)
	private String source;

	@Column(nullable = false)
	private String domain;

	@Builder
	private KnowledgeBase(
		String id,
		String title,
		String content,
		Integer page,
		String source,
		String domain
	) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.page = page;
		this.source = source;
		this.domain = domain;
	}
}
