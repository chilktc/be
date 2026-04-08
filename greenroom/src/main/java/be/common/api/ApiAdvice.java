package be.common.api;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
public class ApiAdvice {
	// 어떤예외를 처리할 것인지 정의
	// MethodArgumentNotValidException 이 익셉션을 처리하도록
	// @ExceptionHandler(MethodArgumentNotValidException.class)
	// 500에러를 하나로 처리할때
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse.ErrorData> internalServerError(Exception e) {
		e.printStackTrace();//<-에러가뜬건지 알아볼때
		// 서버에러입니다
		return ErrorResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse.ErrorData> customException(CustomException e) {
		return ErrorResponse.error(e.getErrorCode());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse.ErrorData> methodArgumentNotValidException(
		MethodArgumentNotValidException e,
		HttpServletRequest request
	) {
		e.printStackTrace();
		var details = Arrays.toString(e.getDetailMessageArguments());
		var message = details.split(",", 2)[1].replace("]", "").trim();

		if (isGraphWriteEndpoint(request)) {
			return ErrorResponse.error(ErrorCode.GRAPH_REQUEST_SCHEMA_MISMATCH);
		}

		return ErrorResponse.error(ErrorCode.VALIDATION_ERROR);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse.ErrorData> httpMessageNotReadableException(
		HttpMessageNotReadableException e,
		HttpServletRequest request
	) {
		e.printStackTrace();

		if (isGraphWriteEndpoint(request)) {
			return ErrorResponse.error(ErrorCode.GRAPH_REQUEST_SCHEMA_MISMATCH);
		}

		return ErrorResponse.error(ErrorCode.VALIDATION_ERROR);
	}

	private boolean isGraphWriteEndpoint(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String method = request.getMethod();
		return ("PUT".equals(method) && "/api/v1/graph_nodes".equals(uri))
			|| ("POST".equals(method) && "/api/v1/graph_analyses".equals(uri));
	}

}
