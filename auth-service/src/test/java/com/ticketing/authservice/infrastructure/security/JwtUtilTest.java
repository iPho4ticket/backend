package com.ticketing.authservice.infrastructure.security;

import static com.ticketing.authservice.infrastructure.helper.util.ArbitraryField.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JwtUtilTest는 JwtUtil 클래스의 토큰 생성 및 검증 기능을 테스트합니다.
 */
class JwtUtilTest {

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil(JWT_SECRET, JWT_EXPIRATION);
	}

	/**
	 * 유효한 JWT 토큰에서 클레임이 올바르게 추출되는지 테스트합니다.
	 */
	@Test
	@DisplayName("유효한 토큰 생성 시 클레임이 정상적으로 추출")
	void validToken_extractingClaims_claimsSuccessfullyExtracted() {
		// Given: 임의의 유효한 JWT 토큰 생성
		String validJwtToken = jwtUtil.createToken(USER_ID, USER_EMAIL, USER_ROLE.getAuthority());

		// When: 토큰에서 클레임 추출
		Claims extractedClaims = jwtUtil.extractClaims(validJwtToken);

		// Then: Hamcrest를 사용하여 토큰에서 추출된 정보와 유효성을 확인
		assertThat(extractedClaims.getSubject(), equalTo(USER_ID.toString()));
		assertThat(extractedClaims.get("email"), equalTo(USER_EMAIL));
		assertThat(extractedClaims.get("role"), equalTo(USER_ROLE.getAuthority()));
		assertThat(jwtUtil.isTokenValid(validJwtToken), is(true)); // 유효한 토큰인지 추가 검증
	}

	/**
	 * 만료된 JWT 토큰의 유효성 검증이 실패하는지 테스트합니다.
	 */
	@Test
	@DisplayName("만료된 토큰의 유효성 검증 시 실패")
	void expiredToken_validatingToken_validationFails() {
		// Given: 만료된 JWT 토큰 생성
		Date expiredDate = new Date(System.currentTimeMillis() - 1000); // 1초 전 만료
		String expiredToken = Jwts.builder()
			.setSubject(USER_ID.toString())
			.claim("email", USER_EMAIL)
			.claim("role", USER_ROLE.getAuthority())
			.setIssuedAt(new Date(System.currentTimeMillis() - 2000)) // 2초 전 발행
			.setExpiration(expiredDate)
			.signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()), SignatureAlgorithm.HS256)
			.compact();

		// When: 토큰의 유효성 검증
		boolean isValid = jwtUtil.isTokenValid(expiredToken);

		// Then: 만료된 토큰이 유효하지 않은지 Hamcrest로 확인
		assertThat(isValid, is(false));
	}
}