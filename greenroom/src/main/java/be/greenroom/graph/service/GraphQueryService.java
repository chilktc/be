package be.greenroom.graph.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import be.greenroom.graph.dto.response.GraphCumulativeDataResponse;
import be.greenroom.graph.dto.response.GraphEdgeResponse;
import be.greenroom.graph.dto.response.GraphNodeResponse;
import be.greenroom.graph.dto.response.GraphNodesResponse;
import be.greenroom.graph.dto.response.GraphUserDataResponse;
import be.greenroom.graph.repository.dao.OrganizationGraphQueryDao;
import be.greenroom.graph.repository.UserGraphEdgeRepository;
import be.greenroom.graph.repository.UserGraphNodeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraphQueryService {

	private static final String GRAPH_CUMULATIVE_TYPE = "graph_cumulative";

	private final UserGraphNodeRepository userGraphNodeRepository;
	private final UserGraphEdgeRepository userGraphEdgeRepository;
	private final OrganizationGraphQueryDao organizationGraphQueryDao;

	@Transactional(readOnly = true)
	public GraphNodesResponse getCumulativeGraph(UUID userId) {
		List<GraphNodeResponse> nodes = userGraphNodeRepository.findByUserIdOrderByLastSeenDesc(userId)
			.stream()
			.map(this::toCumulativeNodeResponse)
			.toList();
		List<GraphEdgeResponse> links = userGraphEdgeRepository.findByUserIdOrderByLastSeenDesc(userId)
			.stream()
			.map(this::toCumulativeEdgeResponse)
			.toList();

		return new GraphNodesResponse(userId, GRAPH_CUMULATIVE_TYPE, new GraphCumulativeDataResponse(nodes, links));
	}

	@Transactional(readOnly = true)
	public GraphUserDataResponse getGraphVisualizationData(UUID userId) {
		var nodeEntities = userGraphNodeRepository.findByUserIdOrderByLastSeenDesc(userId);
		var edgeEntities = userGraphEdgeRepository.findByUserIdOrderByLastSeenDesc(userId);
		List<GraphNodeResponse> nodes = nodeEntities.stream()
			.map(this::toVisualizationNodeResponse)
			.toList();
		List<GraphEdgeResponse> links = edgeEntities.stream()
			.map(this::toVisualizationEdgeResponse)
			.toList();

		Map<String, Long> categoryDistribution = toCategoryDistribution(nodeEntities);

		return validateVisualizationResponse(new GraphUserDataResponse(nodes, links, categoryDistribution));
	}

	// 조직 전체 활성 사용자 기준 그래프 시각화 데이터를 조회 (하나의 조직만 있다고 가정)
	@Transactional(readOnly = true)
	public GraphUserDataResponse getOrganizationGraphVisualizationData() {
		return validateVisualizationResponse(new GraphUserDataResponse(
			organizationGraphQueryDao.findOrganizationNodes(),
			organizationGraphQueryDao.findOrganizationLinks(),
			organizationGraphQueryDao.findOrganizationCategoryDistribution()
		));
	}

	private GraphNodeResponse toCumulativeNodeResponse(be.greenroom.graph.domain.UserGraphNode node) {
		try {
			return validateCumulativeNodeResponse(GraphNodeResponse.forCumulative(node));
		} catch (RuntimeException e) {
			throw new CustomException(ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH);
		}
	}

	private GraphEdgeResponse toCumulativeEdgeResponse(be.greenroom.graph.domain.UserGraphEdge edge) {
		try {
			return validateCumulativeEdgeResponse(GraphEdgeResponse.forCumulative(edge));
		} catch (RuntimeException e) {
			throw new CustomException(ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH);
		}
	}

	private GraphNodeResponse toVisualizationNodeResponse(be.greenroom.graph.domain.UserGraphNode node) {
		try {
			return validateVisualizationNodeResponse(GraphNodeResponse.forVisualization(node));
		} catch (RuntimeException e) {
			throw new CustomException(ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH);
		}
	}

	private GraphEdgeResponse toVisualizationEdgeResponse(be.greenroom.graph.domain.UserGraphEdge edge) {
		try {
			return validateVisualizationEdgeResponse(GraphEdgeResponse.forVisualization(edge));
		} catch (RuntimeException e) {
			throw new CustomException(ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH);
		}
	}

	private GraphNodeResponse validateCumulativeNodeResponse(GraphNodeResponse response) {
		Preconditions.validate(
			response.label() != null && !response.label().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.grp() != null && !response.grp().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.weight() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.mentionCount() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.trend() != null && !response.trend().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.firstSeen() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.lastSeen() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		return response;
	}

	private GraphEdgeResponse validateCumulativeEdgeResponse(GraphEdgeResponse response) {
		Preconditions.validate(
			response.sourceLabel() != null && !response.sourceLabel().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.sourceGrp() != null && !response.sourceGrp().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.targetLabel() != null && !response.targetLabel().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.targetGrp() != null && !response.targetGrp().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.weight() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.relationship() != null && !response.relationship().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.firstSeen() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.lastSeen() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		return response;
	}

	private GraphNodeResponse validateVisualizationNodeResponse(GraphNodeResponse response) {
		Preconditions.validate(
			response.label() != null && !response.label().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.group() != null && !response.group().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.weight() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.mentionCount() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		return response;
	}

	private GraphEdgeResponse validateVisualizationEdgeResponse(GraphEdgeResponse response) {
		Preconditions.validate(
			response.sourceLabel() != null && !response.sourceLabel().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.targetLabel() != null && !response.targetLabel().isBlank(),
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.weight() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		return response;
	}

	private GraphUserDataResponse validateVisualizationResponse(GraphUserDataResponse response) {
		Preconditions.validate(
			response.nodes() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.links() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		Preconditions.validate(
			response.categoryDistribution() != null,
			ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH
		);
		response.nodes().forEach(this::validateVisualizationNodeResponse);
		response.links().forEach(this::validateVisualizationEdgeResponse);
		return response;
	}

	private Map<String, Long> toCategoryDistribution(List<be.greenroom.graph.domain.UserGraphNode> nodeEntities) {
		try {
			Map<String, Long> categoryDistribution = new LinkedHashMap<>();
			nodeEntities.forEach(node ->
				categoryDistribution.merge(node.getGroup().toApiValue(), (long) node.getMentionCount(), Long::sum)
			);
			return categoryDistribution;
		} catch (RuntimeException e) {
			throw new CustomException(ErrorCode.GRAPH_RESPONSE_SCHEMA_MISMATCH);
		}
	}
}
