package be.greenroom.graph.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.graph.domain.GraphGroup;
import be.greenroom.graph.domain.UserGraphEdge;

public interface UserGraphEdgeRepository extends JpaRepository<UserGraphEdge, Long> {
	List<UserGraphEdge> findByUserIdOrderByLastSeenDesc(UUID userId);

	Optional<UserGraphEdge> findByUserIdAndSourceLabelAndSourceGroupAndTargetLabelAndTargetGroup(
		UUID userId,
		String sourceLabel,
		GraphGroup sourceGroup,
		String targetLabel,
		GraphGroup targetGroup
	);
}
