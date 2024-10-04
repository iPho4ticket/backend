package com.ticketing.authservice.infrastructure.helper;

import static com.ticketing.authservice.infrastructure.common.RoleType.*;
import static com.ticketing.authservice.infrastructure.helper.util.ArbitraryField.*;

import com.ticketing.authservice.application.dto.UserDto;

public class ArbitraryUserFactory {

	public static UserDto.Auth.Login createLoginDto() {
		return UserDto.Auth.Login.builder()
			.email(USER_EMAIL)
			.password(PASSWORD)
			.build();
	}

	public static UserDto.Auth.Result createAuthResult() {
		return UserDto.Auth.Result.builder()
			.id(USER_ID)
			.name(USER_NAME)
			.email(USER_EMAIL)
			.password(HASHED_PASSWORD)
			.role(USER)
			.build();
	}
}