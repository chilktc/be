package be.greenroom.graph.repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import be.greenroom.graph.domain.GraphGroup;
import be.greenroom.graph.domain.UserGraphNode;

public interface UserGraphNodeRepository extends JpaRepository<UserGraphNode, Long> {
	List<UserGraphNode> findByUserIdOrderByLastSeenDesc(UUID userId);

	Optional<UserGraphNode> findByUserIdAndLabelAndGroup(UUID userId, String label, GraphGroup group);
}
