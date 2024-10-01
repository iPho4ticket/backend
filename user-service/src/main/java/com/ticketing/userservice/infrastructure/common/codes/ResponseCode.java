package com.ticketing.userservice.infrastructure.common.codes;

import org.springframework.http.HttpStatus;

public interface ResponseCode {
	HttpStatus getStatus();

	String getMessage();

	String getDivisionCode();
}