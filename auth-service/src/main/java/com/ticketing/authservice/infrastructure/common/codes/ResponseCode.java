package com.ticketing.authservice.infrastructure.common.codes;

import org.springframework.http.HttpStatus;

public interface ResponseCode { //TODO: 공통 모듈 적용시 통합
	HttpStatus getStatus();

	String getMessage();

	String getDivisionCode();
}