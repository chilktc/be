package be.greenroom.graph.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import be.common.api.ApiAdvice;
import be.greenroom.graph.dto.response.GraphCumulativeDataResponse;
import be.greenroom.graph.dto.response.GraphEdgeResponse;
import be.greenroom.graph.dto.response.GraphNodeResponse;
import be.greenroom.graph.dto.response.GraphNodesResponse;
import be.greenroom.graph.dto.response.GraphUserDataResponse;
import be.greenroom.graph.service.GraphCommandService;
import be.greenroom.graph.service.GraphQueryService;

class GraphControllerTest {

	private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private MockMvc mockMvc;
	private GraphQueryService graphQueryService;
	private GraphCommandService graphCommandService;

	@BeforeEach
	void setUp() {
		graphQueryService = mock(GraphQueryService.class);
		graphCommandService = mock(GraphCommandService.class);
		GraphController controller = new GraphController(graphQueryService, graphCommandService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(new ApiAdvice())
			.build();
	}

	@Test
	@DisplayName("누적 그래프 조회 API는 AI 서버용 누적 데이터를 반환한다")
	void 누적그래프조회_API_성공() throws Exception {
		when(graphQueryService.getCumulativeGraph(USER_ID)).thenReturn(
			new GraphNodesResponse(
				USER_ID,
				"graph_cumulative",
				new GraphCumulativeDataResponse(
					List.of(new GraphNodeResponse("업무과부하", null, "work_structure", 0.81, 3, "increasing", null, null)),
					List.of(new GraphEdgeResponse("업무과부하", "work_structure", "번아웃", "emotional_exhaustion", 3, "causes", null, null))
				)
			)
		);

		mockMvc.perform(get("/api/v1/graph_nodes").param("user_id", USER_ID.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.user_id").value(USER_ID.toString()))
			.andExpect(jsonPath("$.data.type").value("graph_cumulative"))
			.andExpect(jsonPath("$.data.data.nodes[0].grp").value("work_structure"));
	}

	@Test
	@DisplayName("누적 그래프 저장 API는 요청을 저장 서비스로 전달한다")
	void 누적그래프저장_API_성공() throws Exception {
		String body = """
			{
			  "user_id": "11111111-1111-1111-1111-111111111111",
			  "type": "graph_cumulative",
			  "data": {
			    "nodes": [
			      {
			        "label": "업무과부하",
			        "grp": "work_structure",
			        "weight": 0.81,
			        "mention_count": 3,
			        "trend": "increasing"
			      }
			    ],
			    "links": [
			      {
			        "source_label": "업무과부하",
			        "source_grp": "work_structure",
			        "target_label": "번아웃",
			        "target_grp": "emotional_exhaustion",
			        "weight": 3,
			        "relationship": "causes"
			      }
			    ]
			  }
			}
			""";

		mockMvc.perform(put("/api/v1/graph_nodes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("ok"));

		verify(graphCommandService).upsertCumulativeGraph(any());
	}

	@Test
	@DisplayName("그래프 분석 저장 API는 원본 payload를 저장 서비스로 전달한다")
	void 그래프분석저장_API_성공() throws Exception {
		String body = """
			{
			  "user_id": "11111111-1111-1111-1111-111111111111",
			  "episode_id": "ep_001",
			  "session_id": "sess_001",
			  "analysis_type": "got_analysis",
			  "payload": {
			    "summary": "test"
			  }
			}
			""";

		mockMvc.perform(post("/api/v1/graph_analyses")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("ok"));

		verify(graphCommandService).saveGraphAnalysis(any());
	}

	@Test
	@DisplayName("사용자 그래프 조회 API는 프론트엔드 시각화 데이터를 반환한다")
	void 사용자그래프조회_API_성공() throws Exception {
		when(graphQueryService.getGraphVisualizationData(USER_ID)).thenReturn(
			new GraphUserDataResponse(
				List.of(new GraphNodeResponse("의견 무시", "leadership", null, 0.69, 3, null, null, null)),
				List.of(new GraphEdgeResponse("의견 무시", null, "번아웃", null, 4, null, null, null)),
				Map.of("leadership", 15L, "emotional_exhaustion", 23L)
			)
		);

		mockMvc.perform(get("/api/v1/graph/users/{userId}/data", USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nodes[0].group").value("leadership"))
			.andExpect(jsonPath("$.data.links[0].source_label").value("의견 무시"))
			.andExpect(jsonPath("$.data.category_distribution.leadership").value(15));
	}

	@Test
	@DisplayName("조직 그래프 조회 API는 조직 시각화 데이터를 반환한다")
	void 조직그래프조회_API_성공() throws Exception {
		when(graphQueryService.getOrganizationGraphVisualizationData()).thenReturn(
			new GraphUserDataResponse(
				List.of(new GraphNodeResponse("의견 무시", "leadership", null, 0.72, 14, null, null, null)),
				List.of(new GraphEdgeResponse("의견 무시", null, "번아웃", null, 18, null, null, null)),
				Map.of("leadership", 52L, "emotional_exhaustion", 77L)
			)
		);

		mockMvc.perform(get("/api/v1/graph/organization/data"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nodes[0].group").value("leadership"))
			.andExpect(jsonPath("$.data.links[0].weight").value(18))
			.andExpect(jsonPath("$.data.category_distribution.leadership").value(52));
	}
}
