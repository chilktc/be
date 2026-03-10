package be.greenroom.tracking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import be.common.api.ApiAdvice;
import be.greenroom.tracking.domain.ResolvedHelpType;
import be.greenroom.tracking.domain.ResolvedStateType;
import be.greenroom.tracking.domain.TrackingStatus;
import be.greenroom.tracking.dto.response.TrackingHistoryItemResponse;
import be.greenroom.tracking.service.TrackingService;

class TrackingControllerTest {

	private MockMvc mockMvc;
	private TrackingService trackingService;

	@BeforeEach
	void setUp() {
		trackingService = mock(TrackingService.class);
		TrackingController controller = new TrackingController(trackingService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(new ApiAdvice())
			.build();
	}

	@Test
	@DisplayName("트래킹 생성 API는 해결 요청을 등록한다")
	void 트래킹생성_API_성공() throws Exception {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		String body = """
			{
			  "status":"RESOLVED",
			  "resolvedHelpType":"DIALOGUE_AND_EXPRESSION",
			  "resolvedStateType":"FULLY_DONE",
			  "note":"정리됨"
			}
			""";

		// when then
		mockMvc.perform(post("/greenroom/tickets/{ticketId}/tracking", ticketId)
				.header("X-User-Id", userId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("ok"));

		verify(trackingService).create(eq(userId), eq(ticketId), any());
	}

	@Test
	@DisplayName("트래킹 조회 API는 최신순 목록을 반환한다")
	void 트래킹조회_API_성공() throws Exception {
		// given
		UUID userId = UUID.randomUUID();
		UUID ticketId = UUID.randomUUID();
		when(trackingService.getHistory(userId, ticketId)).thenReturn(List.of(
			new TrackingHistoryItemResponse(
				TrackingStatus.RESOLVED,
				LocalDateTime.of(2026, 3, 9, 8, 30),
				"D+8",
				null,
				ResolvedHelpType.ACTION_CHANGED_SITUATION,
				ResolvedStateType.MOSTLY_OK_SOMETIMES,
				null,
				null
			)
		));

		// when then
		mockMvc.perform(get("/greenroom/tickets/{ticketId}/tracking", ticketId)
				.header("X-User-Id", userId.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].status").value("RESOLVED"))
			.andExpect(jsonPath("$.data[0].dDay").value("D+8"))
			.andExpect(jsonPath("$.data[0].note").doesNotExist());
	}
}
