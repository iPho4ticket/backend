package com.ticketing.userservice.infrastructure.common.codes;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * [공통 코드] API 통신에 대한 '에러 코드'를 Enum 형태로 관리를 한다.
 * Global Error CodeList : 전역으로 발생하는 에러코드를 관리한다.
 * Custom Error CodeList : 업무 페이지에서 발생하는 에러코드를 관리한다
 * Error Code Constructor : 에러코드를 직접적으로 사용하기 위한 생성자를 구성한다.
 */
@Getter
public enum ErrorCode implements ResponseCode, Serializable {

	/**
	 * ******************************* Global Error CodeList ***************************************
	 * HTTP Status Code
	 * 400 : Bad Request
	 * 401 : Unauthorized
	 * 403 : Forbidden
	 * 404 : Not Found
	 * 500 : Internal Server Error
	 * *********************************************************************************************
	 */
	// 잘못된 서버 요청
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "G001", "Bad Request"),

	// @RequestBody 데이터 미 존재
	REQUEST_BODY_MISSING(HttpStatus.BAD_REQUEST, "G002", "Required request body is missing"),

	// 유효하지 않은 타입
	INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "G003", "Invalid Type Value"),

	// Request Parameter 로 데이터가 전달되지 않을 경우
	MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "G004", "Missing Servlet RequestParameter Exception"),

	// 입력/출력 값이 유효하지 않음
	IO_ERROR(HttpStatus.BAD_REQUEST, "G005", "I/O Exception"),

	// com.google.gson JSON 파싱 실패
	JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "G006", "JsonParseException"),

	// com.fasterxml.jackson.core Processing Error
	JACKSON_PROCESS_ERROR(HttpStatus.BAD_REQUEST, "G007", "com.fasterxml.jackson.core Exception"),

	// 권한이 없음
	FORBIDDEN(HttpStatus.FORBIDDEN, "G008", "Forbidden Exception"),

	// 서버로 요청한 리소스가 존재하지 않음
	NOT_FOUND(HttpStatus.NOT_FOUND, "G009", "Not Found Exception"),

	// NULL Point Exception 발생
	NULL_POINTER(HttpStatus.NOT_FOUND, "G010", "NullPointerException"),

	// @RequestBody 및 @RequestParam, @PathVariable 값이 유효하지 않음
	NOT_VALID(HttpStatus.NOT_FOUND, "G011", "handle Validation Exception"),

	// Header 에 데이터가 존재하지 않는 경우
	NOT_VALID_HEADER(HttpStatus.NOT_FOUND, "G012", "Header validation failed"),

	// 권한 없음 (401 Unauthorized)
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "G013", "Unauthorized"),

	// 업체와 상품간 ID 불일치
	COMPANY_MISMATCH(HttpStatus.BAD_REQUEST, "G014", "Company mismatch between order and product"),

	// 서버가 처리 할 방법을 모르는 경우 발생
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G999", "Internal Server Error Exception"),

	; // End

	/**
	 * ******************************* Error Code Constructor ***************************************
	 */
	// 에러 코드의 '코드 상태'를 반환한다.
	private final HttpStatus status;

	// 에러 코드의 '코드간 구분 값'을 반환한다.
	private final String divisionCode;

	// 에러 코드의 '코드 메시지'를 반환한다.
	private final String message;

	// 생성자 구성
	ErrorCode(final HttpStatus status, final String divisionCode, final String message) {
		this.status = status;
		this.divisionCode = divisionCode;
		this.message = message;
	}
}