package be.greenroom.ai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.common.api.ApiResult;
import be.greenroom.ai.dto.request.ContentAnalysisIngestRequest;
import be.greenroom.ai.dto.request.CreateMindFrequencyRequest;
import be.greenroom.ai.dto.request.EmotionLogIngestRequest;
import be.greenroom.ai.dto.request.PodcastEpisodeIngestRequest;
import be.greenroom.ai.dto.request.VisualizationIngestRequest;
import be.greenroom.ai.service.MindFrequencyService;
import be.greenroom.ai.service.PodcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "AI Ingest", description = "AI 서버 결과 수신 API")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class IngestController {

	private final PodcastService podcastService;
	private final MindFrequencyService mindFrequencyService;

	@Operation(summary = "감정 로그 수신", description = "요청을 수신하고 즉시 200 OK를 반환합니다.")
	@PostMapping("/emotion_logs")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> receiveEmotionLogs(@RequestBody EmotionLogIngestRequest request) {
		return ApiResult.ok();
	}

	@Operation(summary = "시각화 수신", description = "요청을 수신하고 즉시 200 OK를 반환합니다.")
	@PostMapping("/visualizations")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> receiveVisualizations(@RequestBody EmotionLogIngestRequest request) {
		return ApiResult.ok();
	}

	@Operation(summary = "팟캐스트 메타데이터 수신", description = "요청을 수신하고 즉시 200 OK를 반환합니다.")
	@PostMapping("/podcast_metadata")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> receivePodcastMetadata(@RequestBody EmotionLogIngestRequest request) {
		return ApiResult.ok();
	}

	@Operation(summary = "콘텐츠 분석 수신", description = "요청을 수신하고 즉시 200 OK를 반환합니다.")
	@PostMapping("/content_analyses")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> receiveContentAnalyses(@RequestBody EmotionLogIngestRequest request) {
		return ApiResult.ok();
	}

	@Operation(summary = "학습데이터 수신", description = "요청을 수신하고 즉시 200 OK를 반환합니다.")
	@PostMapping("/learning")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> receiveLearning(@RequestBody EmotionLogIngestRequest request) {
		return ApiResult.ok();
	}

	@Operation(summary = "팟캐스트 에피소드 수신", description = "요청을 수신하고 즉시 200 OK를 반환합니다.")
	@PostMapping("/podcast_episodes")
	@ResponseStatus(HttpStatus.OK)
	public ApiResult<Void> receivePodcastEpisodes(@RequestBody @Valid PodcastEpisodeIngestRequest request) {
		podcastService.create(request);
		return ApiResult.ok();
	}

	@Operation(summary = "마음 주파수 수신", description = "키워드 목록과 설명을 저장합니다.")
	@PostMapping("/mind-frequencies")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResult<Void> receiveMindFrequencies(@RequestBody @Valid CreateMindFrequencyRequest request) {
		mindFrequencyService.create(request);
		return ApiResult.ok();
	}
}
