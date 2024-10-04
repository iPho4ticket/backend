package com.ticketing.authservice.infrastructure.helper.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ticketing.authservice.infrastructure.common.RoleType;

/**
 * 테스트에 사용될 임의의 필드 값을 정의하는 클래스입니다.
 * 이 클래스는 주로 테스트 시 반복적으로 사용되는 상수 값을 관리합니다.
 */
public class ArbitraryField {

	/** 테스트용 사용자 ID */
	public static final Long USER_ID = 1L;
	/** 테스트용 사용자 이름 */
	public static final String USER_NAME = "Test User";
	/** 테스트용 사용자 이메일 */
	public static final String USER_EMAIL = "testuser@example.com";
	/** 테스트용 사용자 비밀번호 (평문) */
	public static final String PASSWORD = "!P@ssW0rd";
	/** 테스트용 사용자 전화번호 */
	public static final String PHONE_NUMBER = "010-1234-5678";
	/** 테스트용 JWT 토큰 */
	public static final String JWT_TOKEN = "sample.jwt.token";
	/** 테스트용 사용자 권한 */
	public static final RoleType USER_ROLE = RoleType.USER;
	/** JWT 테스트용 시크릿 키 (최소 32바이트) */
	public static final String JWT_SECRET = "myJwtSecretKeyForTesting1234567890!";
	/** JWT 테스트용 만료 시간 (밀리초) */
	public static final long JWT_EXPIRATION = 3600000L; // 1시간
	private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	/** 테스트용 해시된 비밀번호 */
	public static final String HASHED_PASSWORD = passwordEncoder.encode(PASSWORD);
}