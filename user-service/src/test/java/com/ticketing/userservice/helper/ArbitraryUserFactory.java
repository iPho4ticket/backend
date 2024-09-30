package com.ticketing.userservice.helper;

import static com.ticketing.userservice.application.dto.UserDto.*;
import static com.ticketing.userservice.util.ArbitraryField.*;

import com.ticketing.userservice.application.dto.UserDto.*;
import com.ticketing.userservice.domain.User;

/**
 * ArbitraryUserFactory 클래스는 테스트용으로 User 엔티티 및 관련 DTO 객체를 생성하는 헬퍼 클래스입니다.
 * 필드 값은 ArbitraryField에서 가져옵니다.
 */
public class ArbitraryUserFactory {

	/**
	 * User 엔티티 객체를 생성합니다.
	 *
	 * @return 미리 정의된 필드를 사용한 User 객체
	 */
	public static User aUser() {
		return User.builder()
			.name(USER_NAME)
			.phoneNumber(USER_PHONE_NUMBER)
			.password(USER_PASSWORD)
			.email(USER_EMAIL)
			.role(USER_ROLE)
			.build();
	}

	/**
	 * User 생성 요청에 사용되는 Create DTO 객체를 생성합니다.
	 *
	 * @return 미리 정의된 필드를 사용한 Create DTO 객체
	 */
	public static Create aUserCreateDto() {
		return Create.builder()
			.name(USER_NAME)
			.email(USER_EMAIL)
			.password(USER_PASSWORD)
			.phoneNumber(USER_PHONE_NUMBER)
			.role(USER_ROLE)
			.build();
	}

	/**
	 * User 조회 결과에 사용되는 Result DTO 객체를 생성합니다.
	 *
	 * @return 미리 정의된 필드를 사용한 Result DTO 객체
	 */
	public static Result aUserResultDto() {
		return Result.builder()
			.id(USER_ID)
			.name(USER_NAME)
			.email(USER_EMAIL)
			.password(USER_PASSWORD)
			.phoneNumber(USER_PHONE_NUMBER)
			.role(USER_ROLE)
			.build();
	}

	public static Delete.Soft aUserDeleteSoftDto() {
		return Delete.Soft.builder()
			.id(USER_ID)
			.deleterId(DELETER_USER_ID)
			.build();
	}
}