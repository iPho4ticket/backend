package com.ticketing.userservice.infrastructure.common.codes;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum SuccessCode implements ResponseCode, Serializable {
	// 조회 성공 코드 (HTTP Response: 200 OK)
	SELECT_SUCCESS(HttpStatus.OK, "S001", "Select success"),
	// 삭제 성공 코드 (HTTP Response: 200 OK)
	DELETE_SUCCESS(HttpStatus.OK, "S002", "Delete success"),
	// 삽입 성공 코드 (HTTP Response: 201 Created)
	INSERT_SUCCESS(HttpStatus.CREATED, "S003", "Insert success"),
	// 수정 성공 코드 (HTTP Response: 200 OK)
	UPDATE_SUCCESS(HttpStatus.OK, "S004", "Update success");

	// 성공 코드
	private final HttpStatus status;

	// 구분 코드 (division code)
	private final String divisionCode;

	private final String message;

	SuccessCode(HttpStatus status, String divisionCode, String message) {
		this.status = status;
		this.divisionCode = divisionCode;
		this.message = message;
	}
}