package be.greenroom.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.common.api.CustomException;
import be.common.api.ErrorCode;
import be.greenroom.graph.domain.GraphAnalysis;
import be.greenroom.graph.domain.UserGraphEdge;
import be.greenroom.graph.domain.UserGraphNode;
import be.greenroom.graph.dto.request.CreateGraphAnalysisRequest;
import be.greenroom.graph.dto.request.GraphCumulativePayload;
import be.greenroom.graph.dto.request.GraphEdgeUpsertItem;
import be.greenroom.graph.dto.request.GraphNodeUpsertItem;
import be.greenroom.graph.dto.request.PutGraphNodesRequest;
import be.greenroom.graph.repository.GraphAnalysisRepository;
import be.greenroom.graph.repository.UserGraphEdgeRepository;
import be.greenroom.graph.repository.UserGraphNodeRepository;

@ExtendWith(MockitoExtension.class)
class GraphCommandServiceTest {

	private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Mock
	private UserGraphNodeRepository userGraphNodeRepository;
	@Mock
	private UserGraphEdgeRepository userGraphEdgeRepository;
	@Mock
	private GraphAnalysisRepository graphAnalysisRepository;
	@Spy
	private ObjectMapper objectMapper;

	@InjectMocks
	private GraphCommandService graphCommandService;

	@Test
	@DisplayName("누적 그래프 저장 시 신규 노드와 엣지를 저장한다")
	void 누적그래프저장_신규저장() {
		PutGraphNodesRequest request = new PutGraphNodesRequest(
			USER_ID,
			"graph_cumulative",
			new GraphCumulativePayload(
				List.of(new GraphNodeUpsertItem("업무과부하", "work_structure", 0.81, 3, "increasing", null, null)),
				List.of(new GraphEdgeUpsertItem("업무과부하", "work_structure", "번아웃", "emotional_exhaustion", 3, "causes", null, null))
			)
		);
		when(userGraphNodeRepository.findByUserIdAndLabelAndGroup(any(), any(), any())).thenReturn(Optional.empty());
		when(userGraphEdgeRepository.findByUserIdAndSourceLabelAndSourceGroupAndTargetLabelAndTargetGroup(any(), any(), any(), any(), any()))
			.thenReturn(Optional.empty());

		graphCommandService.upsertCumulativeGraph(request);

		ArgumentCaptor<UserGraphNode> nodeCaptor = ArgumentCaptor.forClass(UserGraphNode.class);
		ArgumentCaptor<UserGraphEdge> edgeCaptor = ArgumentCaptor.forClass(UserGraphEdge.class);
		verify(userGraphNodeRepository).save(nodeCaptor.capture());
		verify(userGraphEdgeRepository).save(edgeCaptor.capture());
		assertThat(nodeCaptor.getValue().getGroup().toApiValue()).isEqualTo("work_structure");
		assertThat(edgeCaptor.getValue().getTargetGroup().toApiValue()).isEqualTo("emotional_exhaustion");
	}

	@Test
	@DisplayName("유효하지 않은 grp는 검증 오류를 반환한다")
	void 누적그래프저장_유효하지않은grp_검증오류() {
		PutGraphNodesRequest request = new PutGraphNodesRequest(
			USER_ID,
			"graph_cumulative",
			new GraphCumulativePayload(
				List.of(new GraphNodeUpsertItem("업무과부하", "unknown_group", 0.81, 3, "increasing", null, null)),
				List.of()
			)
		);

		CustomException exception = Assertions.assertThrows(CustomException.class,
			() -> graphCommandService.upsertCumulativeGraph(request));

		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GRAPH_INVALID_GROUP);
	}

	@Test
	@DisplayName("self-loop 엣지는 데이터베이스 제약에 맡기고 서비스에서 제거하지 않는다")
	void 누적그래프저장_selfLoop도저장시도() {
		PutGraphNodesRequest request = new PutGraphNodesRequest(
			USER_ID,
			"graph_cumulative",
			new GraphCumulativePayload(
				List.of(),
				List.of(new GraphEdgeUpsertItem("번아웃", "emotional_exhaustion", "번아웃", "emotional_exhaustion", 3, "causes", null, null))
			)
		);
		when(userGraphEdgeRepository.findByUserIdAndSourceLabelAndSourceGroupAndTargetLabelAndTargetGroup(any(), any(), any(), any(), any()))
			.thenReturn(Optional.empty());

		graphCommandService.upsertCumulativeGraph(request);

		verify(userGraphEdgeRepository).save(any(UserGraphEdge.class));
	}

	@Test
	@DisplayName("그래프 분석 저장 시 payload를 JSON 문자열로 저장한다")
	void 그래프분석저장_payload직렬화() {
		CreateGraphAnalysisRequest request = new CreateGraphAnalysisRequest(
			USER_ID,
			"ep_001",
			"sess_001",
			"got_analysis",
			java.util.Map.of("summary", "test")
		);

		graphCommandService.saveGraphAnalysis(request);

		ArgumentCaptor<GraphAnalysis> captor = ArgumentCaptor.forClass(GraphAnalysis.class);
		verify(graphAnalysisRepository).save(captor.capture());
		assertThat(captor.getValue().getPayload()).contains("summary");
	}
}
