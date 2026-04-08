package be.greenroom.graph.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.graph.domain.GraphAnalysis;

public interface GraphAnalysisRepository extends JpaRepository<GraphAnalysis, Long> {
}
