package be.common.docs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import be.common.api.ErrorCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfiguration {

	@Bean
	public OpenAPI api() {
		Info info = new Info()
			.title("Auth API")
			.version("1.0")
			.description("Auth API documentation");

		SecurityScheme accessTokenScheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization");

		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList("Access Token");

		return new OpenAPI()
			.info(info)
			.servers(List.of(new Server().url("/")))
			.components(new Components().addSecuritySchemes("Access Token", accessTokenScheme))
			.addSecurityItem(securityRequirement);
	}

	@Bean
	public OperationCustomizer customize() {
		return (Operation operation, HandlerMethod handlerMethod) -> {
			ApiErrorCodeExamples apiErrorCodeExamples = handlerMethod.getMethodAnnotation(
				ApiErrorCodeExamples.class);

			if (apiErrorCodeExamples != null) {
				generateErrorCodeResponseExample(operation, apiErrorCodeExamples.value());
			} else {
				ApiErrorCodeExample apiErrorCodeExample = handlerMethod.getMethodAnnotation(
					ApiErrorCodeExample.class);

				if (apiErrorCodeExample != null) {
					generateErrorCodeResponseExample(operation, apiErrorCodeExample.value());
				}
			}

			return operation;
		};
	}

	// 여러 개의 에러 응답값 추가
	private void generateErrorCodeResponseExample(Operation operation, ErrorCode[] errorCodes) {
		ApiResponses responses = operation.getResponses();

		Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorCodes)
			.map(
				errorCode -> ExampleHolder.builder()
					.holder(getSwaggerExample(errorCode))
					.code(errorCode.getStatus().value())
					.name(errorCode.name())
					.build()
			)
			.collect(Collectors.groupingBy(ExampleHolder::getCode));

		addExamplesToResponses(responses, statusWithExampleHolders);
	}

	private void generateErrorCodeResponseExample(Operation operation, ErrorCode errorCode) {
		ApiResponses responses = operation.getResponses();

		ExampleHolder exampleHolder = ExampleHolder.builder()
			.holder(getSwaggerExample(errorCode))
			.name(errorCode.name())
			.code(errorCode.getStatus().value())
			.build();
		addExamplesToResponses(responses, exampleHolder);
	}

	private Example getSwaggerExample(ErrorCode errorCode) {
		SwaggerErrorResponse swaggerErrorResponse = SwaggerErrorResponse.from(errorCode);
		Example example = new Example();
		example.setValue(swaggerErrorResponse);

		return example;
	}

	private void addExamplesToResponses(ApiResponses responses,
		Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
		statusWithExampleHolders.forEach(
			(status, v) -> {
				Content content = new Content();
				MediaType mediaType = new MediaType();
				ApiResponse apiResponse = new ApiResponse();

				v.forEach(
					exampleHolder -> mediaType.addExamples(
						exampleHolder.getName(),
						exampleHolder.getHolder()
					)
				);
				content.addMediaType("application/json", mediaType);
				apiResponse.setContent(content);
				responses.addApiResponse(String.valueOf(status), apiResponse);
			}
		);
	}

	private void addExamplesToResponses(ApiResponses responses, ExampleHolder exampleHolder) {
		Content content = new Content();
		MediaType mediaType = new MediaType();
		ApiResponse apiResponse = new ApiResponse();

		mediaType.addExamples(exampleHolder.getName(), exampleHolder.getHolder());
		content.addMediaType("application/json", mediaType);
		apiResponse.content(content);
		responses.addApiResponse(String.valueOf(exampleHolder.getCode()), apiResponse);
	}
}
