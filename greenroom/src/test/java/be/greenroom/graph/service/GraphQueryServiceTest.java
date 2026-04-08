package be.greenroom.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.greenroom.graph.domain.GraphGroup;
import be.greenroom.graph.domain.GraphTrend;
import be.greenroom.graph.domain.UserGraphEdge;
import be.greenroom.graph.domain.UserGraphNode;
import be.greenroom.graph.dto.response.GraphEdgeResponse;
import be.greenroom.graph.dto.response.GraphNodeResponse;
import be.greenroom.graph.dto.response.GraphNodesResponse;
import be.greenroom.graph.dto.response.GraphUserDataResponse;
import be.greenroom.graph.repository.dao.OrganizationGraphQueryDao;
import be.greenroom.graph.repository.UserGraphEdgeRepository;
import be.greenroom.graph.repository.UserGraphNodeRepository;

@ExtendWith(MockitoExtension.class)
class GraphQueryServiceTest {

	private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Mock
	private UserGraphNodeRepository userGraphNodeRepository;
	@Mock
	private UserGraphEdgeRepository userGraphEdgeRepository;
	@Mock
	private OrganizationGraphQueryDao organizationGraphQueryDao;

	@InjectMocks
	private GraphQueryService graphQueryService;

	@Test
	@DisplayName("AI 서버용 누적 조회는 graph_cumulative 타입으로 반환한다")
	void 누적조회_응답구성() {
		when(userGraphNodeRepository.findByUserIdOrderByLastSeenDesc(USER_ID)).thenReturn(List.of(
			UserGraphNode.create(USER_ID, "업무과부하", GraphGroup.WORK_STRUCTURE, 0.81, 3, GraphTrend.INCREASING, LocalDateTime.now(), LocalDateTime.now())
		));
		when(userGraphEdgeRepository.findByUserIdOrderByLastSeenDesc(USER_ID)).thenReturn(List.of());

		GraphNodesResponse result = graphQueryService.getCumulativeGraph(USER_ID);

		assertThat(result.userId()).isEqualTo(USER_ID);
		assertThat(result.type()).isEqualTo("graph_cumulative");
		assertThat(result.data().nodes()).hasSize(1);
	}

	@Test
	@DisplayName("프론트엔드 조회는 category_distribution을 mention_count 기준으로 계산한다")
	void 시각화조회_카테고리분포계산() {
		when(userGraphNodeRepository.findByUserIdOrderByLastSeenDesc(USER_ID)).thenReturn(List.of(
			UserGraphNode.create(USER_ID, "의견 무시", GraphGroup.LEADERSHIP, 0.69, 3, GraphTrend.STABLE, LocalDateTime.now(), LocalDateTime.now()),
			UserGraphNode.create(USER_ID, "번아웃", GraphGroup.EMOTIONAL_EXHAUSTION, 0.81, 5, GraphTrend.INCREASING, LocalDateTime.now(), LocalDateTime.now())
		));
		when(userGraphEdgeRepository.findByUserIdOrderByLastSeenDesc(USER_ID)).thenReturn(List.of(
			UserGraphEdge.create(USER_ID, "의견 무시", GraphGroup.LEADERSHIP, "번아웃", GraphGroup.EMOTIONAL_EXHAUSTION, 4, "causes", LocalDateTime.now(), LocalDateTime.now())
		));

		GraphUserDataResponse result = graphQueryService.getGraphVisualizationData(USER_ID);

		assertThat(result.nodes()).hasSize(2);
		assertThat(result.links()).hasSize(1);
		assertThat(result.categoryDistribution()).containsEntry("leadership", 3L);
		assertThat(result.categoryDistribution()).containsEntry("emotional_exhaustion", 5L);
	}

	@Test
	@DisplayName("조직 조회는 집계 쿼리 결과를 그대로 시각화 응답으로 반환한다")
	void 조직조회_응답구성() {
		when(organizationGraphQueryDao.findOrganizationNodes()).thenReturn(List.of(
			new GraphNodeResponse("의견 무시", "leadership", null, 0.72, 14, null, null, null)
		));
		when(organizationGraphQueryDao.findOrganizationLinks()).thenReturn(List.of(
			new GraphEdgeResponse("의견 무시", null, "번아웃", null, 18, null, null, null)
		));
		when(organizationGraphQueryDao.findOrganizationCategoryDistribution()).thenReturn(
			java.util.Map.of("leadership", 52L, "emotional_exhaustion", 77L)
		);

		GraphUserDataResponse result = graphQueryService.getOrganizationGraphVisualizationData();

		assertThat(result.nodes()).hasSize(1);
		assertThat(result.links()).hasSize(1);
		assertThat(result.categoryDistribution()).containsEntry("leadership", 52L);
	}
}
