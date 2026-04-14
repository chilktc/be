package be.greenroom.knowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.knowledge.domain.KnowledgeBase;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, String> {
}
