package be.greenroom.graph.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.common.utils.Preconditions;
import be.greenroom.graph.domain.GraphAnalysis;
import be.greenroom.graph.domain.GraphGroup;
import be.greenroom.graph.domain.GraphTrend;
import be.greenroom.graph.domain.UserGraphEdge;
import be.greenroom.graph.domain.UserGraphNode;
import be.greenroom.graph.dto.request.CreateGraphAnalysisRequest;
import be.greenroom.graph.dto.request.GraphEdgeUpsertItem;
import be.greenroom.graph.dto.request.GraphNodeUpsertItem;
import be.greenroom.graph.dto.request.PutGraphNodesRequest;
import be.greenroom.graph.repository.GraphAnalysisRepository;
import be.greenroom.graph.repository.UserGraphEdgeRepository;
import be.greenroom.graph.repository.UserGraphNodeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GraphCommandService {

	private static final String GRAPH_CUMULATIVE_TYPE = "graph_cumulative";

	private final UserGraphNodeRepository userGraphNodeRepository;
	private final UserGraphEdgeRepository userGraphEdgeRepository;
	private final GraphAnalysisRepository graphAnalysisRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void upsertCumulativeGraph(PutGraphNodesRequest request) {
		Preconditions.validate(
			request.data() != null,
			ErrorCode.GRAPH_PAYLOAD_REQUIRED
		);
		Preconditions.validate(
			GRAPH_CUMULATIVE_TYPE.equals(request.type()),
			ErrorCode.GRAPH_INVALID_REQUEST_TYPE
		);

		// 노드 목록을 순회하며 기존 노드는 누적 업데이트, 신규 노드는 새로 저장
		for (GraphNodeUpsertItem node : safeNodes(request)) {
			GraphGroup group = GraphGroup.from(node.grp());
			GraphTrend trend = GraphTrend.from(node.trend());
			String label = requireText(node.label(), ErrorCode.GRAPH_NODE_LABEL_REQUIRED);
			double weight = requireDouble(node.weight(), ErrorCode.GRAPH_WEIGHT_REQUIRED);
			int mentionCount = requireInt(node.mentionCount(), ErrorCode.GRAPH_MENTION_COUNT_REQUIRED);
			LocalDateTime firstSeen = node.firstSeen() != null ? node.firstSeen() : LocalDateTime.now();
			LocalDateTime lastSeen = node.lastSeen() != null ? node.lastSeen() : firstSeen;
			userGraphNodeRepository.findByUserIdAndLabelAndGroup(request.userId(), label, group)
				.ifPresentOrElse(existing -> {
					existing.updateCumulative(weight, mentionCount, trend, lastSeen);
				}, () -> userGraphNodeRepository.save(UserGraphNode.create(
					request.userId(),
					label,
					group,
					weight,
					mentionCount,
					trend,
					firstSeen,
					lastSeen
				)));
		}

		// 엣지 목록을 순회하며 기존 엣지는 누적 업데이트, 신규 엣지는 새로 저장
		for (GraphEdgeUpsertItem edge : safeLinks(request)) {
			GraphGroup sourceGroup = GraphGroup.from(edge.sourceGrp());
			GraphGroup targetGroup = GraphGroup.from(edge.targetGrp());
			String sourceLabel = requireText(edge.sourceLabel(), ErrorCode.GRAPH_EDGE_SOURCE_LABEL_REQUIRED);
			String targetLabel = requireText(edge.targetLabel(), ErrorCode.GRAPH_EDGE_TARGET_LABEL_REQUIRED);
			String relationship = requireText(edge.relationship(), ErrorCode.GRAPH_EDGE_RELATIONSHIP_REQUIRED);
			int weight = requireInt(edge.weight(), ErrorCode.GRAPH_WEIGHT_REQUIRED);
			LocalDateTime firstSeen = edge.firstSeen() != null ? edge.firstSeen() : LocalDateTime.now();
			LocalDateTime lastSeen = edge.lastSeen() != null ? edge.lastSeen() : firstSeen;
			userGraphEdgeRepository.findByUserIdAndSourceLabelAndSourceGroupAndTargetLabelAndTargetGroup(
					request.userId(),
					sourceLabel,
					sourceGroup,
					targetLabel,
					targetGroup
				)
				.ifPresentOrElse(existing -> {
					existing.updateCumulative(weight, relationship, lastSeen);
				}, () -> userGraphEdgeRepository.save(UserGraphEdge.create(
					request.userId(),
					sourceLabel,
					sourceGroup,
					targetLabel,
					targetGroup,
					weight,
					relationship,
					firstSeen,
					lastSeen
				)));
		}
	}

	@Transactional
	public void saveGraphAnalysis(CreateGraphAnalysisRequest request) {
		Preconditions.validate(request.payload() != null, ErrorCode.GRAPH_PAYLOAD_REQUIRED);
		try {
			graphAnalysisRepository.save(GraphAnalysis.create(
				request.userId(),
				request.episodeId(),
				request.sessionId(),
				requireText(request.analysisType(), ErrorCode.GRAPH_ANALYSIS_TYPE_REQUIRED),
				objectMapper.writeValueAsString(request.payload())
			));
		} catch (JsonProcessingException e) {
			throw new CustomException(ErrorCode.GRAPH_ANALYSIS_PAYLOAD_SERIALIZATION_FAILED);
		}
	}

	private Iterable<GraphNodeUpsertItem> safeNodes(PutGraphNodesRequest request) {
		return request.data().nodes() == null ? java.util.List.of() : request.data().nodes();
	}

	private Iterable<GraphEdgeUpsertItem> safeLinks(PutGraphNodesRequest request) {
		return request.data().links() == null ? java.util.List.of() : request.data().links();
	}

	private String requireText(String value, ErrorCode errorCode) {
		Preconditions.validate(value != null && !value.isBlank(), errorCode);
		return value.trim();
	}

	private double requireDouble(Double value, ErrorCode errorCode) {
		Preconditions.validate(value != null, errorCode);
		return value;
	}

	private int requireInt(Integer value, ErrorCode errorCode) {
		Preconditions.validate(value != null, errorCode);
		return value;
	}
}
