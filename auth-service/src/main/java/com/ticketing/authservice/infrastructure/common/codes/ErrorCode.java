package com.ticketing.authservice.infrastructure.common.codes;

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
public enum ErrorCode implements ResponseCode, Serializable { //TODO: 공통 모듈 적용시 통합

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

	// 비밀번호 불일치
	PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "A001", "Invalid password"),

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